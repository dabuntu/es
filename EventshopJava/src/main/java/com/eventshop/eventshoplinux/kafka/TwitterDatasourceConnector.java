package com.eventshop.eventshoplinux.kafka;

/**
 * Created by abhisekmohanty on 28/5/15.
 */
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.servlets.RegisterServlet;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class TwitterDatasourceConnector {
    private static final Logger logger = LoggerFactory.getLogger(TwitterDatasourceConnector.class);

    /*Hashmap to store all the running ds*/
    HashMap<Integer, DataSource> n_enabledSources;
    Status newStatus;

    public void start(String status, HashMap<Integer, DataSource> enabledSources) {

       // System.out.println("Inside TDC.start");
       // System.out.println("in tdc raw json status is " + status);
        n_enabledSources = enabledSources;

        /** Producer properties **/
        Properties props = new Properties();
        props.put("metadata.broker.list", Config.getProperty("kafkaURI"));
        props.put("serializer.class", Config.getProperty("kafkaSerializationClass"));
        props.put("request.required.acks", Config.getProperty("RequiresAck"));

        ProducerConfig config = new ProducerConfig(props);

        final Producer<String, String> producer = new Producer<String, String>(config);

        final Map<String, String> headers = new HashMap<String, String>();

//        for (Status status : statuses) {

        try {
            newStatus = DataObjectFactory.createStatus(status);
        } catch (TwitterException e) {
            e.printStackTrace();
        }
        String tweet = newStatus.getText();
        //System.out.println(tweet);
        int i = 0;
        for (Map.Entry<Integer, DataSource> entry : enabledSources.entrySet()) {
            boolean test = false;
            int dsId = entry.getKey();
            DataSource ds = entry.getValue();
            String bagOfWords = ds.getBagOfWords().toString();
            bagOfWords = bagOfWords.replace("[", "");
            bagOfWords = bagOfWords.replace("]", "");
            StringTokenizer st = new StringTokenizer(bagOfWords, ", ");
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
            //    System.out.println("@@@@@@@@" + token);
                if (tweet.contains(token)) {
                    test = true;
                }
            }
            if (test) {
              //  System.out.println("In TDC Tweet belongs to " + dsId);
                try {
                    KeyedMessage<String, String> data = new KeyedMessage<String, String>("ds" + dsId, status);
                    //      System.out.println(status);
                    producer.send(data);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
//        }
    }


}