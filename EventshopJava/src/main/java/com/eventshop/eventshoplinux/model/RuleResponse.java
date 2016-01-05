package com.eventshop.eventshoplinux.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Map;

/**
 * Created by nandhiniv on 8/27/15.
 */
@JsonIgnoreProperties
public class RuleResponse {

    String dataSource;
    Map<String, Object> syntaxAndOperators;

    public RuleResponse(String dataSource, Map<String, Object> syntaxAndOperators) {
        this.dataSource = dataSource;
        this.syntaxAndOperators = syntaxAndOperators;
    }

    @Override
    public String toString() {
        return "{" +
                "dataSource='" + dataSource + '\'' +
                ", syntaxAndOperators=" + syntaxAndOperators +
                '}';
    }
}
