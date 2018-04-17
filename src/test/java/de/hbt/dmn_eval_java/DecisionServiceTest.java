package de.hbt.dmn_eval_java;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class DecisionServiceTest {

    @Test
    public void testSingleResult() throws InterruptedException, ExecutionException, TimeoutException {
        DecisionService decisionService = new DecisionService();
        Future<Decision> decisionFuture = decisionService.registerDecision("decisionPackagingMatchingScore", "src/test/resources/packaging_matching_score.dmn");
        Decision decision = decisionFuture.get(10, TimeUnit.SECONDS);
        assertEquals("decisionPackagingMatchingScore", decision.getDecisionId());

        Map<String, Object> input = new HashMap<>();
        ContractLot salesLot = new ContractLot();
        ContractLot purchaseLot = new ContractLot();
        salesLot.setPackaging("Bags");
        purchaseLot.setPackaging("Bulk");
        input.put("purchaseLot", purchaseLot);
        input.put("salesLot", salesLot);
        DecisionEvaluationResult<MatchingScoreResult> output = decisionService.evaluateDecision("decisionPackagingMatchingScore", input, MatchingScoreResult.class);
        assertEquals(0, output.getSingleResult().getMatchingScore().intValue());

        salesLot.setPackaging("Bulk");
        output = decisionService.evaluateDecision("decisionPackagingMatchingScore", input, MatchingScoreResult.class);
        assertEquals(30, output.getSingleResult().getMatchingScore().intValue());
    }

    @Test
    public void testMultipleResults() throws InterruptedException, ExecutionException, TimeoutException {
        DecisionService decisionService = new DecisionService();
        Future<Decision> decisionFuture = decisionService.registerDecision("decision", "src/test/resources/test-collect.dmn");
        Decision decision = decisionFuture.get(10, TimeUnit.SECONDS);
        assertEquals("decision", decision.getDecisionId());

        Map<String, Object> input = new HashMap<>();
        Map<String, Object> category = new HashMap<>();
        category.put("category", "A");
        input.put("input", category);
        DecisionEvaluationResult<MessageAndOutputValueResult> output = decisionService.evaluateDecision("decision", input, MessageAndOutputValueResult.class);
        assertEquals(4, output.getResultList().size());
        assertEquals("Message 1", output.getResultList().get(0).getMessage());
        assertEquals("Message 3", output.getResultList().get(1).getMessage());
        assertEquals("Message 4", output.getResultList().get(2).getMessage());
        assertEquals("Message 5", output.getResultList().get(3).getMessage());
        assertEquals("Value 1", output.getResultList().get(0).getOutput().getProperty());
        assertNull(output.getResultList().get(1).getOutput().getProperty());
        assertEquals("Value 4", output.getResultList().get(2).getOutput().getProperty());
        assertEquals("Value 5", output.getResultList().get(3).getOutput().getProperty());
    }

    @Test
    public void testDependenDecision() throws InterruptedException, ExecutionException, TimeoutException {
        DecisionService decisionService = new DecisionService();
        Future<Decision> decisionFuture = decisionService.registerDecision("decisionPrimary", "src/test/resources/test-collect-drg.dmn");
        Decision decision = decisionFuture.get(10, TimeUnit.SECONDS);
        assertEquals("decisionPrimary", decision.getDecisionId());

        Map<String, Object> input = new HashMap<>();
        Map<String, Object> category = new HashMap<>();
        category.put("category", "A");
        input.put("input", category);

        DecisionEvaluationResult<OutputScoreResult> output = decisionService.evaluateDecision("decisionPrimary", input, OutputScoreResult.class);
        assertEquals(Integer.valueOf(50), output.getSingleResult().getScore());

    }

    @Test
    public void registerInvalidDmn() throws TimeoutException, InterruptedException {
        DecisionService decisionService = new DecisionService();
        Future<Decision> decision = decisionService.registerDecision("decision", "src/test/resources/test-collect-invalid.dmn");
        try {
            decision.get(10, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            assertNotNull(e.getCause());
            assertEquals(DecisionRegistrationException.class, e.getCause().getClass());
        }
    }

    @Test
    public void testFunctions() throws InterruptedException, ExecutionException, TimeoutException {
        DecisionService decisionService = new DecisionService();
        Future<Decision> decisionFuture = decisionService.registerDecision("decision", "src/test/resources/test-functions.dmn");
        Decision decision = decisionFuture.get(1, TimeUnit.MINUTES);
        assertEquals("decision", decision.getDecisionId());

        // register the decision some more times to get a rough idea of the asymptotic processing time
        Future<Decision> decisionFuture2 = decisionService.registerDecision("decision2", "src/test/resources/test-functions-decision-2.dmn");
        Future<Decision> decisionFuture3 = decisionService.registerDecision("decision3", "src/test/resources/test-functions-decision-3.dmn");
        Future<Decision> decisionFuture4 = decisionService.registerDecision("decision4", "src/test/resources/test-functions-decision-4.dmn");
        Future<Decision> decisionFuture5 = decisionService.registerDecision("decision5", "src/test/resources/test-functions-decision-5.dmn");
        assertEquals("decision2", decisionFuture2.get(1, TimeUnit.MINUTES).getDecisionId());
        assertEquals("decision3", decisionFuture3.get(1, TimeUnit.MINUTES).getDecisionId());
        assertEquals("decision4", decisionFuture4.get(1, TimeUnit.MINUTES).getDecisionId());
        assertEquals("decision5", decisionFuture5.get(1, TimeUnit.MINUTES).getDecisionId());

        Map<String, Object> input = new HashMap<>();
        input.put("input1", 1);
        input.put("input2", "2");
        input.put("input3", true);
        BiFunction<Integer, String, Long> func1 = (Integer intValue, String stringValue) -> intValue != null && intValue == 1 && "2".equals(stringValue)? 42L: 0;
        Function<String, Long> func2 = (String value) -> value != null && value.equals("2")? 42L: -1;
        Function<Boolean, String> func3 = (Boolean value) -> value? "ok": "nok";
        input.put("func1", func1);
        input.put("func2", func2);
        input.put("func3", func3);

        DecisionEvaluationResult<OutputResult> output = decisionService.evaluateDecision("decision", input, OutputResult.class);
        OutputResult outputResult = output.getSingleResult();
        assertEquals("ok", outputResult.getOutput());

        // evaluate some more times to get a rought idea of the asymptotic evaluation duration
        decisionService.evaluateDecision("decision", input, OutputResult.class);
        decisionService.evaluateDecision("decision", input, OutputResult.class);
        decisionService.evaluateDecision("decision", input, OutputResult.class);
        decisionService.evaluateDecision("decision", input, OutputResult.class);
        decisionService.evaluateDecision("decision", input, OutputResult.class);
    }

}
