/*
 *  ©2018 HBT Hamburger Berater Team GmbH
 *  All Rights Reserved.
 */
package de.hbt.dmn_eval_java;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a decision, identified by its {@linkplain #decisionId decision id}. A decision is backed by a decision table.
 * Evaluating a decision may require the evaluation other 'required' decisions first.
 */
public class Decision {

    private String decisionId;
    private DecisionTable decisionTable;
    private Map<String, String> requiredDecisions;

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public DecisionTable getDecisionTable() {
        return decisionTable;
    }

    /**
     * Returns the decisions whose evaluation is required before this decision can be evaluation.
     * The required decisions are referenced by their decision id.
     * @return the list of required decisions
     */
    public List<String> getRequiredDecisions() {
        if (requiredDecisions == null) {
            return Collections.emptyList();
        }
        List<String> flattenedOutputNames = new ArrayList<>(requiredDecisions.size());
        for (int i = 0; i < requiredDecisions.size(); ++i) {
            flattenedOutputNames.add(requiredDecisions.get(String.valueOf(i)));
        }
        return flattenedOutputNames;
    }

}
