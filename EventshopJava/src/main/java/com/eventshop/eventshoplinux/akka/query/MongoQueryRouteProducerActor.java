package com.eventshop.eventshoplinux.akka.query;

import akka.camel.javaapi.UntypedProducerActor;

/**
 * Created by nandhiniv on 7/14/15.
 */
public class MongoQueryRouteProducerActor extends UntypedProducerActor {

    @Override
    public String getEndpointUri() {
        return "direct:mongoQueryRouteProducer";
    }
}
