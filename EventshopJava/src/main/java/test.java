import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

/**
 * Created by abhisekmohanty on 19/10/15.
 */
public class test {

    public static void main(String[] args) {
        String twtConsumerKey = "d4mqtPhYMgRiwOrFZPZ74j8g0";
        String twtConsumerSecret = "MVqw17JiLzFCg5jpGCM00ld4LN2V744uOkNvSRESxuPm667pix";
        String twtAccessToken = "3241153922-uxziMTdQFZpAR3qQ7n9z1rgiJIBLaQsz0kbEIol";
        String twtAccessTokenSecret = "UIyUVWzfyMWs4J7Hqh39w0lDhbenJfSzilDuqCyNR0aL6";
                            ConfigurationBuilder cb = new ConfigurationBuilder();
                            cb.setDebugEnabled(true)
                                    .setOAuthConsumerKey(twtConsumerKey)
                                    .setOAuthConsumerSecret(twtConsumerSecret)
                                    .setOAuthAccessToken(twtAccessToken)
                                    .setOAuthAccessTokenSecret(twtAccessTokenSecret);
                            try {

                                System.out.println("In twitter search");
                                TwitterFactory factory = new TwitterFactory(cb.build());
                                Twitter twitter = factory.getInstance();

//                            ResponseList<User> result = twitter.searchUsers("Raghav Kamran", 10);
//
//                            for (User user : result) {
//                                System.out.println(user.getScreenName());
//                            }getScreenName

//                            twitter.showUser("raghavkamran");

//                            System.out.println(user.getName());
//                            System.out.println(user.getURL());


                                List<Status> statuses = twitter.getUserTimeline("repcardenas");
//                            Query query = new Query();
//                            query.setCount(3200);
//                            query.setQuery("crime");
//                            QueryResult result;
//                            result = twitter.search(query);
//                            List<Status> statuses = result.getTweets();
                                System.out.println("&&&&&&&&&" + statuses.size());

                                for(Status status : statuses){
                                    System.out.println(status);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
    }
