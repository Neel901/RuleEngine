package com.example.RuleEngine.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
//@AllArgsConstructor
@NoArgsConstructor
public class Node {
    private String type; // "operator" or "operand"
    private String value; // Operator ("AND", "OR") or condition (e.g., "age > 30")
    private Node left;
    private Node right;

    // Constructors
    public Node(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public Node(String type, String value, Node left, Node right) {
        this.type = type;
        this.value = value;
        this.left = left;
        this.right = right;
    }
}
