/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of evaluating a decision. Depending on the {@link HitPolicy} of the decision,
 * the result is either a list of values or a single value.
 * @param <T> the run-time type of the result value
 */
public class DecisionEvaluationResult<T> {

    private String decisionId;
    private List<T> resultList = new ArrayList<>();

    public DecisionEvaluationResult(String decisionId) {
        this.decisionId = decisionId;
    }

    /**
     * Returns the list of all results of evaluating the decision. If the hit policy of the decision is 'UNIQUE'
     * or 'FIRST', there is at most one result, otherwise, there may be more than one result. In any case,
     * the list may be empty, if no rule matched.
     * @return the list of all results
     */
    public List<T> getResultList() {
        return resultList;
    }

    /**
     * Returns the single result of evaluating the decision, for decisions with hit policy 'UNIQUE' or 'FIRST'.
     * If there are multiple results, an exception is thrown.
     * @return the single result
     */
    public T getSingleResult() {
        if (resultList.isEmpty()) {
            return null;
        }
        if (resultList.size() > 1) {
            throw new DecisionEvaluationException("More than one result was evaluated for decision '" + decisionId + "'");
        }
        return resultList.get(0);
    }

    /**
     * Returns the first result of valuating the decision. If there are multiple results, the first one is returned,
     * which is that of the first matching rule if multiple rules matched.
     * @return the first result
     */
    public T getFirstResult() {
        if (resultList.isEmpty()) {
            return null;
        }
        return resultList.get(0);
    }

    public void addResult(T result) {
        this.resultList.add(result);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (T result: resultList) {
            sb.append("Result for decision '").append(decisionId).append("': ").append(result).append(System.lineSeparator());
        }
        return sb.toString();
    }

}
