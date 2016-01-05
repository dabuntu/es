package com.eventshop.eventshoplinux.domain.rule;

/**
 * Created by aravindh on 9/1/15.
 */
public class DSRuleElement {
    String dataSourceID;
    int ruleID;
    String dataSourceName;
    String ruleName;


    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDataSourceID() {
        return dataSourceID;
    }

    public void setDataSourceID(String dataSourceID) {
        this.dataSourceID = dataSourceID;
    }

    public int getRuleID() {
        return ruleID;
    }

    public void setRuleID(int ruleID) {
        this.ruleID = ruleID;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

}
