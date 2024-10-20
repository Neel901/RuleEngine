CREATE DATABASE `rule_engine`;

use database  `rule_engine`;

CREATE TABLE `rules` (
  `rule_id` int NOT NULL AUTO_INCREMENT,
  `rule_string` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`rule_id`),
  UNIQUE KEY `u1` (`rule_string`)
);
