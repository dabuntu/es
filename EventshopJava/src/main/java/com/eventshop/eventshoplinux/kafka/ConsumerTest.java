package com.eventshop.eventshoplinux.kafka;

/**
 * Created by aravindh on 5/27/15.
 */
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import java.util.HashMap;
import java.util.List;

public class ConsumerTest implements Runnable {
    private KafkaStream m_stream;
    private int m_threadNumber;
    HashMap<Integer, DataSource> m_enabledSources;

    public ConsumerTest(KafkaStream a_stream, int a_threadNumber, HashMap<Integer, DataSource> enabledSources) {
        m_threadNumber = a_threadNumber;
        m_stream = a_stream;
        m_enabledSources = enabledSources;
    }

    public void run() {
       ConsumerIterator<byte[], byte[]> it = m_stream.iterator();
        while (it.hasNext()) {
//            System.out.println("Thread String status" + new String(it.next().message()));
//            try {
//                Status status = DataObjectFactory.createStatus(new String(it.next().message()));
//                System.out.println("Tweet in consumerTest is " + status.getText());
            TwitterDatasourceConnector twitterDatasourceConnector = new TwitterDatasourceConnector();
            twitterDatasourceConnector.start(new String(it.next().message()), m_enabledSources);

//            } catch (TwitterException e) {
//                e.printStackTrace();
//            }
        }



    }
}