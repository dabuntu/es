package com.eventshop.eventshoplinux.ruleEngine;

import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.model.RuleOperator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nandhiniv on 8/26/15.
 */
public class EventshopUtils {

    private final static Logger LOGGER = LoggerFactory.getLogger(EventshopUtils.class);
    public static void main(String[] args) {

//        String s ="\"timestamp\":\"DATETIME\",\n" +
//                "\"theme\":\"STRING\",\n" +
//                "\"value\":\"NUMBER\",\n" +
//                "\"location\":{\"lon\":\"NUMBER\",\"lat\":\"NUMBER\"}";

//        String s = "\"timestamp\":\"DATETIME\",\n" +
//                "\"theme\":\"STRING\",\n" +
//                "\"value\":\"NUMBER\",\n" +
//                "\"location\":{\"lon\":\"NUMBER\",\"lat\":\"NUMBER\",\"nest\":{\"lat\":\"NUMBER\",\"lon\":\"NUMBER\"}}";

        String s = "\"timestamp\":\"DATETIME\",\n" +
                "\"theme\":\"STRING\",\n" +
                "\"value\":\"NUMBER\",\n" +
                "\"location\":{\"lon\":\"NUMBER\",\"lat\":\"NUMBER\",\"altitue\":{\"height\":\"NUMBER\"}}";

        ConcurrentHashMap<String, Object> allSyntaxAndOperators = getSyntaxAndOperators(s);

    }

    public static ConcurrentHashMap<String, Object> getSyntaxAndOperators(String s) {
        ConcurrentHashMap<String, Object> finalResult = EventshopUtils.convertSyntaxToJson(s);
        ConcurrentHashMap<String, List<String>> operatorMap = new ConcurrentHashMap<>();

        List<RuleOperator> ruleOperatorList = new RuleDao().getAllEnabledRuleOperators();
        List<String> allOperators = new ArrayList<>();


        for (RuleOperator ruleOperator : ruleOperatorList) {
            allOperators.add(ruleOperator.getOperator());
            if (operatorMap.containsKey(ruleOperator.getDataType())) {
                List<String> opList = operatorMap.get(ruleOperator.getDataType());
                opList.add(ruleOperator.getOperator());
                operatorMap.replace(ruleOperator.getDataType(), opList);

            } else {
                final ArrayList<String> opList = new ArrayList<>();
                opList.add(ruleOperator.getOperator());
                operatorMap.put(ruleOperator.getDataType(), opList);
            }
        }


        for (ConcurrentHashMap.Entry<String, Object> entry : finalResult.entrySet()) {
            List<String> operators = new ArrayList<>();
            final String value = String.valueOf(entry.getValue()).toLowerCase();
            if (operatorMap.containsKey(value)) {
                operators = operatorMap.get(value);
            } else {
                operators.addAll(allOperators);
            }
            finalResult.replace(entry.getKey(), operators);
//            System.out.println(entry.getKey() + "-" + entry.getValue());
            LOGGER.debug(entry.getKey() + "-" + finalResult.get(entry.getKey()));
        }
        return finalResult;
    }

    public static ConcurrentHashMap<String, Object> checkContainsInner(ConcurrentHashMap<String, Object> map, Boolean hasInner) {
        for (ConcurrentHashMap.Entry<String, Object> entry : map.entrySet()) {
            if (String.valueOf(entry.getValue()).contains("{")) {
                hasInner = true;
                break;
            }
        }
        if (hasInner) {
            for (ConcurrentHashMap.Entry<String, Object> entry : map.entrySet()) {
                if (String.valueOf(entry.getValue()).contains("{")) {
                    final ConcurrentHashMap<String, Object> innerGsonAsMap = getGsonAsMap(String.valueOf(entry.getValue()));
                    for (ConcurrentHashMap.Entry<String, Object> innerEntry : innerGsonAsMap.entrySet()) {
                        final String key = innerEntry.getKey();
                        final Object value = innerEntry.getValue();
                        innerGsonAsMap.remove(key);
                        innerGsonAsMap.put(entry.getKey() + "." + key, value);
                    }
                    map.putAll(innerGsonAsMap);
                    map.replace(entry.getKey(), "OBJECT");
                }
            }
            checkContainsInner(map, false);
        }
        return map;
    }

    public static ConcurrentHashMap<String, Object> convertSyntaxToJson(String syntax) {
        if (!syntax.startsWith("{")) {
            syntax = "{" + syntax + "}";
        }
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
        map = getGsonAsMap(syntax);

        ConcurrentHashMap<String, Object> finalResult = new ConcurrentHashMap<String, Object>();
        finalResult = checkContainsInner(map, false);

        return finalResult;
    }

    public static ConcurrentHashMap getGsonAsMap(String string) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(Object.class, new NaturalDeserializer());
        Gson gson = gsonBuilder.create();
        ConcurrentHashMap<String, Object> map = new ConcurrentHashMap<String, Object>();
        map = gson.fromJson(string, ConcurrentHashMap.class);
        return map;
    }
}
