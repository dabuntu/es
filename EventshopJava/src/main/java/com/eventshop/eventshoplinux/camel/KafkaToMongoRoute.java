package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.model.STT;
import com.eventshop.eventshoplinux.ruleEngine.EventshopUtils;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.*;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.internal.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.internal.spi.mapper.JacksonMappingProvider;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;
import oi.thekraken.grok.api.Grok;
import oi.thekraken.grok.api.Match;
import oi.thekraken.grok.api.exception.GrokException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by nandhiniv on 7/24/15.
 */

/**
 * Changes all the data read from Kafka to STT format and populates Mongo
 */
public class KafkaToMongoRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaToMongoRoute.class);

    @Override
    public void configure() throws Exception {

        /**
         * To Read from a file Kafka topic and populate Mongo with STT
         */
        from("direct:csvField")
                .aggregate(header("kafka.TOPIC"), new SimpleAggregationStrategy())
                .completionSize(100)
                .completionInterval(3000)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        List<DBObject> dbObjectList = new ArrayList<DBObject>();
                        String ds = exchange.getIn().getHeader("kafka.TOPIC", String.class);
                        ds = ds.replace("ds", "");
                        DataSource dataSource = DataCache.registeredDataSources.get(ds);
                        List<String> resultList = exchange.getIn().getBody(ArrayList.class);

                        final Configuration configuration = Configuration.builder()
                                .jsonProvider(new JacksonJsonNodeJsonProvider())
                                .mappingProvider(new JacksonMappingProvider())
                                .build();

                        for (String input : resultList) {
                            String syntax = dataSource.getSyntax();
                            if (!syntax.startsWith("{")) {
                                syntax = "{" + syntax + "}";
                            }
                            //Converts the syntax to a Map
                            ConcurrentHashMap<String, Object> map = EventshopUtils.convertSyntaxToJson(syntax);
                            JsonParser parser = new JsonParser();
                            JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();

                            String line = input.replace("[", "");
                            line = line.replace("]", "");
                            String[] lineSplit = line.split(",");
                            Date dateVal = null;
                            Boolean hasDateTime = false;
                            //Parse the map and check the corresponding index exists and update the syntax json to the document
                            // going to be inserted in Mongo
                            for (ConcurrentHashMap.Entry<String, Object> entry : map.entrySet()) {
                                String keyIndex = entry.getKey() + "_index";
                                if (jObj.has(keyIndex)) {
                                    final int index = jObj.get(keyIndex).getAsInt();

                                    if (String.valueOf(entry.getValue()).equalsIgnoreCase("DATETIME")) {
                                        hasDateTime = true;
                                        String dateTimeFormat = jObj.get(entry.getKey() + "_format").getAsString();
                                        String value = lineSplit[index];
                                        value = value.trim();
                                        value = value.replace("T", " ");

                                        SimpleDateFormat simpleDateFormat = null;
                                        if (!dateTimeFormat.equalsIgnoreCase("Long")) {
                                            simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                                            dateVal = simpleDateFormat.parse(value);
                                        } else {
                                            dateVal = new Date(Long.valueOf(value));
                                        }
                                        JsonNode updatedJson
                                                = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dateVal).json();
                                        syntax = updatedJson.toString();
                                    } else if (String.valueOf(entry.getValue()).equalsIgnoreCase("NUMBER")) {
                                        String value = lineSplit[index];
                                        value = value.trim();
                                        Double dVal = Double.valueOf(value);
                                        JsonNode updatedJson
                                                = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dVal).json();
                                        syntax = updatedJson.toString();
                                    } else {
                                        String value = lineSplit[index];
                                        value = value.trim();
                                        JsonNode updatedJson
                                                = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), value).json();
                                        syntax = updatedJson.toString();
                                    }
                                }
                            }
                            JsonObject asJsonObject = parser.parse(syntax).getAsJsonObject();
                            asJsonObject.addProperty("raw_data", line);
                            if (!hasDateTime) {
                                dateVal = new Date();
                                asJsonObject.addProperty("timestamp", dateVal.getTime());
                            }
                            DBObject dbObj = (DBObject) JSON.parse(asJsonObject.toString());
                            //Add the document to the document list to update as a batch
                            dbObjectList.add(dbObj);
                        }

                        String dsId = dataSource.getSrcID();
                        String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=insert";
                        exchange.getOut().setHeader("mPath", mongoPath);
                        exchange.getOut().setBody(dbObjectList);
                    }
                })
                .choice()
                .when(header("mPath").isNotNull())
                .recipientList(header("mPath"))
        ;

        /**Read from kafka to Mongo for XML type */
        from("direct:xml")
                .aggregate(header("kafka.TOPIC"), new SimpleAggregationStrategy())
                .completionSize(100)
                .completionInterval(3000)
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        String ds = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
                        ds = ds.replace("ds", "");
                        DataSource dataSource = DataCache.registeredDataSources.get(ds);
