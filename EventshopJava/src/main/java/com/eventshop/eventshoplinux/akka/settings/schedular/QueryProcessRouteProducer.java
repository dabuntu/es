package com.eventshop.eventshoplinux.akka.settings.schedular;

import akka.camel.javaapi.UntypedProducerActor;

/**
 * Created by nandhiniv on 6/11/15.
 */

/**
 * This actor is used to send message to the Camel Endpoint which starts the query execution.
 */
public class QueryProcessRouteProducer extends UntypedProducerActor {
    public String getEndpointUri() {
        return "direct:queryInit";
    }


}

