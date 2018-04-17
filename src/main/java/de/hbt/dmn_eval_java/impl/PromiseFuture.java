/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java.impl;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 * A completable future to represent the outcome of a Javascript Promise.
 * Implementing subclasses must define the logic that is to be executed when the promise is resolved,
 * as well as when it is rejected.
 *
 * @param <T> the run-time type of the promise's result
 */
public abstract class PromiseFuture<T> extends CompletableFuture<T> {

    private final ScriptObjectMirror promise;
    private final ScriptObjectMirror nashornEventLoop;

    private boolean promiseHandled;

    /**
     * Creates the future from the given Javascript promise. The given reference to the event loop of Nashorn
     * is required to trigger the asynchronous processing.
     * @param promise the promise to be awaited
     * @param nashornEventLoop the event loop of Nashorn
     */
    public PromiseFuture(ScriptObjectMirror promise, ScriptObjectMirror nashornEventLoop) {
        this.promise = promise;
        this.nashornEventLoop = nashornEventLoop;
        this.promiseHandled = false;
    }

    private void handlePromise() {
        if (!promiseHandled) {
            promise.callMember("then", resolve(), reject());

            nashornEventLoop.callMember("process");
            promiseHandled = true;
        }
    }

    public T get() throws InterruptedException, ExecutionException {
        handlePromise();
        return super.get();
    }

    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        handlePromise();
        return super.get(timeout, unit);
    }

    /**
     * Creates and returns a consumer that consumes the event that the promise is resolved successfully. The promise's result
     * is the single input argument for the consumer.
     * @return the consumer for resolved promises
     */
    protected abstract Consumer<?> resolve();

    /**
     * Creates and returns a consumer that consumes the event that the promise is rejected. The error object is the single
     * input argument for the consumer.
     * @return the consumer for rejected promises
     */
    protected abstract Consumer<?> reject();

}
