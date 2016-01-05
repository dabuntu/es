package com.eventshop.eventshoplinux.kafka;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

public class TwitterProducer {
    private static final Logger logger = LoggerFactory.getLogger(TwitterProducer.class);
    /*Hashmap to store all the running ds*/
    HashMap<Integer, DataSource> enabledSources;
    /** Information necessary for accessing the Twitter API */
    private String consumerKey;
    private String consumerSecret;
    private String accessToken;
    private String accessTokenSecret;
    /** The actual Twitter stream. It's set up to collect raw JSON data */
    private TwitterStream twitterStream;

    public void start() {

        enabledSources = DataCache.twitterSources;

        /** Producer properties **/
        Properties props = new Properties();
        props.put("metadata.broker.list", Config.getProperty("kafkaURI"));
        props.put("serializer.class", Config.getProperty("kafkaSerializationClass"));
        props.put("request.required.acks", Config.getProperty("RequiresAck"));

        ProducerConfig config = new ProducerConfig(props);

        final Producer<String, String> producer = new Producer<String, String>(config);

        /** Twitter properties **/
        consumerKey = Config.getProperty("twtConsumerKey");
        consumerSecret = Config.getProperty("twtConsumerSecret");
        accessToken = Config.getProperty("twtAccessToken");
        accessTokenSecret = Config.getProperty("twtAccessTokenSecret");

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(consumerKey);
        cb.setOAuthConsumerSecret(consumerSecret);
        cb.setOAuthAccessToken(accessToken);
        cb.setOAuthAccessTokenSecret(accessTokenSecret);
        cb.setJSONStoreEnabled(true);
        cb.setIncludeEntitiesEnabled(true);
//
        FilterQuery query = new FilterQuery();
        DataSourceManagementDAO dsDao = new DataSourceManagementDAO();
        StringTokenizer vals = dsDao.getAllBagOfWords();
        String[] keywordsArray = new String[vals.countTokens()];

        int i = 0;
        while (vals.hasMoreTokens()) {
            keywordsArray[i] = vals.nextToken();
//            System.out.println(keywordsArray[i]);
            i++;
        }
        query.track(keywordsArray);

//        double[][] bb = {{66.621094, 7.188101}, {98.085938, 34.452218}};
//
//        query.locations(bb);
        // twitterStream.filter(query);

        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        final Map<String, String> headers = new HashMap<String, String>();

        StatusListener listener = new StatusListener() {
            public void onStatus(Status status) {
                KeyedMessage<String, String> data = new KeyedMessage<String, String>("Tweets", DataObjectFactory.getRawJSON(status));
                producer.send(data);
                // System.out.println("*********************");

            }

            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}

            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}

            public void onScrubGeo(long userId, long upToStatusId) {}

            public void onException(Exception ex) {
                logger.info("Shutting down Twitter sample stream...");
                ex.printStackTrace();
                twitterStream.shutdown();
            }

            public void onStallWarning(StallWarning warning) {}
        };
        twitterStream.addListener(listener);
        twitterStream.filter(query);
        //Start the consumer
        TwitterMongoConsumer tmc = new TwitterMongoConsumer(Config.getProperty("zkURI"), "group1", Config.getProperty("twitterInitialTopic"));
        logger.debug("call tmc.run");
        tmc.run(1, enabledSources);
    }
}