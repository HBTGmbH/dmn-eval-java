/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java.impl;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

/**
 * Java interface to wrap functions from the Javascript dmn-eval-js library.
 */
public interface DmnEvalJsWrapper {

    /**
     * Evaluates the decision with the given decision id. The DMN definition of the decision (and its required decision)
     * must be given, as well as the decision input.
     * @param decisionId the id of the decision that shall be evaluated
     * @param parsedDecision the DMN definition of the decision
     * @param decisionInput the input as a {@link WrappedInputObject}
     * @return the decision result
     */
    Object evaluateDecision(String decisionId, ScriptObjectMirror parsedDecision, Object decisionInput);

    /**
     * Parses the DMN definition of a decision from a string with the DMN XML content.
     * @param dmnXml the DMN XML content
     * @return the parsed DMN definition
     */
    ScriptObjectMirror parseDmnXml(String dmnXml);

}
