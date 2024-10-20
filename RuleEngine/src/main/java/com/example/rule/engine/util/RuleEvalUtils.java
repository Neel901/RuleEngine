package com.example.rule.engine.util;

import com.example.rule.engine.dto.Node;

import java.util.Map;

public class RuleEvalUtils {

    public static boolean isValidRuleString(String ruleString) {
        // Return true if comparison operators are present
        return ruleString.contains(">") || ruleString.contains("<") ||
                ruleString.contains("=") || ruleString.contains(">=") ||
                ruleString.contains("<=") || ruleString.contains("!=");
    }

    // Function to evaluate a single condition against the provided user data
    public static boolean evaluateCondition(String condition, Map<String, Object> data) {
        // Preprocess operators in the condition for easy parsing
        condition = condition.replaceAll("==", "=")  // Replace '==' with '='
                .replaceAll("!=", "≠")  // Replace '!=' with '≠'
                .replaceAll(">=", "≥")  // Replace '>=' with '≥'
                .replaceAll("<=", "≤"); // Replace '<=' with '≤'

        // Define all possible operators
        String[] operators = {"≥", "≤", ">", "<", "=", "≠"};
        String operator = null;

        // Identify the operator in the condition
        for (String op : operators) {
            if (condition.contains(op)) {
                operator = op;
                break;
            }
        }

        // If no operator found, the condition is invalid
        if (operator == null) {
            throw new IllegalArgumentException("Invalid condition: " + condition);
        }

        // Split the condition into attribute and value based on the operator
        String[] parts = condition.split("\\" + operator);
        String attribute = parts[0].trim(); // Attribute part (e.g., age)
        String valueStr = parts[1].trim().replace("'", ""); // Value part (e.g., 30, 'Sales')

        Object attributeValue = data.get(attribute); // Get the value from user data

        // If the attribute is not present in the data, the condition cannot be evaluated
        if (attributeValue == null) {
            return false;
        }

        // Handle numeric and string comparisons
        try {
            // If the attribute value is numeric, compare numerically
            if (attributeValue instanceof Number) {
                double attrValueNum = Double.parseDouble(attributeValue.toString());
                double valueNum = Double.parseDouble(valueStr);

                // Evaluate the numeric condition based on the operator
                switch (operator) {
                    case ">":
                        return attrValueNum > valueNum;
                    case "<":
                        return attrValueNum < valueNum;
                    case "≥":
                        return attrValueNum >= valueNum;
                    case "≤":
                        return attrValueNum <= valueNum;
                    case "=":
                        return attrValueNum == valueNum;
                    case "≠":
                        return attrValueNum != valueNum;
                }
            } else {
                // If the attribute is a string, compare strings
                int compareResult = attributeValue.toString().compareTo(valueStr);

                // Evaluate the string condition based on the operator
                switch (operator) {
                    case "=":
                        return attributeValue.toString().equals(valueStr);
                    case "≠":
                        return !attributeValue.toString().equals(valueStr);
                    case ">":
                        return compareResult > 0;
                    case "<":
                        return compareResult < 0;
                    case "≥":
                        return compareResult >= 0;
                    case "≤":
                        return compareResult <= 0;
                }
            }
        } catch (NumberFormatException e) {
            // Handle cases where attributeValue is a string but valueStr is numeric or vice versa
            return false;
        }

        return false; // Default return false if no valid comparison could be made
    }
}
