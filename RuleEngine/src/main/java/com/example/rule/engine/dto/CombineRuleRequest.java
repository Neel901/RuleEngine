package com.example.rule.engine.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CombineRuleRequest {

    @NotEmpty(message = "Rule1 mandatory")
    private String rule1;

    @NotEmpty(message = "Rule2 mandatory")
    private String rule2;

    @NotEmpty(message = "Operator mandatory")
    private String operator;
}
