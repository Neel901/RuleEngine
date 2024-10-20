package com.example.rule.engine.service;

import com.example.rule.engine.dto.Node;
import com.example.rule.engine.dto.RuleEngineResponse;
import com.example.rule.engine.exception.RuleEngineException;

import java.util.Map;

public interface AppService {
    void saveRule(String ruleString) throws RuleEngineException;

    // Function to create the AST from the rule string
    RuleEngineResponse createRule(String ruleString);

    // Function to combine two rules using an operator (e.g., combine rule1 AND rule2)
    RuleEngineResponse combineRules(Node rule1, Node rule2, String operator);

    // Function to evaluate the AST against user data
    boolean evaluateRule(Node node, Map<String, Object> data);
}
