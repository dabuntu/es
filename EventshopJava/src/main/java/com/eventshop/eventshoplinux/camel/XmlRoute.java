package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.language.tokenizer.TokenizeLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by nandhiniv on 6/29/15.
 */
public class XmlRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(XmlRoute.class);

    @Override
    public void configure() throws Exception {

        /**
         * Route to read the file and redirect based on isList=true
         */
        from("direct:toXPath")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        String body = exchange.getIn().getBody(String.class);
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        exchange.getOut().setBody(body);

                        //Check if it is a list and split the list before loading to kafka
                        Boolean isList = false;

                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(ds.getWrapper().getWrprKeyValue()).getAsJsonObject();

                        if (jObj.has("isList")) {
                            JsonElement isList1 = jObj.get("isList");
                            if (isList1 != null) {
                                isList = isList1.getAsBoolean();
                            }
                        }


                        if (isList) {
                            String rootElement = jObj.get("rootElement").getAsString();
                            rootElement = rootElement.replace("/", "//");
                            String tokenizeElement = jObj.get("tokenizeElement").getAsString();
                            exchange.getOut().setHeader("isList", true);
                            exchange.getOut().setHeader("tokenizeElement", tokenizeElement);
                            exchange.getOut().setHeader("rootElement", rootElement);

                        }
                    }
                })
                .choice()
                .when(header("isList").isEqualTo(true))
                .to("direct:splitXmlList")
                .otherwise()
                .to("direct:populateKafka")
                .end()


        ;
        /**
         Route to split the list and tokenize it before populating kafka
         */
        from("direct:splitXmlList")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        final String splitToken = exchange.getIn().getHeader("tokenizeElement").toString();

                        final TokenizeLanguage tok = new TokenizeLanguage();
                        tok.setXml(true);
                        tok.setIncludeTokens(true);
                        tok.setToken(splitToken);
                        tok.setInheritNamespaceTagName(exchange.getIn().getHeader("rootElement", String.class));
                        final Expression expression = tok.createExpression();
                        List result = expression.evaluate(exchange, List.class);
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setBody(result);

                    }
                })
                .split().body()
                .to("direct:populateKafka")
        ;


    }
}
