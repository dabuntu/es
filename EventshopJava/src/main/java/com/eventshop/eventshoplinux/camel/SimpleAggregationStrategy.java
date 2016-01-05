package com.eventshop.eventshoplinux.camel;

import org.apache.camel.Exchange;
import org.apache.camel.processor.aggregate.AggregationStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhisekmohanty on 22/5/15.
 */
public class SimpleAggregationStrategy implements AggregationStrategy{

    @Override
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        List<String> strList;
        if (oldExchange == null) {
            strList = new ArrayList<String>();
            strList.add(newExchange.getIn().getBody(String.class));
            newExchange.getOut().setHeaders(newExchange.getIn().getHeaders());
            newExchange.getOut().setBody(strList);
            return newExchange;
        } else {
            strList = oldExchange.getIn().getBody(ArrayList.class);
            strList.add(newExchange.getIn().getBody(String.class));
            oldExchange.getOut().setBody(strList);
            oldExchange.getOut().setHeaders(oldExchange.getIn().getHeaders());
//            if (strList.size() == newExchange.getIn().getHeader("size", Integer.class)) {
//                oldExchange.getOut().setHeader("Stop", true);
//                oldExchange.getIn().setHeader("Stop", true);
//            }

            return oldExchange;
        }
    }
}