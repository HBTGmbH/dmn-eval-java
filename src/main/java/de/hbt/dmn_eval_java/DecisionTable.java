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
 * Represents a decision table, which is the core of a decision. This class is a simplified model of how DMN
 * defines a decision table. We store the hit policy, the input expressions (one for each input column)
 * and the output names (one for each output column), but for example not the rules (one for each row).
 */
public class DecisionTable {

    private HitPolicy hitPolicy;
    private Map<String, String> inputExpressions;
    private Map<String, String> outputNames;

    public HitPolicy getHitPolicy() {
        return hitPolicy;
    }

    public void setHitPolicy(HitPolicy hitPolicy) {
        this.hitPolicy = hitPolicy;
    }

    public List<String> getInputExpressions() {
        return toList(inputExpressions);
    }

    public List<String> getOutputNames() {
        return toList(outputNames);
    }

    private static List<String> toList(Map<String, String> map) {
        if (map == null) {
            return Collections.emptyList();
        }
        List<String> flattenedValies = new ArrayList<>(map.size());
        for (int i = 0; i < map.size(); ++i) {
            flattenedValies.add(map.get(String.valueOf(i)));
        }
        return flattenedValies;
    }

}
