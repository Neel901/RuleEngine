package com.example.RuleEngine.service;

import com.example.RuleEngine.model.Node;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AppService {

    private int index; // Used to track the current position of the token during parsing
    private String[] tokens; // Tokenized input of the rule string

    // Function to create the AST from the rule string
    public Node createRule(String ruleString) {
        // Preprocess the rule string to ensure consistent spacing around parentheses and operators
        ruleString = ruleString.replaceAll("\\(", " ( ") // Add spaces around '('
                .replaceAll("\\)", " ) ") // Add spaces around ')'
                .replaceAll("(?i)AND", "AND") // Standardize 'AND' operator to upper case
                .replaceAll("(?i)OR", "OR");  // Standardize 'OR' operator to upper case

        tokens = ruleString.trim().split("\\s+"); // Tokenize the rule string based on whitespace
        index = 0; // Start from the first token
        return parseExpression(); // Begin parsing the rule string into an AST
    }

    // Parse expressions: expressions are chains of terms combined with OR operators
    private Node parseExpression() {
        Node node = parseTerm(); // Start by parsing the first term
        // If the next token is 'OR', we continue building the tree
        while (index < tokens.length && tokens[index].equals("OR")) {
            String operator = tokens[index++]; // Consume 'OR'
            Node right = parseTerm(); // Parse the next term
            node = new Node("operator", operator, node, right); // Create a new node for the 'OR' operation
        }
        return node; // Return the root of the parsed expression
    }

    // Parse terms: terms are chains of factors combined with AND operators
    private Node parseTerm() {
        Node node = parseFactor(); // Parse the first factor
        // If the next token is 'AND', we continue building the tree
        while (index < tokens.length && tokens[index].equals("AND")) {
            String operator = tokens[index++]; // Consume 'AND'
            Node right = parseFactor(); // Parse the next factor
            node = new Node("operator", operator, node, right); // Create a new node for the 'AND' operation
        }
        return node; // Return the root of the parsed term
    }

    // Parse factors: factors are either parenthesized expressions or simple conditions
    private Node parseFactor() {
        // If the current token is '(', we need to parse an inner expression
        if (tokens[index].equals("(")) {
            index++; // Skip '('
            Node node = parseExpression(); // Parse the inner expression
            index++; // Skip ')'
            return node; // Return the parsed inner expression
        } else {
            // Otherwise, it's a condition (e.g., age > 30)
            return parseCondition();
        }
    }

    // Parse a single condition (e.g., 'age > 30')
    private Node parseCondition() {
        StringBuilder condition = new StringBuilder();
        // Accumulate tokens until we encounter 'AND', 'OR', or a closing parenthesis
        while (index < tokens.length && !tokens[index].equals("AND") && !tokens[index].equals("OR") && !tokens[index].equals(")") && !tokens[index].equals("(")) {
            condition.append(tokens[index++]).append(" "); // Append each token to form the condition
        }
        return new Node("operand", condition.toString().trim()); // Create an operand node for the condition
    }

    // Function to combine two rules using an operator (e.g., combine rule1 AND rule2)
    public Node combineRules(Node rule1, Node rule2, String operator) {
        // Create a new operator node with the two rules as children
        return new Node("operator", operator.toUpperCase(), rule1, rule2);
    }

    // Function to evaluate the AST against user data
    public boolean evaluateRule(Node node, Map<String, Object> data) {
        // If the current node is an operator (AND/OR)
        if (node.getType().equals("operator")) {
            // Recursively evaluate the left and right subtrees
            boolean leftEval = evaluateRule(node.getLeft(), data);
            boolean rightEval = evaluateRule(node.getRight(), data);

            // Apply the AND or OR operation on the results of the left and right evaluations
            if (node.getValue().equals("AND")) {
                return leftEval && rightEval; // Both must be true for AND
            } else if (node.getValue().equals("OR")) {
                return leftEval || rightEval; // At least one must be true for OR
            }
        } else {
            // If it's an operand, evaluate the condition
            return evaluateCondition(node.getValue(), data);
        }
        return false; // Default case, should never reach here if rule is well-formed
    }

    // Function to evaluate a single condition against the provided user data
    private boolean evaluateCondition(String condition, Map<String, Object> data) {
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
