package com.example.rule.engine.service.impl;

import com.example.rule.engine.dto.RuleEngineResponse;
import com.example.rule.engine.exception.RuleEngineException;
import com.example.rule.engine.dto.Node;
import com.example.rule.engine.model.Rule;
import com.example.rule.engine.repository.RuleRepository;
import com.example.rule.engine.service.AppService;
import com.example.rule.engine.util.RuleEvalUtils;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import org.slf4j.Logger;

@Service
public class AppServiceImpl implements AppService {

    Logger log = LoggerFactory.getLogger(AppServiceImpl.class);

    @Autowired
    private RuleRepository ruleRepository;

    private int index; // Used to track the current position of the token during parsing
    private String[] tokens; // Tokenized input of the rule string

    @Override
    public void saveRule(String ruleString) throws RuleEngineException {
        // Check if the rule string is valid (contains at least one operator)
        if (!RuleEvalUtils.isValidRuleString(ruleString)) {
            throw new RuleEngineException("Invalid rule string. Please try again.");
        }

        // Save the rule string to the database
        Rule rule = new Rule();
        rule.setRule_string(ruleString);
        try {
            ruleRepository.save(rule); // Save the rule to the repository
        } catch (DataIntegrityViolationException ex) {
            log.error("Duplicate rule- [{}]", rule, ex);
            throw new RuleEngineException("Duplicate rule. Please try again");
        } catch (DataAccessException ex) {
            log.error("Invalid rule - [{}]", rule, ex);
            throw new RuleEngineException("Invalid rule. Please try again");
        }
    }

    // Function to create the AST from the rule string
    @Override
    public RuleEngineResponse createRule(String ruleString) {
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
    private RuleEngineResponse parseExpression() {
        Node node = parseTerm(); // Start by parsing the first term
        // If the next token is 'OR', we continue building the tree
        while (index < tokens.length && tokens[index].equals("OR")) {
            String operator = tokens[index++]; // Consume 'OR'
            Node right = parseTerm(); // Parse the next term
            node = new Node("operator", operator, node, right); // Create a new node for the 'OR' operation
        }
        return RuleEngineResponse.builder()
                .node(node)
                .build(); // Return the root of the parsed expression
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
            Node node = parseExpression().getNode(); // Parse the inner expression
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
        while (index < tokens.length && !tokens[index].equals("AND")
                && !tokens[index].equals("OR") && !tokens[index].equals(")")
                && !tokens[index].equals("(")) {
            condition.append(tokens[index++]).append(" "); // Append each token to form the condition
        }
        return new Node("operand", condition.toString().trim()); // Create an operand node for the condition
    }

    // Function to combine two rules using an operator (e.g., combine rule1 AND rule2)
    @Override
    public RuleEngineResponse combineRules(Node rule1, Node rule2, String operator) {
        // Create a new operator node with the two rules as children
        return RuleEngineResponse.builder()
                .node(new Node("operator", operator.toUpperCase(), rule1, rule2))
                .build();
    }

    // Function to evaluate the AST against user data
    @Override
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
            return RuleEvalUtils.evaluateCondition(node.getValue(), data);
        }
        return false; // Default case, should never reach here if rule is well-formed
    }
}
