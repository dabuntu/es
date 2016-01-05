package com.eventshop.eventshoplinux.akka;

import akka.camel.javaapi.UntypedProducerActor;

/**
 * Created by nandhiniv on 6/11/15.
 */

/**
 * This class is used to send message to the Camel Endpoint which will execute the alerts for the query.
 */
public class AlertRouteProducer extends UntypedProducerActor {
    public String getEndpointUri() {
        return "direct:runAlertForQuery";
    }
}
