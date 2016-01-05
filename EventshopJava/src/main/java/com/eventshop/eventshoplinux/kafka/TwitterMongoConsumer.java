package com.eventshop.eventshoplinux.kafka;


import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import kafka.consumer.ConsumerConfig;
import kafka.consumer.KafkaStream;
import kafka.javaapi.consumer.ConsumerConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


/**
 * Created by aravindh on 5/27/15.
 */


public class TwitterMongoConsumer {
    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterMongoConsumer.class);
    private final ConsumerConnector consumer;
    private final String topic;
    private  ExecutorService executor;

    public TwitterMongoConsumer(String a_zookeeper, String a_groupId, String a_topic) {
        consumer = kafka.consumer.Consumer.createJavaConsumerConnector(
                createConsumerConfig(a_zookeeper, a_groupId));
        this.topic = a_topic;
    }

    public void shutdown() {
        if (consumer != null) consumer.shutdown();
        if (executor != null) executor.shutdown();
        try {
            if (!executor.awaitTermination(5000, TimeUnit.MILLISECONDS)) {
                LOGGER.error("Timed out waiting for consumer threads to shut down, exiting uncleanly");
            }
        } catch (InterruptedException e) {
            LOGGER.error("Interrupted during shutdown, exiting uncleanly");
        }
    }

    public void run(int a_numThreads, HashMap<Integer, DataSource> enabledSources) {

        LOGGER.debug("Inside tmc.run");
        Map<String, Integer> topicCountMap = new HashMap<String, Integer>();
        topicCountMap.put(topic, new Integer(a_numThreads));
        Map<String, List<KafkaStream<byte[], byte[]>>> consumerMap = consumer.createMessageStreams(topicCountMap);
        List<KafkaStream<byte[], byte[]>> streams = consumerMap.get(topic);

        // now launch all the threads
        //
        executor = Executors.newFixedThreadPool(a_numThreads);

        // now create an object to consume the messages
        //
        int threadNumber = 0;
        for (final KafkaStream stream : streams) {
            executor.submit(new ConsumerTest(stream, threadNumber, enabledSources));
            threadNumber++;
        }
    }

    private static ConsumerConfig createConsumerConfig(String a_zookeeper, String a_groupId) {
        Properties props = new Properties();
        props.put("zookeeper.connect", a_zookeeper);
        props.put("group.id", a_groupId);
        props.put("zookeeper.session.timeout.ms", "400");
        props.put("zookeeper.sync.time.ms", "200");
        props.put("auto.commit.interval.ms", "1000");

        return new ConsumerConfig(props);
    }

//    public static void main(String[] args) {
//        String zooKeeper = args[0];
//        String groupId = args[1];
//        String topic = args[2];
//        int threads = Integer.parseInt(args[3]);
//
//        TwitterMongoConsumer example = new TwitterMongoConsumer(zooKeeper, groupId, topic);
//        example.run(threads);
//
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException ie) {
//
//        }
//      //  example.shutdown();
//    }
}
