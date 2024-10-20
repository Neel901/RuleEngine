package com.example.rule.engine.controller;

import com.example.rule.engine.dto.CombineRuleRequest;
import com.example.rule.engine.dto.CreateRuleRequest;
import com.example.rule.engine.dto.EvaluateRuleRequest;
import com.example.rule.engine.dto.RuleEngineResponse;
import com.example.rule.engine.exception.RuleEngineException;
import com.example.rule.engine.dto.Node;
import com.example.rule.engine.model.Rule;
import com.example.rule.engine.repository.RuleRepository;
import com.example.rule.engine.service.impl.AppServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RuleEngineResponse> createRule(@RequestBody CreateRuleRequest request) {
        String ruleString = request.getRuleString();
        try {
            ruleService.saveRule(ruleString);
        } catch (RuleEngineException e) {
            return ResponseEntity.badRequest()
                    .body(RuleEngineResponse.builder().message(e.getMessage()).build());
        }
        return ResponseEntity.ok(ruleService.createRule(ruleString));
    }

    @PostMapping("/combine")
    public Node combineRules(@RequestBody CombineRuleRequest request) {
        String ruleString1 = request.getRule1();
        String ruleString2 = request.getRule2();
        String operator = request.getOperator();

        RuleEngineResponse rule1 = ruleService.createRule(ruleString1);
        RuleEngineResponse rule2 = ruleService.createRule(ruleString2);

        return ruleService.combineRules(rule1.getNode(), rule2.getNode(), operator).getNode();
    }

    @PostMapping("/evaluate")
    public RuleEngineResponse evaluateRule(@RequestBody EvaluateRuleRequest request) {
        String ruleString = request.getRule();
        Map<String, Object> data = request.getData();

        RuleEngineResponse rule = ruleService.createRule(ruleString);
        boolean result = ruleService.evaluateRule(rule.getNode(), data);

        return RuleEngineResponse.builder().result(result).build();
    }
}
