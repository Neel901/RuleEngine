package com.example.RuleEngine.controller;

import com.example.RuleEngine.model.Node;
import com.example.RuleEngine.service.AppService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin
public class AppController {

    @Autowired
    private AppService ruleService;

    @PostMapping("/create")
    public Node createRule(@RequestBody Map<String, String> request) {
        String ruleString = request.get("ruleString");
        return ruleService.createRule(ruleString);
    }

    @PostMapping("/combine")
    public Node combineRules(@RequestBody Map<String, Object> request) {
        String ruleString1 = (String) request.get("rule1");
        String ruleString2 = (String) request.get("rule2");
        String operator = (String) request.get("operator");

        Node rule1 = ruleService.createRule(ruleString1);
        Node rule2 = ruleService.createRule(ruleString2);

        return ruleService.combineRules(rule1, rule2, operator);
    }

    @PostMapping("/evaluate")
    public Map<String, Boolean> evaluateRule(@RequestBody Map<String, Object> request) {
        String ruleString = (String) request.get("rule");
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        Node rule = ruleService.createRule(ruleString);
        boolean result = ruleService.evaluateRule(rule, data);

        return Map.of("result", result);
    }
}
