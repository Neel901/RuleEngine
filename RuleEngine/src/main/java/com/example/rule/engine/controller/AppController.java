package com.example.rule.engine.controller;

import com.example.rule.engine.dto.RuleEngineResponse;
import com.example.rule.engine.exception.RuleEngineException;
import com.example.rule.engine.dto.Node;
import com.example.rule.engine.model.Rule;
import com.example.rule.engine.repository.RuleRepository;
import com.example.rule.engine.service.impl.AppServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin
public class AppController {

    @Autowired
    private AppServiceImpl ruleService;

    @Autowired
    private RuleRepository ruleRepository;

    // API to get all rules
    @GetMapping
    public List<Rule> getAllRules() {
        return ruleRepository.findAll(); // Fetch all rules from the database
    }

    @PostMapping("/create")
    public RuleEngineResponse createRule(@RequestBody Map<String, String> request) {
        String ruleString = request.get("ruleString");
        try {
            ruleService.saveRule(ruleString);
        } catch (RuleEngineException e) {
            return RuleEngineResponse.builder().message(e.getMessage()).build();
        }
        return ruleService.createRule(ruleString);
    }

    @PostMapping("/combine")
    public Node combineRules(@RequestBody Map<String, Object> request) {
        String ruleString1 = (String) request.get("rule1");
        String ruleString2 = (String) request.get("rule2");
        String operator = (String) request.get("operator");

        RuleEngineResponse rule1 = ruleService.createRule(ruleString1);
        RuleEngineResponse rule2 = ruleService.createRule(ruleString2);

        return ruleService.combineRules(rule1.getNode(), rule2.getNode(), operator).getNode();
    }

    @PostMapping("/evaluate")
    public Map<String, Boolean> evaluateRule(@RequestBody Map<String, Object> request) {
        String ruleString = (String) request.get("rule");
        Map<String, Object> data = (Map<String, Object>) request.get("data");

        RuleEngineResponse rule = ruleService.createRule(ruleString);
        boolean result = ruleService.evaluateRule(rule.getNode(), data);

        return Map.of("result", result);
    }
}
