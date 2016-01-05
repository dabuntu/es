package com.eventshop.eventshoplinux.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by nandhiniv on 8/26/15.
 */
@JsonIgnoreProperties
public class RuleOperator {

    private int id;
    private String dataType;
    private String operator;
    private int enabled;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public int getEnabled() {
        return enabled;
    }

    public void setEnabled(int enabled) {
        this.enabled = enabled;
    }
}
