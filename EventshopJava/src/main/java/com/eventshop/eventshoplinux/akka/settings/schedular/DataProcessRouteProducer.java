package com.eventshop.eventshoplinux.akka.settings.schedular;

import akka.camel.javaapi.UntypedProducerActor;

/**
 * Created by nandhiniv on 6/5/15.
 */

/**
 * This actor is used to route message to the camel Endpoint which identifies and routes based on the data source type.
 */
public class DataProcessRouteProducer extends UntypedProducerActor {
    public String getEndpointUri() {
        return "direct:dataSourceRouter";
    }

    public boolean autoAck() {
        return false;
    }


}
