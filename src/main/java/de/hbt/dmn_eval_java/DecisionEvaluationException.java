/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

/**
 * Instances of this class are thrown when the evaluation of a decision by the {@link DecisionService} failed.
 */
public class DecisionEvaluationException extends RuntimeException {

    static final long serialVersionUID = 0L;

    /**
     * Creates a decision evaluation exception with the given message.
     * @param msg the exception message
     */
    public DecisionEvaluationException(String msg) {
        super(msg);
    }

    /**
     * Creates a decision evaluation exception with the given message and root cause.
     * @param msg the exception message
     * @param cause the root cause
     */
    public DecisionEvaluationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
