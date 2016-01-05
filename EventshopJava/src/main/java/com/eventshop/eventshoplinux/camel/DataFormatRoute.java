package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.lifecycle.DynamicRouteAdder;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Created by nandhiniv on 7/26/15.
 */
public class DataFormatRoute extends RouteBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(DataFormatRoute.class);
    @Override
    public void configure() throws Exception {
        /**
         * Route to read the file and send to {} endpoint
         */
        from("direct:readFromFile")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        String filePath = ds.getUrl();
                        URL url = new URL(filePath);

                        exchange.getOut().setBody(url.openStream());
                    }
                })
                .to("direct:dataType");

        from("direct:readFromRest")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);

                        String wrapperKeyValue = ds.getWrapper().getWrprKeyValue();
                        JsonParser parser = new JsonParser();
                        JsonObject wrapperObj = (JsonObject)parser.parse(wrapperKeyValue);
                        String baseUrl = ds.getUrl().replace("\"", "");
                        if (wrapperObj.has("queryParams")) {
                            baseUrl = baseUrl + "?";
                            JsonObject queryParams = (JsonObject) wrapperObj.getAsJsonArray("queryParams").get(0);

                            Set<Map.Entry<String, JsonElement>> entrySet = queryParams.entrySet();
                            for (Map.Entry<String, JsonElement> entry : entrySet) {
                                LOGGER.debug(entry.getKey() + " : " + entry.getValue());
                                baseUrl = baseUrl + entry.getKey() + "=" + entry.getValue().getAsString().replaceAll("\"", "") + "&";
                            }
                        }

                        URL obj = new URL(baseUrl);
                        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                        // optional default is GET
                        con.setRequestMethod("GET");

                        //add request header

                        int responseCode = con.getResponseCode();
                        LOGGER.info("\nSending 'GET' request to URL : " + baseUrl);
                        LOGGER.info("Response Code : " + responseCode);

                        BufferedReader in = new BufferedReader(
                                new InputStreamReader(con.getInputStream()));
                        String inputLine;
                        StringBuffer response = new StringBuffer();

                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        //print result
                        LOGGER.debug(response.toString());


                        exchange.getOut().setBody(response);
                    }
                })
                .to("direct:dataType");

        from("direct:toTwitterSearchAndStreaming")
                .to("direct:dataType");
//                .to("direct:kafkaConsumerCreation")
//                .to("direct:twitterSearch")
        //This should be changed to twitterMongo
        ;


        from("direct:dataType")
                .to("direct:checkDirectLoad")
                .to("direct:kafkaConsumerCreation")
                .to("direct:mongoCollectionCreation")
                .choice()
                .when(header("directLoad").isNotEqualTo("true"))
                .choice()
                .when(header("dsType").isEqualTo("csvField"))
                .to("direct:toCSVPath")
                .when(header("dsType").isEqualTo("xml"))
                .to("direct:toXPath")
                .when(header("dsType").isEqualTo("json"))
                .to("direct:toJsonPath")
                .when(header("dsType").isEqualTo("Twitter"))
                .to("direct:twitterSearch")
                .when(header("dsType").isEqualTo("visual"))
                .to("direct:toVisual")
                .end()
                .end()

                .delay(Integer.parseInt(Config.getProperty("mongoReadDelay")))
                .to("direct:commonQueryMongo")

        ;


        from("direct:checkDirectLoad")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setBody(exchange.getIn().getBody());
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(ds.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        boolean isDirectLoad=false;
                        if (jObj.has("directLoad")){
                            isDirectLoad= jObj.get("directLoad").getAsBoolean();
                        }

                        if (isDirectLoad){
                            exchange.getOut().setHeader("directLoad", true);
                        }
                    }
                });


        from("direct:mongoCollectionCreation")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setBody(exchange.getIn().getBody());
                        MongoClient mongoClient = new MongoClient();
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        String dsId = ds.getSrcID();
                        DB db = mongoClient.getDB(Config.getProperty("mongoDB"));
                        LOGGER.info("Trying to index...");
                        boolean collectionExists = db.collectionExists("ds" + dsId);
                        if (collectionExists == false) {
                            try {
                                db.createCollection("ds" + dsId, null);

                                DBCollection col = mongoClient.getDB(Config.getProperty("mongoDB")).getCollection("ds" + ds.getSrcID());
                                col.createIndex(new BasicDBObject("timeStamp", -1));
                                //col.ensureIndex(new BasicDBObject("timeStamp",-1),"sparse",true);

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            LOGGER.info("Indexing done... ");
                        } else {
                            LOGGER.info("No indexing needed...");
                        }

                    }
            });


/**
 * Create kafka consumet and redirect to dsType endpoint
 */
        from("direct:kafkaConsumerCreation")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setBody(exchange.getIn().getBody());

                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        String dsId = "ds" + ds.srcID;
                        String uri = "kafka:" + Config.getProperty("kafkaURI") + "?topic=" + dsId + "&zookeeperHost="
                                + Config.getProperty("zkHostName") + "&zookeeperPort=" + Config.getProperty("zkPort")
                                + "&batchSize=" + Integer.parseInt(Config.getProperty("kafkaReadBatchSize")) + "&groupId=group1";
                        CamelContext context = getContext();
                        LOGGER.info("Kafka Started at : "+ new Date());
                        //Check if already exists
                        if (context.hasEndpoint(uri) == null) {
                            String to;
//                            .when(header("directLoad").isEqualTo("true"))
                            Boolean directLoad = exchange.getIn().getHeader("directLoad", Boolean.class);
                            if (directLoad != null) {
                                if (directLoad) {
                                    to = "direct:" + "directLoad";
                                    exchange.getOut().setHeader("dsType", "directLoad");
                                    context.addRoutes(new DynamicRouteAdder(context, uri, to));

                                }
                            } else {
                                to = "direct:" + exchange.getIn().getHeader("dsType");
                                context.addRoutes(new DynamicRouteAdder(context, uri, to));

                            }
                        }
                    }
                });
    }
}
