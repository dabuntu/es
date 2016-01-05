package com.eventshop.eventshoplinux.ruleEngine;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by nandhiniv on 8/25/15.
 */
@JsonIgnoreProperties
public class Rule{


    String ruleID;
    String ruleName;
    String dataField;
    String ruleOperator;
    String ruleParameters;

    public Rule(){

    }

    public Rule(String dataField, String ruleOperator, String ruleParameters) {
        this.dataField = dataField;
        this.ruleOperator = ruleOperator;
        this.ruleParameters = ruleParameters;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "dataField='" + dataField + '\'' +
                ", ruleOperator='" + ruleOperator + '\'' +
                ", ruleParameters='" + ruleParameters + '\'' +
                '}';
    }
    public String getRuleID() {
        return ruleID;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDataField() {
        return dataField;
    }

    public void setDataField(String dataField) {
        this.dataField = dataField;
    }

    public String getRuleOperator() {
        return ruleOperator;
    }

    public void setRuleOperator(String ruleOperator) {
        this.ruleOperator = ruleOperator;
    }

    public String getRuleParameters() {
        return ruleParameters;
    }

    public void setRuleParameters(String ruleParameters) {
        this.ruleParameters = ruleParameters;
    }
}

