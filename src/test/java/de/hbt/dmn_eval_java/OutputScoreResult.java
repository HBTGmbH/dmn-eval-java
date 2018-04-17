package de.hbt.dmn_eval_java;

import java.util.HashMap;
import java.util.Map;

public class OutputScoreResult {

    private Map<String, Integer> output = new HashMap<>();

    public Integer getScore() {
        return this.output.get("score");
    }

    public void setScore(Integer score) {
        this.output.put("score", score);
    }
}
