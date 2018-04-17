/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

/**
 * Instances of this class are thrown when the {@link DecisionService} failed to initialize properly.
 */
public class DecisionServiceInstantiationException extends RuntimeException {

    static final long serialVersionUID = 0L;

    /**
     * Creates a decision service instantiation exception with the given message.
     * @param msg the exception message
     */
    public DecisionServiceInstantiationException(String msg) {
        super(msg);
    }

    /**
     * Creates a decision service instantiation with the given message and root cause.
     * @param msg the exception message
     * @param cause the root cause
     */
    public DecisionServiceInstantiationException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
