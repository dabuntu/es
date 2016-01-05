package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by nandhiniv on 5/13/15.
 */
public class CsvRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(CsvRoute.class);

    @Override
    public void configure() throws Exception {

        from("direct:toCSVPath")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        LOGGER.debug("Kafka ended at : "+ new Date());
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setBody(exchange.getIn().getBody(String.class));
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(ds.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        String dsType = jObj.get("datasource_type").getAsString();
                        String sptlWrpr = jObj.get("spatial_wrapper").getAsString();
//                        int latIndex = jObj.get("lat_index").getAsInt();
//                        int lngIndex = jObj.get("lon_index").getAsInt();
//                        int valIndex = jObj.get("val_index").getAsInt();

                        exchange.getOut().setHeader("datasource_type", dsType);
                        exchange.getOut().setHeader("spatial_wrapper", sptlWrpr);
//                        exchange.getOut().setHeader("lat_index", latIndex);
//                        exchange.getOut().setHeader("lon_index", lngIndex);
//                        exchange.getOut().setHeader("val_index", valIndex);
                        exchange.getOut().setHeader("dataSource", ds);
                    }
                })
                .unmarshal().csv()
                .split(body())
                .to("direct:populateKafka");
    }
}
