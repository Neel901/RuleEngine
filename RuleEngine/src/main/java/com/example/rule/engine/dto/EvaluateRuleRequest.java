package com.example.rule.engine.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EvaluateRuleRequest {

    @NotEmpty(message = "Rule must not be empty")
    private String rule;

    @NotEmpty(message = "Data must not be empty")
    private Map<String, Object> data;
}
