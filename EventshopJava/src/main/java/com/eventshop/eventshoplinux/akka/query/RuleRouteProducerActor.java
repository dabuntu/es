package com.eventshop.eventshoplinux.akka.query;

import akka.camel.javaapi.UntypedProducerActor;

/**
 * Created by nandhiniv on 8/31/15.
 */
public class RuleRouteProducerActor extends UntypedProducerActor {
    @Override
    public String getEndpointUri() {
        return "direct:applyRule";
    }
}