//                        String body = exchange.getIn().getBody(String.class);
                        List<DBObject> dbObjectList = new ArrayList<DBObject>();
                        List<String> resultList = exchange.getIn().getBody(ArrayList.class);
                        JsonParser parser = new JsonParser();
//                        JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        Grok grok = Grok.create("src/main/resources/patterns");


                        final Configuration configuration = Configuration.builder()
                                .jsonProvider(new JacksonJsonNodeJsonProvider())
                                .mappingProvider(new JacksonMappingProvider())
                                .build();

                        for (String input : resultList) {
                            String syntax = dataSource.getSyntax();
                            if (!syntax.startsWith("{")) {
                                syntax = "{" + syntax + "}";
                            }
                            //Converts the syntax to a Map
                            ConcurrentHashMap<String, Object> map = EventshopUtils.convertSyntaxToJson(syntax);
                            JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();

                            String line = input.replace("[", "");
                            line = line.replace("]", "");
                            String[] lineSplit = line.split(",");
                            Date dateVal = null;
                            Boolean hasDateTime = false;
                            //Parse the map and check the corresponding index exists and update the syntax json to the document
                            // going to be inserted in Mongo
                            for (ConcurrentHashMap.Entry<String, Object> entry : map.entrySet()) {


                                try {
                                    String Key = entry.getKey().replace(".", "_");
                                    String keyPath = Key + "_path";
                                    String keyGrok = Key + "_grok";
                                    if (String.valueOf(entry.getValue()).equalsIgnoreCase("DATETIME")) {
                                        hasDateTime = true;
                                        String value = "";
                                        String dateTimeFormat = jObj.get(entry.getKey() + "_format").getAsString();

                                        if (jObj.has(keyPath)) {
                                            final String path = jObj.get(keyPath).getAsString();
                                            value = xpath(path).evaluate(getContext(), input);

                                            if (jObj.has(keyGrok)) {
                                                String expression = jObj.get(keyGrok).getAsString();
                                                grok.compile(expression);
                                                Match match = grok.match(value);
                                                match.captures();
                                                LOGGER.debug("match is " + match.toJson(true));
                                                JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                            }

                                            value = value.trim();
    //                                    value = value.replace("T", " ");

                                            SimpleDateFormat simpleDateFormat = null;
                                            if (!dateTimeFormat.equalsIgnoreCase("Long")) {
                                                simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                                                dateVal = simpleDateFormat.parse(value);
                                            } else {
                                                dateVal = new Date(Long.valueOf(value));
                                            }
                                            JsonNode updatedJson
                                                    = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dateVal).json();
                                            syntax = updatedJson.toString();

                                        }

                                    } else if (String.valueOf(entry.getValue()).equalsIgnoreCase("NUMBER")) {
                                        String value = "";
                                        if (jObj.has(keyPath)) {
                                            final String path = jObj.get(keyPath).getAsString();
                                            value = xpath(path).evaluate(getContext(), input);

                                            if (jObj.has(keyGrok)) {
                                                String expression = jObj.get(keyGrok).getAsString();
                                                LOGGER.debug("expression:::::   " + expression);
                                                grok.compile(expression);

                                                Match match = grok.match(value);
                                                match.captures();
                                                LOGGER.debug("match is " + match.toJson(true));
                                                JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                            }

                                            value = value.trim();
                                            LOGGER.debug("value:::::::  " + value);
                                            Double dVal = Double.valueOf(value);
                                            JsonNode updatedJson
                                                    = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dVal).json();
                                            syntax = updatedJson.toString();

                                        }

                                    } else {
                                        String value = "";
                                        if (jObj.has(keyPath)) {
                                            final String path = jObj.get(keyPath).getAsString();
                                            value = xpath(path).evaluate(getContext(), input);

                                            if (jObj.has(keyGrok)) {
                                                String expression = jObj.get(keyGrok).getAsString();
                                                grok.compile(expression);
                                                Match match = grok.match(value);
                                                match.captures();
                                                LOGGER.debug("match is " + match.toJson(true));
                                                JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                            }

                                            value = value.trim();
                                            JsonNode updatedJson
                                                    = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), value).json();
                                            syntax = updatedJson.toString();

                                        }

                                    }
                                    LOGGER.debug("syntax is : " + syntax);
                                } catch (GrokException e) {
                                    e.printStackTrace();
                                } catch (JsonSyntaxException e) {
                                    e.printStackTrace();
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (NumberFormatException e) {
                                    e.printStackTrace();
                                } catch (NullPointerException e) {
                                    LOGGER.info("Exception occured when evaluating Grok because the pattern did not match.");
                                    e.printStackTrace();
                                }

                            }

                            JsonObject asJsonObject = parser.parse(syntax).getAsJsonObject();
                            asJsonObject.addProperty("raw_data", line);
                            asJsonObject.addProperty("theme", dataSource.getSrcTheme());
                            if (!hasDateTime) {
                                dateVal = new Date();
                                asJsonObject.addProperty("timestamp", dateVal.getTime());
                            }
                            DBObject dbObj = (DBObject) JSON.parse(asJsonObject.toString());
                            //Add the document to the document list to update as a batch
                            dbObjectList.add(dbObj);
                        }

                        String dsId = dataSource.getSrcID();
                        String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=insert";
                        exchange.getOut().setHeader("mPath", mongoPath);
                        exchange.getOut().setBody(dbObjectList);


                             }
                         }
                )
                .choice()
                .when(header("mPath").isNotNull())
                .recipientList(header("mPath"))
        ;

        /**
         * Consumes from kafka topic and converts to STT and populates Mongo
         */
        from("direct:json")
                .aggregate(header("kafka.TOPIC"), new SimpleAggregationStrategy())
                .completionSize(100)
                .completionInterval(3000)
                .process(new Processor() {
                             @Override
                             public void process(Exchange exchange) throws Exception {

                                 LOGGER.info("Consuming from Kafka in Json");
                                 exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                                 List<DBObject> dbObjectList = new ArrayList<DBObject>();

                                 System.out.println("Consuming from Kafka in Json");

                                 String ds = exchange.getIn().getHeader("kafka.TOPIC", String.class);
                                 LOGGER.info("DS: " + ds);
                                 ds = ds.replace("ds", "");

                                 DataSource dataSource = DataCache.registeredDataSources.get(ds);
                                 List<String> resultList = exchange.getIn().getBody(ArrayList.class);
                                 LOGGER.info("Result List: " + resultList.toString());
                                 for (String result : resultList) {
                                     LOGGER.debug("Result List ****** : " + result);
                                 }
                                 System.out.println("1.Before Grok...");
                                 Grok grok = Grok.create("src/main/resources/patterns");
                                 System.out.println("2.After Grok...");
                                 final Configuration configuration = Configuration.builder()
                                         .jsonProvider(new JacksonJsonNodeJsonProvider())
                                         .mappingProvider(new JacksonMappingProvider())
                                         .build();
                                 System.out.println("3.After Configuration...");
                                 for (String input : resultList) {
                                     System.out.println("4.Into Loop...");
                                     String syntax = dataSource.getSyntax();
                                     if (!syntax.startsWith("{")) {
                                         syntax = "{" + syntax + "}";
                                     }
                                     //Converts the syntax to a Map
                                     ConcurrentHashMap<String, Object> map = EventshopUtils.convertSyntaxToJson(syntax);
                                     JsonParser parser = new JsonParser();
                                     JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();


                                     Date dateVal = null;
                                     Boolean hasDateTime = false;
                                     //Parse the map and check the corresponding index exists and update the syntax json to the document
                                     // going to be inserted in Mongo
                                     for (ConcurrentHashMap.Entry<String, Object> entry : map.entrySet()) {


                                         String Key = entry.getKey();
                                         String keyIndex = entry.getKey() + "_path";
                                         String keyGrok = Key + "_grok";
                                         try {
                                             if (jObj.has(keyIndex)) {
                                                 if (String.valueOf(entry.getValue()).equalsIgnoreCase("DATETIME")) {
                                                     hasDateTime = true;
                                                     String dateTimeFormat = jObj.get(entry.getKey() + "_format").getAsString();
                                                     String value = JsonPath.read(input, ("$." + jObj.get(entry.getKey() + "_path").toString().replace("\"", ""))).toString();
                                                     //        line.get(entry.getKey() + "_path").toString();//lineSplit[index];
                                                     value = value.trim();
                                                     value = value.replace("T", " ");

                                                     if (jObj.has(keyGrok)) {
                                                         String expression = jObj.get(keyGrok).getAsString();
                                                         grok.compile(expression);
                                                         Match match = grok.match(value);
                                                         match.captures();
                                                         System.out.println("match is " + match.toJson(true));
                                                         JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                         value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                                     }

                                                     SimpleDateFormat simpleDateFormat = null;
                                                     if (!dateTimeFormat.equalsIgnoreCase("Long")) {
                                                         simpleDateFormat = new SimpleDateFormat(dateTimeFormat);
                                                         dateVal = simpleDateFormat.parse(value);
                                                     } else {
                                                         dateVal = new Date(Long.valueOf(value));
                                                     }
                                                     JsonNode updatedJson
                                                             = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dateVal).json();
                                                     syntax = updatedJson.toString();
                                                 } else if (String.valueOf(entry.getValue()).equalsIgnoreCase("NUMBER")) {
                                                     String value = JsonPath.read(input, ("$." + jObj.get(entry.getKey() + "_path").toString().replace("\"", ""))).toString();
                                                     //line.get(jObj.get(entry.getKey() + "_path").toString().replace("\\","")).toString();

                                                     if (jObj.has(keyGrok)) {
                                                         String expression = jObj.get(keyGrok).getAsString();
                                                         LOGGER.info("expression:::::   " + expression);
                                                         grok.compile(expression);

                                                         Match match = grok.match(value);
                                                         match.captures();
                                                         LOGGER.debug("match is " + match.toJson(true));
                                                         JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                         value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                                     }
                                                     value = value.trim();
                                                     Double dVal = Double.valueOf(value);
                                                     JsonNode updatedJson
                                                             = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), dVal).json();
                                                     syntax = updatedJson.toString();
                                                     LOGGER.debug("NUMBER:" + dVal);
                                                 } else {

                                                     if (jObj.has(keyIndex)) {
                                                         String value = JsonPath.read(input, ("$." + jObj.get(entry.getKey() + "_path").toString().replace("\"", ""))).toString();
                                                         //line.get(jObj.get(entry.getKey() + "_path").toString()).toString();


                                                         if (jObj.has(keyGrok)) {
                                                             String expression = jObj.get(keyGrok).getAsString();
                                                             grok.compile(expression);
                                                             Match match = grok.match(value);
                                                             match.captures();
                                                             LOGGER.debug("match is " + match.toJson(true));
                                                             JsonObject jsonObject = parser.parse(match.toJson()).getAsJsonObject();
                                                             value = jsonObject.get(Key.replace(".", "_")).getAsString();
                                                         }

                                                         value = value.trim();

                                                         JsonNode updatedJson
                                                                 = JsonPath.using(configuration).parse(syntax).set("$." + entry.getKey(), value).json();
                                                         syntax = updatedJson.toString();
                                                         LOGGER.info("String:" + value);
                                                     }
                                                 }
                                             }
                                         } catch (GrokException e) {
                                             e.printStackTrace();
                                         } catch (JsonSyntaxException e) {
                                             e.printStackTrace();
                                         } catch (ParseException e) {
                                             e.printStackTrace();
                                         } catch (NumberFormatException e) {
                                             e.printStackTrace();
                                         }
                                     }
                                     LOGGER.info("SYNTAX:" + syntax);

                                     //Add exceptions
                                     JsonObject asJsonObject = parser.parse(syntax).getAsJsonObject();
                                     asJsonObject.addProperty("raw_data", input);
                                     if (!hasDateTime) {
                                         dateVal = new Date();
                                         asJsonObject.addProperty("timestamp", dateVal.getTime());
                                     }
                                     LOGGER.debug("Line:" + asJsonObject.toString());
                                     DBObject dbObj = (DBObject) JSON.parse(asJsonObject.toString());
                                     //Add the document to the document list to update as a batch
                                     dbObjectList.add(dbObj);

                                     // }
                                     String dsId = dataSource.getSrcID();
                                     String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=insert";
                                     exchange.getOut().setHeader("mPath", mongoPath);
                                     exchange.getOut().setBody(dbObjectList);


                                 }
                             }
                         }
                )
                .choice()
                .when(header("mPath").isNotNull())
                .recipientList(header("mPath"))
        ;
                    /**
                     * Consumes from Twitter Kafka and populates Mongo
                     */
                    from("direct:twitterProcess")
                            .process(new Processor() {
                                         @Override
                                         public void process(Exchange exchange) throws Exception {
                                             DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
                                             List<Status> statusList = exchange.getIn().getBody(ArrayList.class);
                                             List<STT> sttList = new ArrayList<STT>();
                                             for (Status status : statusList) {
                                                 if (status.getGeoLocation() != null) {
                                                     STT stt = new STT();
                                                     stt.setRawData(status.getText().toString());
                                                     stt.setTheme(dataSource.getSrcTheme());
                                                     stt.setLoc(new ELocation(status.getGeoLocation().getLongitude(), status.getGeoLocation().getLatitude()));
                                                     stt.set_id(Long.valueOf(status.getId()));
                                                     stt.setTimestamp(status.getCreatedAt());
                                                     stt.setValue(1.0);
                                                     sttList.add(stt);
                                                 }
                                             }
                                             //    System.out.println("Inserting tweets with geo location. Tweet size is " + sttList.size());
                                             String dsId = dataSource.getSrcID();
                                             String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=insert";
                                             System.out.println("mPath while insert in file route is " + mongoPath);
                                             exchange.getOut().setHeader("mPath", mongoPath);
                                             exchange.getOut().setBody(sttList);
                                             exchange.getOut().setHeader("datasource", dataSource);
                                             String operation = "sum";
                                             exchange.getOut().setHeader("spatial_wrapper", operation);
                                         }
                                     }
                            )
