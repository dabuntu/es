package com.eventshop.eventshoplinux.ruleEngine;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Created by nandhiniv on 8/25/15.
 */
public class CreateMongoQuery {

    private final static Logger LOGGER = LoggerFactory.getLogger(CreateMongoQuery.class);


//    String sttString = "{\"timestamp\":1436761992124,\"_id\":null,\"theme\":\"Current Impl\",\"raw_data\":\"Raw data\",\"value\":4.5,\"location\":{\"lon\":20.5,\"lat\":54.5}}";
//    STT_<ELocation,Long,String,Double> stt = objectMapper.readValue(sttString, new TypeReference<STT_<ELocation, Long, String, Double>>() { });
//
//    String sttWindString = "{\"timestamp\":1436769573860,\"theme\":\"Wind theme\",\"raw_data\":\"Raw_data\",\"_id\":null,\"value\":{\"speed\":5,\"direction\":\"NE\"},\"location\":\"Bangalore, India\"}\n";
//    STT_<String, Date, String,Wind> sttWind = objectMapper.readValue(sttWindString, new TypeReference<STT_<String, Date, String,Wind>>() { });


    public static void main(String[] args) throws Exception {

//        StringTokenizer st = new StringTokenizer("10.1,10.2,10.3,10.4",",");
//        while(st.hasMoreElements()){
//            System.out.println(st.nextElement());
//        }
        StringTokenizer st;
        Rule rule1 = new Rule("loc", "radius", "-117.37,33.19,2");
//        Rule rule1 = new Rule("loc", "coordinates", "-112.969727,32.249974,-114.257813,33.000325");
//        Rule rule1 = new Rule("value", "=", "100");

//        Rule rule2 = new Rule("value", ">", "100");
////        Rule rule3 = new Rule("value", ">", "50");
//
        ArrayList<Rule> ruleList = new ArrayList<>();
        ruleList.add(rule1);
//        ruleList.add(rule2);
//        ruleList.add(rule3);

        Rules rules = new Rules(ruleList, "ds18", "loc");

//        ObjectMapper objectMapper = new ObjectMapper();
//        String result = objectMapper.writeValueAsString(rules);
//
//        System.out.println(result);


        // Connect to the Mongo database
        Mongo mongoConn = new Mongo(Config.getProperty("mongoHost"), Integer.parseInt(Config.getProperty("mongoPort")));
        DB mongoDb = mongoConn.getDB("events");
        DBCollection collection = mongoDb.getCollection(rules.getSource());


        // Building the query parameters from Rules
        QueryBuilder query = new QueryBuilder();

        List<Rule> rulesList = rules.getRules();

        for (Rule rule : rulesList) {
            switch (rule.getRuleOperator()) {
                case "<"://Number
                    query.put(rule.getDataField()).lessThan(Double.valueOf(rule.getRuleParameters()));
                    break;
                case ">"://Number
                    query.put(rule.getDataField()).greaterThan(Double.valueOf(rule.getRuleParameters()));
                    break;
                case "="://Number
                    query.put(rule.getDataField()).is(Double.valueOf(rule.getRuleParameters()));
                    break;
                case "!="://Number
                    query.put(rule.getDataField()).notEquals(Double.valueOf(rule.getRuleParameters()));
                    break;
                case "regex"://String
                    query.put(rule.getDataField()).regex(Pattern.compile(rule.getRuleParameters()));
                    break;
                case "equals"://String
                    query.put(rule.getDataField()).equals(rule.getRuleParameters());
                    break;
                case "coordinates"://Location
                    st = new StringTokenizer(rule.getRuleParameters(), ",");
                    query.put(rule.getDataField()).withinBox(
                            Double.valueOf(String.valueOf(st.nextElement()))
                            , Double.valueOf(String.valueOf(st.nextElement()))
                            , Double.valueOf(String.valueOf(st.nextElement()))
                            , Double.valueOf(String.valueOf(st.nextElement()))
                    );
                    break;
                case "address"://Location
                    query.put(rule.getDataField()).equals(rule.getRuleParameters());
                    break;
                case "radius"://Location
                    st = new StringTokenizer(rule.getRuleParameters(), ",");
                    query.put(rule.getDataField()).withinCenter(
                            Double.valueOf(String.valueOf(st.nextElement()))
                            , Double.valueOf(String.valueOf(st.nextElement()))
                            , Double.valueOf(String.valueOf(st.nextElement()))
                    );
                    break;
                default:
                    LOGGER.info("Invalid Query Operator");

            }

        }

        LOGGER.debug(query.toString());


        LOGGER.debug(""+collection.getCount());

//        query.put("location.row").is(3);
//        query.put("value").is(false);

        DBCursor dbCursor = collection.find(query.get());
//
        LOGGER.debug("Size : " + dbCursor.size());


        while (dbCursor.hasNext()) {
            StringBuffer result = new StringBuffer();

            result.append("{");

            DBObject next = dbCursor.next();
            LOGGER.debug(""+next);

            st = new StringTokenizer(rules.getExtractFields(), ",");


            while (st.hasMoreElements()) {
                String field = String.valueOf(st.nextElement());
                result.append("\"" + field + "\"" + " : " + next.get(field));

                if (st.hasMoreElements()) {
                    result.append(", ");
                }

            }
            result.append("}");
            LOGGER.debug(""+result);
            ;

        }

    }
}
