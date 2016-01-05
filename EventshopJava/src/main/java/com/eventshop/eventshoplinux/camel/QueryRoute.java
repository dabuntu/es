package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nandhiniv on 7/14/15.
 */

/**
 * Route used by Akka actors to read from Mongo and construct Emages
 */
public class QueryRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoRoute.class);

    @Override
    public void configure() throws Exception {


        from("direct:mongoQueryRouteProducer")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        MongoQueryMessage mongoQueryMessage = exchange.getIn().getBody(MongoQueryMessage.class);
                        int dsID = mongoQueryMessage.getDataSourceID();
                        DataSource dataSource = new DataSourceManagementDAO().getDataSource(dsID);
                        Long timeToFilter = mongoQueryMessage.getTimeToFilter();
                        Long endTimeToCheck = mongoQueryMessage.getEndTimeToFilter();
                        double nelat = mongoQueryMessage.getNelat();
                        double nelong = mongoQueryMessage.getNelong();
                        double swlat = mongoQueryMessage.getSwlat();
                        double swlong = mongoQueryMessage.getSwlong();
                        double latUnit = mongoQueryMessage.getLatUnit();
                        double longUnit = mongoQueryMessage.getLonUnit();
                        String spatial_wrapper = mongoQueryMessage.getSpatial_wrapper();

                        exchange.getOut().setHeader("dataSource", dataSource);
                        exchange.getOut().setHeader("datasource", dataSource);
                        exchange.getOut().setHeader("timeToCheck", timeToFilter);
                        exchange.getOut().setHeader("endTimeToCheck", endTimeToCheck);
                        exchange.getOut().setHeader("nelat", nelat);
                        exchange.getOut().setHeader("nelong", nelong);
                        exchange.getOut().setHeader("swlat", swlat);
                        exchange.getOut().setHeader("swlong", swlong);
                        exchange.getOut().setHeader("createEmageFile", false);
                        exchange.getOut().setHeader("latUnit", latUnit);
                        exchange.getOut().setHeader("longUnit", longUnit);
                        exchange.getOut().setHeader("spatial_wrapper", spatial_wrapper);

                    }
                }).to("direct:commonQuery")
        ;

    }
}
