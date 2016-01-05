package com.eventshop.eventshoplinux.camel.queryProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by nandhiniv on 5/19/15.
 */
public class QueryProcessorRoute extends RouteBuilder {


    protected Log log = LogFactory.getLog(this.getClass().getName());

    @Override
    public void configure() throws Exception {

//
        from("direct:queryInit")
                .to("direct:masterActor")

        ;


    }


}
