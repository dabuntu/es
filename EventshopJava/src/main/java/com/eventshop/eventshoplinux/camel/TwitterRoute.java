package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nandhiniv on 5/5/15.
 */
public class TwitterRoute extends RouteBuilder {

    private final static org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(TwitterRoute.class);

    @Override
    public void configure() throws Exception {


        from("direct:twitterSearch")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);

                        double swLat = dataSource.getInitParam().swLat;
                        double swLong = dataSource.getInitParam().swLong;
                        double neLat = dataSource.getInitParam().neLat;
                        double neLong = dataSource.getInitParam().neLong;
                        double a = neLat - swLat;
                        double b = neLong - swLong;
                        double lat = swLat + (a / 2);
                        double lng = swLong + (b / 2);
                        double radius = (Math.ceil(Math.abs(Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2))))) / 2;
                        LOGGER.info("lat:" + lat);
                        LOGGER.info("long:" + lng);
                        LOGGER.info("radius:" + radius);
                        boolean queryExist=true;
                        ArrayList<String> bagOfWords = dataSource.getBagOfWords();
                        String queryStr = bagOfWords.get(0);
                        if(queryStr.equals("*")){
                            queryExist=false;
                        }else {
                            for (int i = 1; i < bagOfWords.size(); i++)
                                queryStr += (" OR " + bagOfWords.get(i));
                        }


                        Twitter twitter;

                        ConfigurationBuilder cb = new ConfigurationBuilder();
                        cb.setDebugEnabled(true)
                                .setOAuthConsumerKey(Config.getProperty("twtConsumerKey"))
                                .setOAuthConsumerSecret(Config.getProperty("twtConsumerSecret"))
                                .setOAuthAccessToken(Config.getProperty("twtAccessToken"))
                                .setOAuthAccessTokenSecret(Config.getProperty("twtAccessTokenSecret"));
//                        cb.setUseSSL(true);

                        TwitterFactory tf = new TwitterFactory(cb.build());
                        twitter = tf.getInstance();

                        Query query = new Query();
                        query.setCount(3200);
                        query.setGeoCode(new GeoLocation(lat, lng), radius * 110, Query.KILOMETERS);
                        if(queryExist){
                            query.setQuery(queryStr);
                        }

                        QueryResult result;
                        LOGGER.info("Reading from Twitter...");
                        List<Status> tweets = new ArrayList<Status>();
                        try {

                            result = twitter.search(query);
                            tweets.addAll(result.getTweets());
                            LOGGER.info("list size is " + tweets.size());
                            long maxId = result.getMaxId() - 1;
//                            LOGGER.info("MaxId is " + maxId);
                            query = result.nextQuery();

                            if (query != null) {
                                query.setMaxId(maxId);
                                do {
                                    LOGGER.info("Getting next set of tweets.");
                                    result = twitter.search(query);
                                    tweets.addAll(result.getTweets());
                                    LOGGER.info("list size is " + tweets.size());
                                    maxId = result.getMaxId() - 1;
//                                        LOGGER.info("MaxId is " + maxId);
                                    query = result.nextQuery();
                                } while (query != null);
                            }


                            LOGGER.info("Finished reading " + tweets.size() + " from twitter.");

                        } catch (TwitterException e1) {
                            e1.printStackTrace();
                        }
                        exchange.getOut().setBody(tweets);
                    }
                })
                .to("direct:twitterProcess");
//                .to("direct:commonQueryMongo");


        from("direct:Twitter")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        String ds = exchange.getIn().getHeader("kafka.TOPIC", String.class);
                        ds = ds.replace("ds", "");
                        DataSource dataSource = DataCache.registeredDataSources.get(ds);
                        String input = exchange.getIn().getBody(String.class);
                        Status newStatus = DataObjectFactory.createStatus(input);
                        List<Status> tweets = new ArrayList<Status>();
                        //GeoFilter Logic
                        if (newStatus.getGeoLocation() != null) {
                            double swlat = dataSource.getInitParam().getSwLat();
                            double swLong = dataSource.getInitParam().getSwLong();
                            double nelat = dataSource.getInitParam().getNeLat();
                            double nelong = dataSource.getInitParam().getNeLong();

                            double currLat = newStatus.getGeoLocation().getLongitude();
                            double currLong = newStatus.getGeoLocation().getLatitude();
                            if ((currLat >= swlat && currLat <= nelat) && (currLong >= swLong && currLong <= nelong)) {
                                LOGGER.info("Current Lat Long : " + currLat + ":" + currLong);
                                LOGGER.info("SW lat long: " + swlat + ":" + swLong);
                                LOGGER.info("NE lat long: " + nelat + ":" + nelong);
                                LOGGER.info("Tweet with in range");
                                tweets.add(newStatus);
                            } else {
                                LOGGER.info("Current Lat Long : " + currLat + ":" + currLong);
                                LOGGER.info("SW lat long: " + swlat + ":" + swLong);
                                LOGGER.info("NE lat long: " + nelat + ":" + nelong);
                                LOGGER.info("Tweet Not in Range...");

                            }

                        } else {
                            LOGGER.info("Tweet not GEO tagged... ");
                        }


                        if (tweets.size() > 0) {
                            exchange.getOut().setHeader("hasTweets", true);
                        }
                        exchange.getOut().setHeader("datasource", dataSource);
                        exchange.getOut().setBody(tweets);
                    }
                })  .choice()
                .when(header("hasTweets").isEqualTo(true))
                .to("direct:twitterProcess")
                .endChoice();





    }

}