package com.example.rule.engine.repository;

import com.example.rule.engine.model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RuleRepository extends JpaRepository<Rule,Integer> {
}
