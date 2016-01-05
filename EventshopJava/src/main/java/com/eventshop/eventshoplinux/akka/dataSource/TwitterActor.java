package com.eventshop.eventshoplinux.akka.dataSource;

import akka.actor.UntypedActor;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.kafka.TwitterProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by nandhiniv on 6/17/15.
 */

/**
 * This actor is used to start to start the Twitter Streaming API.
 */
public class TwitterActor extends UntypedActor {

    private final static Logger LOG = LoggerFactory.getLogger(DataSourceSchedular.class);
    private boolean isTwitterConnectionEstablished = false;
    private TwitterProducer twitterProducer1 = new TwitterProducer();
    private TwitterProducer twitterProducer2 = new TwitterProducer();
    private int twitterHandleBy = 0;


    public boolean autoAck() {
        return false;
    }

    /**
     * This method is used to start/ switch the TwitterProducer.
     *
     * @param message, of type StartTwitterProducer
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof StartTwitterProducer) {
            StartTwitterProducer startTwitterProducer = (StartTwitterProducer) message;
            String dsID = String.valueOf(startTwitterProducer.getId());
            DataSource ds = DataCache.registeredDataSources.get(dsID);
            if (ds.getUrl().contains("www.twitter.com")) {
                if (isTwitterConnectionEstablished) {
                    if (twitterHandleBy == 1) {
                        LOG.info("Switching to connection2");
                        twitterProducer2.start();
                        LOG.info("Connection Established...");
                        twitterHandleBy = 2;
                        LOG.info("Closing connection1");
                        twitterProducer1 = new TwitterProducer();
                        LOG.info("Connection1 closed");
                    } else if (twitterHandleBy == 2) {
                        LOG.info("Switching to connection1");
                        twitterProducer1.start();
                        LOG.info("connection established...");
                        twitterHandleBy = 1;
                        LOG.info("Closing connection2");
                        twitterProducer2 = new TwitterProducer();
                        LOG.info("Connection2 closed");
                    }
                } else {
                    twitterProducer1.start();
                    isTwitterConnectionEstablished = true;
                    twitterHandleBy = 1;
                }

            }
        }
    }

    /**
     * This class is used by the TwitterActor to start the Twitter Streaming API.
     */
    public static class StartTwitterProducer {

        private int id;

        public StartTwitterProducer(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

}
