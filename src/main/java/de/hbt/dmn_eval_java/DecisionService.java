/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

import com.google.gson.Gson;
import de.hbt.dmn_eval_java.impl.DmnEvalJsWrapper;
import de.hbt.dmn_eval_java.impl.InputObjectWrapper;
import de.hbt.dmn_eval_java.impl.PromiseFuture;
import de.hbt.dmn_eval_java.impl.WrappedInputObject;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * The decision service allows to evaluate decisions using the {@link #evaluateDecision(String, Object, Class)}} method.
 * Before a decision can be evaluated, it needs to be registered first using the {@link #registerDecision(String, String)}} method.
 */
public class DecisionService {

    private static final Logger logger = LoggerFactory.getLogger(DecisionService.class);

    private ScriptEngine scriptEngine;
    private DmnEvalJsWrapper dmnEvalJsWrapper;

    private ScriptObjectMirror nashornEventLoop;
    private ConcurrentHashMap<String, ScriptObjectMirror> parsedDmnDefinitions = new ConcurrentHashMap<>();

    /**
     * Bootstraps the decision service.
     * @throws DecisionServiceInstantiationException thrown if the decision service could not be initialized properly
     */
    public DecisionService() {
        long startTime = System.nanoTime();
        logger.info("Starting the decision service...");
        initScriptEngine();
        bootstrapDmnEngineJs();
        logger.info("Decision service successfully started in {} seconds.", (System.nanoTime() - startTime) / 1E9);
    }

    private void initScriptEngine() {
        ScriptContext scriptContext;
        try {
            scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
            scriptContext = scriptEngine.getContext();
        } catch (Exception e) {
            throw new DecisionServiceInstantiationException("Failed to initialize script engine.", e);
        }
        runScript("my-timer-polyfill.js");
        runScript("nashorn-polyfill.js");

        nashornEventLoop = (ScriptObjectMirror) scriptContext.getBindings(ScriptContext.ENGINE_SCOPE).get("nashornEventLoop");
    }

    private void bootstrapDmnEngineJs() {
        ScriptObjectMirror dmnEvalJs = (ScriptObjectMirror) runScript("dmn-eval-js.js");
        runScript("dmn-eval-js-wrapper.js");
        try {
            dmnEvalJsWrapper = (DmnEvalJsWrapper) ((Invocable) scriptEngine).invokeFunction("newDmnEvalJsWrapper", dmnEvalJs);
        } catch (ScriptException | NoSuchMethodException e) {
            throw new DecisionServiceInstantiationException("Failed to load embedded dmn-eval-js engine.", e);
        }
    }

    /**
     * Registers the decision with the given id with the decision service. The DMN definition of the decision
     * is loaded from the file with the given name. That file must exist in the classpath.
     * Registering decision is an asynchronous operation. Calling this method returns a {@link Future}.
     * @param decisionId the DMN decision id of the decision
     * @param dmnFile the XML file with the DMN definition
     * @return the future of the registration
     * @throws DecisionRegistrationException thrown if the decision could not be registered
     */
    public Future<Decision> registerDecision(String decisionId, String dmnFile) {
        logger.info("Registering decision '{}'...", decisionId);
        String dmnXml;
        try {
            logger.info("Loading DMN definition from file '{}'...", dmnFile);
            dmnXml = new String(Files.readAllBytes(Paths.get(dmnFile)), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DecisionEvaluationException("Failed to load DMN file '" + dmnFile + "'", e);
        }

        long time = System.nanoTime();
        logger.info("Parsing DMN definition for decision '{}'...", decisionId);
        ScriptObjectMirror promise = dmnEvalJsWrapper.parseDmnXml(dmnXml);

        CompletableFuture<Decision> result = new PromiseFuture<>(promise, nashornEventLoop) {

            protected Consumer<?> resolve() {
                return (parsedDmnDefinition) -> {
                    parsedDmnDefinitions.put(decisionId, (ScriptObjectMirror) parsedDmnDefinition);
                    logger.info("Successfully parsed DMN definition for decision '{}' in {} msecs.", decisionId, (System.nanoTime() - time) / 1000000L);
                    Decision decision = getDecision((ScriptObjectMirror) ((ScriptObjectMirror) parsedDmnDefinition).get(decisionId));
                    if (decision == null) {
                        DecisionRegistrationException decisionRegistrationException = new DecisionRegistrationException("No decision '" + decisionId + "' is contained in the given DMN definition.");
                        this.completeExceptionally(decisionRegistrationException);
                    } else {
                        decision.setDecisionId(decisionId);
                        this.complete(decision);
                    }
                };
            }

            protected Consumer<?> reject() {
                return (error) -> {
                    logger.error("Failed to parse DMN definition for decision '{}': {}", decisionId, String.valueOf(error));
                    DecisionRegistrationException decisionRegistrationException;
                    if (error instanceof Throwable) {
                        decisionRegistrationException = new DecisionRegistrationException("Failed to register decision '" + decisionId + "'", (Throwable) error);
                    } else {
                        decisionRegistrationException = new DecisionRegistrationException("Failed to register decision '" + decisionId + "': " + String.valueOf(error));
                    }
                    this.completeExceptionally(decisionRegistrationException);
                };
            }
        };

        return result;
    }

    private Decision getDecision(ScriptObjectMirror parsedDecision) {
        Gson gson = new Gson();
        String decisionString = gson.toJson(parsedDecision);
        return gson.fromJson(decisionString, Decision.class);
    }

    /**
     * Evaluates the decision with the given id, using the given execution context as input for the decision. The decision must have
     * been registered before with the decision service.
     * <p>
     * Even if the decision does not return a result because no rule matched, an instance of the {@link DecisionEvaluationResult} is returned.
     * Whether none, one, or more rules matched is reflected by the returned instance and can be queried using the corresponding methods on it.
     *
     * @param decisionId the DMN decision id of the decision
     * @param decisionInput the input for the decision, must be a Java bean or a {@link Map}
     * @param resultType a class instance of the decision result type
     * @param <T> the Java runtime type of the decision result
     * @return the decision result
     */
    public <T> DecisionEvaluationResult<T> evaluateDecision(String decisionId, Object decisionInput, Class<T> resultType) {
        long startTime = System.nanoTime();
        logger.info("Evaluating decision '{}'...", decisionId);
        if (logger.isDebugEnabled()) {
            logger.debug("Input for decision '{}': {}", decisionId, decisionInput);
        }
        ScriptObjectMirror parsedDmnDefinition = parsedDmnDefinitions.get(decisionId);
        if (parsedDmnDefinition == null) {
            throw new DecisionEvaluationException("Decision '" + decisionId + "' was not successfully registered before.");
        }
        ScriptObjectMirror parsedDecision = (ScriptObjectMirror) parsedDmnDefinition.get(decisionId);
        WrappedInputObject wrappedInput = InputObjectWrapper.INSTANCE.wrapInput(decisionInput);
        if (!wrappedInput.getType().equals("map")) {
            throw new DecisionEvaluationException("Decision input must be a POJO or of type java.util.Map");
        }
        ScriptObjectMirror scriptResult = (ScriptObjectMirror) dmnEvalJsWrapper.evaluateDecision(decisionId, parsedDmnDefinition, wrappedInput);

        DecisionEvaluationResult<T> result = buildDecisionResult(decisionId, parsedDecision, scriptResult, resultType);
        if (logger.isDebugEnabled()) {
            logger.debug("Result for decision '{}': {}", decisionId, decisionInput);
        }
        logger.info("Evaluating decision '{}' took {} msecs.", decisionId, (System.nanoTime() - startTime) / 1000000L);
        return result;
    }

    private <T> DecisionEvaluationResult<T> buildDecisionResult(String decisionId, ScriptObjectMirror parsedDecision, ScriptObjectMirror scriptResult,
                                                                Class<T> resultType) {
        Gson gson = new Gson();
        DecisionTable decisionTable = getDecision(parsedDecision).getDecisionTable();
        DecisionEvaluationResult<T> decisionEvaluationResult = new DecisionEvaluationResult<>(decisionId);
        if (!scriptResult.isEmpty()) {
            if ((decisionTable.getHitPolicy() == HitPolicy.FIRST) || (decisionTable.getHitPolicy() == HitPolicy.UNIQUE)) {
                String resultString = gson.toJson(scriptResult);
                T result = gson.fromJson(resultString, resultType);
                decisionEvaluationResult.addResult(result);
            } else {
                List<Object> list = makeListFromPseudoMap(scriptResult);
                for (Object listElement: list) {
                    String listElementString = gson.toJson(listElement);
                    T result = gson.fromJson(listElementString, resultType);
                    decisionEvaluationResult.addResult(result);
                }
            }
        }
        return decisionEvaluationResult;
    }

    private List<Object> makeListFromPseudoMap(ScriptObjectMirror pseudoMap) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < pseudoMap.size(); ++i) {
            list.add(pseudoMap.get(String.valueOf(i)));
        }
        return list;
    }

    private Object runScript(String scriptFile) {
        try (InputStream jsFileStream = DecisionService.class.getClassLoader().getResourceAsStream(scriptFile)) {
            if (jsFileStream == null) {
                throw new IllegalArgumentException("Did not find script file " + scriptFile + " in classpath.");
            }
            return scriptEngine.eval(new InputStreamReader(jsFileStream));
        } catch (ScriptException e) {
            throw new DecisionServiceInstantiationException("Failed to run script file " + scriptFile, e);
        } catch (IOException e) {
            throw new DecisionServiceInstantiationException("Failed to load script file " + scriptFile, e);
        }
    }

}
