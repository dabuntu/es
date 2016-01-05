package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.kafka.KafkaConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nandhiniv on 7/24/15.
 */
public class PopulateKafkaRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(PopulateKafkaRoute.class);

    @Override
    public void configure() throws Exception {
        /**
         Populate kafka with the message received common
         */
        from("direct:populateKafka")
             //   .threads(Integer.parseInt(Config.getProperty("threadPoolSize")), Integer.parseInt(Config.getProperty("maxthreadPoolSize")))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        try {
                            exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                            DataSource ds = exchange.getIn().getHeader("dataSource", DataSource.class);
                            String kafkaPath = "kafka:" + Config.getProperty("kafkaURI") + "?topic=ds" + ds.getSrcID() + "&serializerClass=" + Config.getProperty("kafkaSerializationClass");
                            exchange.getOut().setBody(exchange.getIn().getBody());
                            exchange.getOut().setHeader(KafkaConstants.PARTITION_KEY, "1");
                            exchange.getOut().setHeader("kPath", kafkaPath);
                            System.out.println("Successfully populated Kafka");
                        } catch (Exception e) {

                            e.printStackTrace();
                        }
                    }
                })
                .recipientList(header("kPath"));

    }
}