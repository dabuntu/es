package com.eventshop.eventshoplinux.demo;

/**
 * Created by nandhiniv on 6/16/15.
 */

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;

public class DynamicRoute extends RouteBuilder {
    private final String from;
    private final String to;

    public DynamicRoute(CamelContext context, String from, String to) {
        super(context);
        this.from = from;
        this.to = to;
    }

    @Override
    public void configure() throws Exception {
        from(from).to(to);
    }
}