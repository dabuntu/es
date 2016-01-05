package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nandhiniv on 6/29/15.
 */
public class JsonRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonRoute.class);

    @Override
    public void configure() throws Exception {

        /**
         * Redirect based on isList=true
         */
        from("direct:toJsonPath")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        String body = exchange.getIn().getBody(String.class);
                        exchange.getOut().setBody(body);

                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        exchange.getOut().setHeader("dataSource", ds);

                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(ds.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        String dsType = jObj.get("datasource_type").getAsString();

                        //Check if it is a list and split the list before loading to kafka
//                        JsonObject jObj = exchange.getIn().getHeader("jsonObj", JsonObject.class);
                        JsonElement isList1 = jObj.get("isList");
                        Boolean isList = false;
                        if (isList1 != null) {
                            isList = isList1.getAsBoolean();
                        }
                        String tokenizeElement = "";
                        String rootElement = "";
                        if (isList) {
                            if (!jObj.get("rootElement").isJsonNull()) {
                                rootElement = jObj.get("rootElement").getAsString();
                            }
                            if (!jObj.get("tokenizeElement").isJsonNull()) {
                                tokenizeElement = jObj.get("tokenizeElement").getAsString();
                            }
                            exchange.getOut().setHeader("isList", true);
                            exchange.getOut().setHeader("tokenizeElement", tokenizeElement);
                            exchange.getOut().setHeader("rootElement", rootElement);
                        }
                    }
                })
                .choice()
                .when(header("isList").isEqualTo(true))
                .to("direct:jsonSplitList")
                .otherwise()
                .to("direct:populateKafka")
        ;
        /**
         Route to split the list and tokenize it before populating kafka
         */
        from("direct:jsonSplitList")
//                .split(body().tokenizeXML("item", "cities/list/")).streaming()
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        String body = exchange.getIn().getBody(String.class);
                        String tokenizeElement = exchange.getIn().getHeader("tokenizeElement", String.class);
                        String rootElement = exchange.getIn().getHeader("rootElement", String.class);

                        JsonParser parser = new JsonParser();
                        final JsonElement parse = parser.parse(body);
                        JsonArray jsonElements = null;
                        if (parse.isJsonObject()) {

                            JsonObject jsonObj = parse.getAsJsonObject();


                            String root = "";
                            if (!rootElement.isEmpty()) {
//                            root = String.valueOf(JsonPath.read(body, ("$." + rootElement)));
                                root = jsonObj.get(rootElement).getAsString();

                            } else {
                                root = body;
                            }

                            List<String> element = new ArrayList();

                            if (!tokenizeElement.isEmpty()) {
//                            element = JsonPath.read(root, ("$." + tokenizeElement));
                                jsonElements = jsonObj.get(tokenizeElement).getAsJsonArray();

                            }
                        } else {
                            jsonElements = parse.getAsJsonArray();

                        }

                        LOGGER.info("Successfully split the list. List has {} elements", jsonElements.size());
                        exchange.getOut().setBody(jsonElements);

                    }
                })
                .split().body().streaming()
                .to("direct:populateKafka")
        ;


    }
}
