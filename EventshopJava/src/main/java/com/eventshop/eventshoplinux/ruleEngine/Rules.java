package com.eventshop.eventshoplinux.ruleEngine;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by nandhiniv on 8/25/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Rules {

    int ruleID;
    String ruleName;
    List<Rule> rules;
    String source;
    String extractFields;
    int userId;

    public Rules() {

    }

    @Override
    public String toString() {
        return "Rules{" +
                "ruleID=" + ruleID +
                ", ruleName='" + ruleName + '\'' +
                ", rules=" + rules +
                ", source='" + source + '\'' +
                ", extractFields='" + extractFields + '\'' +
                ", userId=" + userId +
                '}';
    }

    public Rules(List<Rule> rules, String source, String extractFields) {
        this.rules = rules;
        this.source = source;
        this.extractFields = extractFields;
    }



    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getExtractFields() {
        return extractFields;
    }

    public void setExtractFields(String extractFields) {
        this.extractFields = extractFields;
    }

    public int getRuleID() {
        return ruleID;
    }

    public void setRuleID(int ruleID) {
        this.ruleID = ruleID;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

}