//                .to("mongodb:mongoBean?database=events&collection=twitter&operation=insert")
                            .

                    recipientList(header("mPath")

                    );
                    from("direct:directLoad")
                            .convertBodyTo(String.class)
//                            .aggregate(header("kafka.TOPIC"), new SimpleAggregationStrategy())
//                            .completionSize(100)
//                            .completionInterval(3000)
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    System.out.println("Direct Load Initiated....");
//                                    List<DBObject> dbObjectList = new ArrayList<DBObject>();
                                    exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                                    String ds = exchange.getIn().getHeader(KafkaConstants.TOPIC, String.class);
                                    ds = ds.replace("ds", "");
                                    DataSource dataSource = DataCache.registeredDataSources.get(ds);

//                                    List<String> resultList = exchange.getIn().getBody(ArrayList.class);
//                                    for (String result : resultList) {
                                    String result = exchange.getIn().getBody(String.class);
                                        JsonParser parser = new JsonParser();
                                    final JsonElement jsonElement = parser.parse(result);
                                    DBObject dbObject;
                                    JsonObject jObj = null;
                                    if (jsonElement.isJsonObject()) {
                                        jObj = jsonElement.getAsJsonObject();
                                        dbObject = (DBObject) JSON.parse(jObj.toString());
                                    } else if (jsonElement.isJsonArray()) {
                                        JsonArray jsonArray = jsonElement.getAsJsonArray();
                                        jObj = new JsonObject();

// populate the array
                                        jObj.add("data", jsonArray);
                                        dbObject = (DBObject) JSON.parse(jObj.toString());
                                    }
                                    exchange.getOut().setBody(jObj.toString());
                                    String dsId = dataSource.getSrcID();
                                    String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=insert";
                                    exchange.getOut().setHeader("mPath", mongoPath);
                                    LOGGER.info("Direct Load done...");
                                }
                            })
                            .choice()
                            .when(header("mPath").isNotNull())
                            .recipientList(header("mPath"));
                }

    public void run() {
        String input = "{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T02:53:17.181Z\",\"end_time\":\"2015-10-17T02:53:22.181Z\"},\"where\":{\"geo_location\":{\"latitude\":28.613152,\"longitude\":77.272167},\"revgeo_places\":[{\"latitude\":28.613152,\"longitude\":77.272167,\"name\":\"Commonwealth Games Village\",\"category\":\"residential\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":5,\"intent_expression_name\":\"Dirty Toilet\",\"intent_expression_display_name\":\"Clean This Now\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"restroom\",\"confidence\":0.85},{\"concept_name\":\"box\",\"confidence\":0.65}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/dirty-toilet.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Nightmare Toilets at Sports Complex\"}";

//        JsonObject jObj = new JsonObject(input);
        String value = JsonPath.read(input, ("$." + "media[0].where.geo_location.latitude").toString().replace("\"", "")).toString();
        System.out.println(value);

        System.out.println(System.currentTimeMillis());

    }
    }