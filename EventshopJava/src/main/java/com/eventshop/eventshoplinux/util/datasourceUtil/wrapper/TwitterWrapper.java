/*package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import static com.eventshop.eventshoplinux.constant.Constant.DB_URL;
import static com.eventshop.eventshoplinux.constant.Constant.DRIVER_NAME;
import static com.eventshop.eventshoplinux.constant.Constant.PASSWORD;
import static com.eventshop.eventshoplinux.constant.Constant.USR_NAME;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;
import twitter4j.GeoLocation;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.SpatialMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.TemporalMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.STTEmageIterator;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;

public class TwitterWrapper extends Wrapper {

	protected static Log log = LogFactory.getLog(TwitterWrapper.class);
	private String[] bagOfWords;

	private long[][] sinceID;
	private Twitter twitter;

	private LinkedBlockingQueue<STTPoint> ls;

	private boolean isRunning = false;

	boolean[][] isPopulated;// = new boolean [13][30];
	// int [][] numTweetsLoc = new int[13][30];

	// For storing tweets into MySQL
	private boolean saveTweets;
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private Connection conn;
	private String tableName;
	private String theme;

	public TwitterWrapper(String url, String theme, FrameParameters params,
			boolean saveTweets) {
		super(url, theme, params);

		if (params.numOfRows == 0 || params.numOfColumns == 0) {
			params.calcRowsColumns();
			log.info("calculated params, rows:" + params.numOfRows + " ,cols:"
					+ params.numOfColumns);
		}

		isPopulated = new boolean[params.numOfRows][params.numOfColumns];

		// Twitter API v1.1 requires that the request must be authenticated
		ConfigurationBuilder cb = new ConfigurationBuilder();
		
		 * cb.setDebugEnabled(true)
		 * .setOAuthConsumerKey("jYjBN7eXBhU0EurloMYGFQ")
		 * .setOAuthConsumerSecret
		 * ("lBekOCCDULTkr7JBLPBlvAV9MV4wVkYoKYcEoBimaWY")
		 * .setOAuthAccessToken("159216395-cJVwHRC2nQdikscMo1Kg6ag1t0vcpmAT3epNZqrB"
		 * )
		 * .setOAuthAccessTokenSecret("baXaT7OlavzZwkG6U3WFLMB2OUnUnWq9xRcxXYg1vs"
		 * );
		 
		cb.setDebugEnabled(true)
				.setOAuthConsumerKey(Config.getProperty("twtConsumerKey"))
				.setOAuthConsumerSecret(Config.getProperty("twtConsumerSecret"))
				.setOAuthAccessToken(Config.getProperty("twtAccessToken"))
				.setOAuthAccessTokenSecret(
						Config.getProperty("twtAccessTokenSecret"));
		cb.setUseSSL(true);

		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();

		ls = new LinkedBlockingQueue<STTPoint>();

		this.saveTweets = saveTweets;
		if (this.saveTweets) {
			this.tableName = "tbl_" + theme + "_tweet";
			connection();
			createTweetTable(theme);
		}

	}

	public void setBagOfWords(String[] words) {
		bagOfWords = words;
	}

	@Override
	public void run() {
		isRunning = true;

		try {
			getPopulation();
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}

		int numofRows = params.getNumOfRows();
		int numOfColumns = params.getNumOfColumns();

		System.out.println("rows = " + numofRows + "cols = " + numOfColumns);
		// Initialize the SinceID matrix
		sinceID = new long[numofRows][numOfColumns];
		for (int i = 0; i < numofRows; ++i)
			for (int j = 0; j < numOfColumns; ++j)
				sinceID[i][j] = 0;

		// Setting the bagOfWords for search
		String queryStr = bagOfWords[0];
		for (int i = 1; i < bagOfWords.length; i++)
			queryStr += (" OR " + bagOfWords[i]);

		// Set end time
		long endTime = (long) Math.ceil(System.currentTimeMillis()
				/ params.timeWindow)
				* params.timeWindow + params.syncAtMilSec;

		while (isRunning) {
			STTPoint point;

			Date start = new Date(endTime - params.timeWindow);

			int numQueries = 0;
			// Loop for Latitude Longitude blocks
			for (double i = params.swLat; i < params.neLat; i = i + params.latUnit) 
			{
				for (double j = params.swLong; j < params.neLong; j = j + params.longUnit) 
				{
					if (!isRunning)
						break;

					int y = (int) Math.ceil(Math.abs((BigDecimal.valueOf(j))
							.subtract(BigDecimal.valueOf(params.swLong))
							.divide(BigDecimal.valueOf(params.longUnit))
							.doubleValue()));
					int x = (int) Math.ceil(Math.abs((BigDecimal.valueOf(i))
							.subtract(BigDecimal.valueOf(params.swLat))
							.divide(BigDecimal.valueOf(params.latUnit))
							.doubleValue()));

					int ret = 0;

					// Uncomment this while running the default frame params
					//if (isPopulated[12-x][y])
					if (isPopulated[x][y]) 
					{
						ret = doCollection(i + 0.5 * params.latUnit, j + 0.5
								* params.longUnit, x, y, params.latUnit, start,
								queryStr);
						numQueries++;
					}
						point = new STTPoint(ret, start, new Date(endTime),
								params.latUnit, params.longUnit, i, j, theme);
						ls.add(point);
				}
			}
				log.info("TwitterWrapper NumQueries made:" + numQueries);

				// Sleeping when window is not up yet
				endTime += params.timeWindow;
				while (System.currentTimeMillis() < endTime) 
				{
					try {
						Thread.sleep(endTime - System.currentTimeMillis());
					} catch (InterruptedException e) {
						log.error(e.getMessage());
					}
				}
			}
		}

	public int doCollection(double lat, double lng, int x, int y,
			double latUnit, Date date, String queryStr) {
		// Create the query and initialize the parameters
		Query query = new Query();
		query.setCount(100);
		query.setGeoCode(new GeoLocation(lat, lng), 60.0 * latUnit,
				Query.KILOMETERS);
		query.setQuery(queryStr);
		// query.setSinceId(sinceID[x][y]);
		query.setSince(new SimpleDateFormat("yyyy-MM-dd").format(date));

		boolean firstOne = true;
		int count = 0;
		QueryResult result;
		try {
			do {
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();

				for (Status tweet : tweets) {
					log.info("@" + tweet.getUser().getScreenName() + " - "
							+ tweet.getText());
					// log.info(tweet.getGeoLocation());
					// log.info(tweet.getPlace());]
					// Update sinceID of this block
					if (firstOne) {
						sinceID[x][y] = tweet.getId();
						firstOne = false;
					}
					if (this.saveTweets) {
						GeoLocation tw_loc = tweet.getGeoLocation();
						double p_lat = 0;
						double p_lon = 0;
						if (tw_loc != null) {
							p_lat = tw_loc.getLatitude();
							p_lon = tw_loc.getLongitude();
						}
						// insert tweet to db only if it contains all the words
						// in the bag.
						// if(tweet.getText().contains("India") &&
						// tweet.getText().contains("#CWC15"))
						insertTweet(lat, lng, x, y, tweet, p_lat, p_lon);
					}
					count++;
				}
			} while ((query = result.nextQuery()) != null && count < 100);
			// log.info("#Tweets @ (lat,long) " + count + " (" + lat + ", " +
			// lng + ")");

		} catch (TwitterException e) {
			log.error("theme:" + theme + ", error: " + e.getMessage());
			try {
				if (e.getMessage().contains("500"))
					return 0;
				if (e.getMessage().contains("420")
						|| e.getMessage().contains("429")
						|| e.getMessage().contains("limit")) {
					Thread.sleep(1000 * 60 * 15);
					log.info("thread sleep for 15 minutes");
				}
			} catch (InterruptedException e2) {
				System.out.println("why error here?" + e2);
				log.error(e2.getMessage());
			}
			return -1;
		}
		return count;
	}

	
	 * This method works with twitter4j version 2.0 which supports twitter API
	 * 1.0. This API 1.0 is not longer supported. By: Siripen, 10/02/13
	 

	
	 * public int doCollection(double lat, double lng, int x, int y, double
	 * latUnit, Date date, String queryStr) { // Create the query and initialize
	 * the parameters Query query = new Query();
	 * 
	 * query.setGeoCode(new GeoLocation(lat,lng), 60.0*latUnit,
	 * Query.KILOMETERS); query.setRpp(100); query.setQuery(queryStr);
	 * query.setSinceId(sinceID[x][y]); query.setSince(new
	 * SimpleDateFormat("yyyy-MM-dd").format(date));
	 * 
	 * int count = 0; int pageID = 1; boolean more = true; boolean firstOne =
	 * true; while(more) { int cnt = 0; query.setPage(pageID); pageID++;
	 * 
	 * try { QueryResult result = twitter.search(query); Iterator<Tweet>
	 * iterator = result.getTweets().iterator(); while(iterator.hasNext()) {
	 * Tweet tweet = iterator.next(); // Update sinceID of this block
	 * if(firstOne) { sinceID[x][y] = tweet.getId(); firstOne = false; }
	 * 
	 * if(this.saveTweets) { GeoLocation tw_loc = tweet.getGeoLocation(); double
	 * p_lat = 0; double p_lon = 0; if (tw_loc != null) { p_lat =
	 * tw_loc.getLatitude(); p_lon = tw_loc.getLongitude(); } insertTweet(lat,
	 * lng, x, y, tweet, p_lat, p_lon); } cnt++; count++; }
	 * 
	 * // No more tweets in the search result if (queryStr.indexOf("Cain")>-1 ||
	 * queryStr.indexOf("Romney")>-1 || queryStr.indexOf("Perry")>-1
	 * )//political tweets { if(cnt < 100 ) //this page contains less than 100
	 * ...i.e. incomplete. hence no point asking for next page more = false; }
	 * else { more=false;}
	 * 
	 * 
	 * } catch (TwitterException e) { try { if(e.getMessage().contains("500"))
	 * return 0; if(e.getMessage().contains("420")) { Thread.sleep(1000*60*20);
	 * } } catch (InterruptedException e2) { log.error(e2.getMessage()); }
	 * return -1; } } return count; }
	 

	@Override
	public boolean stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
		return true;
	}

	@Override
	public STTPoint next() {
		try {
			return ls.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public boolean hasNext() {
		return (ls.peek() != null);
	}

	@Override
	public void remove() {
		ls.remove();
	}

	public void getPopulation() throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(
				Config.getProperty("tempDir") + "visual/newPopulation.txt"));
		String myline = "";
		// double startLat = 24.5;

		int numPop = 0;
		System.out.println("paramssw==" + params.swLat + "params.neLat "
				+ params.neLat + " now params.latUnit " + params.latUnit);
		System.out.println("J LOOP -===params.swLong==" + params.swLong
				+ "params.neLong " + params.neLong + " now params.longUnit "
				+ params.longUnit);
		for (double i = params.swLat; i < params.neLat; i += params.latUnit) {
			myline = br1.readLine();
			StringTokenizer vals = new StringTokenizer(myline, " ,");
			log.info("size of vals:" + vals.countTokens());

			for (double j = params.swLong; j < params.neLong; j += params.longUnit) {
				String val = vals.nextToken();
				if (val.compareTo("1") == 0) {
					isPopulated[(int) ((i - params.swLat) / params.latUnit)][(int) ((j - params.swLong) / params.longUnit)] = true;
					numPop++;
				} else
					isPopulated[(int) ((i - params.swLat) / params.latUnit)][(int) ((j - params.swLong) / params.longUnit)] = false;
			}
		}

		//log.info("numPop:" + numPop);

		// need to change this code and remove hard coded values
		// for(int i = 0; i < 13; i++)
		// {
		// for(int j = 0; j < 30; j++)
		// {
		// numTweetsLoc[i][j] =0;
		// }
		// }
	}

	
	 * public void setPopulation() throws IOException {
	 * 
	 * int []vals = new int[13*30];
	 * 
	 * for(int i = 0; i < 13; i++) { for(int j = 0; j < 30; j++) { vals[i*j] =
	 * numTweetsLoc[i][j]; } } Arrays.sort(vals);
	 * 
	 * int cutOffVal = vals[vals.length-40]; cutOffVal = 5;
	 * log.info("cutoff is :"+ cutOffVal); FileWriter f0 = new
	 * FileWriter("newPopulation.txt");
	 * 
	 * for(int i = 0; i < 13; i++) { for(int j = 0; j < 30; j++) { if (j<29) {
	 * if(numTweetsLoc[i][j]>=cutOffVal) { f0.write(" 1 ,"); } else {
	 * f0.write(" 0 ,"); } } else { if(numTweetsLoc[i][j]>=cutOffVal) {
	 * f0.write(" 1 "); } else { f0.write(" 0 "); }
	 * 
	 * }
	 * 
	 * } f0.write("\n"); } f0.close(); }
	 
	void createTweetTable(String theme) {
		String query1 = "CREATE TABLE IF NOT EXISTS " + tableName + " ( "
				+ "id INT AUTO_INCREMENT," + "tweetid BIGINT NOT NULL,"
				+ "latitude DOUBLE NOT NULL," + "longitude DOUBLE NOT NULL,"
				+ "date DATETIME NOT NULL," + "text TEXT NOT NULL,"
				+ "p_latitude DOUBLE NOT NULL,"
				+ "p_longitude DOUBLE NOT NULL," + "userName TEXT NOT NULL,"
				+ "PRIMARY KEY(id));\n\n";
		String query2 = "CREATE INDEX idx_" + theme + "_Latitude ON "
				+ tableName + "(latitude);\n";
		String query3 = "CREATE INDEX idx_" + theme + "_Longitude ON "
				+ tableName + "(longitude);\n";
		String query4 = "CREATE INDEX idx_" + theme + "_Date ON " + tableName
				+ "(date);";

		try {
			log.info(query1);
			Statement statement = conn.createStatement();
			statement.execute(query1);
			statement.execute(query2);
			statement.execute(query3);
			statement.execute(query4);
		} catch (SQLException e) {
			connection();
		}
	}

	public int insertTweet(double lat, double lng, int x, int y, Status tweet,
			double p_lat, double p_lon) {
		String text = textFilter(tweet.getText());
		if (text == null)
			return -1;

		String date = formatter.format(tweet.getCreatedAt());
		String sql = "INSERT INTO " + tableName + " VALUES (NULL, "
				+ tweet.getId() + ", " + lat + ", " + lng + ", '" + date
				+ "', '" + text + "'," + p_lat + "," + p_lon + ", '"
				+ textFilter(tweet.getUser().getName()) + "')";
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			log.error(sql);
			log.error(e.getMessage());
			connection();
		}
		return 0;
	}

	
	 * This method works with twitter4j version 2.0 which supports twitter API
	 * 1.0. This API 1.0 is not longer supported. By: Siripen, 10/02/13
	 
	
	 * public int insertTweet(double lat, double lng, int x, int y, Tweet tweet,
	 * double p_lat, double p_lon) { String text = textFilter(tweet.getText());
	 * if(text == null) return -1;
	 * 
	 * String date = formatter.format(tweet.getCreatedAt()); String sql =
	 * "INSERT INTO " + tableName + " VALUES (NULL, " + tweet.getId() + ", " +
	 * lat + ", " + lng + ", '" + date + "', '" + text +
	 * "',"+p_lat+","+p_lon+", '"+textFilter(tweet.getFromUser())+"')"; try {
	 * Statement statement = conn.createStatement(); statement.execute(sql); }
	 * catch (SQLException e) { log.error(sql); log.error(e.getMessage());
	 * connection(); } return 0; }
	 

	public String textFilter(String text) {
		if (text == null || text.length() == 0)
			return null;

		for (int i = 0; i < text.length(); i++)
			if (Character.UnicodeBlock.of(text.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN) {
				return null;
			}

		text = text.replaceAll("\t|\r|\n|\r\n|\f", " ");
		text = text.replaceAll("'", "");
		text = text.replace('\\', ' ');
		return text;
	}

	public Connection connection() {
		try {
			Class.forName(DRIVER_NAME);
			String url = Config.getProperty(DB_URL);
			String userName = Config.getProperty(USR_NAME);
			String pwd = Config.getProperty(PASSWORD);
			conn = DriverManager.getConnection(url, userName, pwd);
			// log.info("Connected to Drishti!!");

		} catch (Exception e) {
			// log.error(e.getMessage());
			// log.error("COULD NOT Connect to Drishti!!");
		}
		return conn;
	}

	public static void main(String[] args) {
		String imgBasePath = Config.getProperty("context") + Constant.RESULT_DS;
		String tempDir = Config.getProperty("tempDir");
		String url = Config.getProperty("twtrURL");
		FrameParameters fp = new FrameParameters(1 * 60 * 60 * 1000, 0, 2, 2,
				24, -125, 50, -66);// CHANGED TO 6 MINUTES for testing...change
									// back !!!!*****
		FrameParameters fpFinal = new FrameParameters(5 * 60 * 1000, 0, 0.1,
				0.1, 24, -125, 50, -66);

		// 1. Twitter-Obama
		TwitterWrapper wrapper = new TwitterWrapper(url, "test", fp, true);
		wrapper.setBagOfWords(new String[] { "test", "test2", "test3" });
		STTEmageIterator EIterObama = new STTEmageIterator();
		EIterObama.setSTTPointIterator(wrapper);

		STMerger mergerObama = new STMerger(fpFinal);
		SpatialMapper sp = SpatialMapper.repeat;
		TemporalMapper tp = TemporalMapper.repeat;
		mergerObama.addIterator(EIterObama, sp, tp);
		mergerObama.setMergingExpression("mulED(R0,1)");
		DataProcess process = new DataProcess(mergerObama, EIterObama, wrapper,
				tempDir + "0_Twitter-test", imgBasePath + "0", "0");
		new Thread(process).start();
		
		 * try {
		 * 
		 * long timeWindow = 1000*60*60; //*60*24*2;//the last 2 days long
		 * syncAtMilliSec = 1000;
		 * 
		 * double latUnit = 2; double longUnit = 2; double swLat = 24; double
		 * swLong = -125; double neLat = 50; double neLong = -66;
		 * 
		 * FrameParameters params = new FrameParameters(timeWindow,
		 * syncAtMilliSec, latUnit,longUnit, swLat,swLong , neLat, neLong);
		 * String url = Config.getProperty("twtrURL");
		 * 
		 * //ds1 TwitterWrapper wrapper = new TwitterWrapper(url, "Flu", params,
		 * true); //To get the population wrapper.getPopulation();
		 * 
		 * wrapper.setBagOfWords(new String[]{"obama","president","barack"});
		 * wrapper.run();
		 * 
		 * while (wrapper.hasNext()) { STTPoint a = wrapper.next();
		 * wrapper.log.info(a.latitude + " " + a.longitude + " " + a.value +
		 * " , "); }
		 * 
		 * } catch(Exception e) { log.error(e.getMessage()); }
		 
		System.exit(0);
	}
}
*/
package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.SpatialMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionMapper.TemporalMapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.domain.datasource.emageiterator.STTEmageIterator;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

import static com.eventshop.eventshoplinux.constant.Constant.*;




public class TwitterWrapper extends Wrapper 
{
	
	protected static Log log=LogFactory.getLog(TwitterWrapper.class);
	boolean[][] isPopulated;// = new boolean [13][30];
	private String[] bagOfWords;
	private long[][] sinceID;
	private Twitter twitter;
	private LinkedBlockingQueue<STTPoint> ls;
	private boolean isRunning = false;
	//int [][] numTweetsLoc = new int[13][30];
	// For storing tweets into MySQL
	private boolean saveTweets;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Connection conn;
	private String tableName;
	private String theme;
	
	public TwitterWrapper(String url, String theme, FrameParameters params, boolean saveTweets)
	{
		super(url, theme, params);
		
		if (params.numOfRows == 0  || params.numOfColumns == 0) {
			params.calcRowsColumns();
			log.info("calculated params, rows:"+params.numOfRows+" ,cols:"+params.numOfColumns);
		}
		
		isPopulated = new boolean[params.numOfRows][params.numOfColumns];
		
		// Twitter API v1.1 requires that the request must be authenticated
		ConfigurationBuilder cb = new ConfigurationBuilder();
        /*cb.setDebugEnabled(true)
              .setOAuthConsumerKey("jYjBN7eXBhU0EurloMYGFQ")
              .setOAuthConsumerSecret("lBekOCCDULTkr7JBLPBlvAV9MV4wVkYoKYcEoBimaWY")
              .setOAuthAccessToken("159216395-cJVwHRC2nQdikscMo1Kg6ag1t0vcpmAT3epNZqrB")
              .setOAuthAccessTokenSecret("baXaT7OlavzZwkG6U3WFLMB2OUnUnWq9xRcxXYg1vs");
              */
        cb.setDebugEnabled(true)
        .setOAuthConsumerKey(Config.getProperty("twtConsumerKey"))
        .setOAuthConsumerSecret(Config.getProperty("twtConsumerSecret"))
        .setOAuthAccessToken(Config.getProperty("twtAccessToken"))
        .setOAuthAccessTokenSecret(Config.getProperty("twtAccessTokenSecret"));
//        cb.setUseSSL(true);

		TwitterFactory tf = new TwitterFactory(cb.build());
        twitter = tf.getInstance();
        
		ls = new LinkedBlockingQueue<STTPoint>();
		
		this.saveTweets = saveTweets;		
		if(this.saveTweets)
		{			
			this.tableName = "tbl_" + theme + "_tweet";
			connection();
			createTweetTable(theme);
		}
		
	}

	public static void main(String[] args) {
		String imgBasePath = Config.getProperty("context") + Constant.RESULT_DS;
		String tempDir = Config.getProperty("tempDir");
		String url = Config.getProperty("twtrURL");
		FrameParameters fp = new FrameParameters(1 * 60 * 60 * 1000, 0, 2, 2, 24, -125, 50, -66);//CHANGED TO 6 MINUTES for testing...change back !!!!*****
		FrameParameters fpFinal = new FrameParameters(5 * 60 * 1000, 0, 0.1, 0.1, 24, -125, 50, -66);

		// 1. Twitter-Obama
		TwitterWrapper wrapper = new TwitterWrapper(url, "test", fp, true);
		wrapper.setBagOfWords(new String[]{"test", "test2", "test3"});
		STTEmageIterator EIterObama = new STTEmageIterator();
		EIterObama.setSTTPointIterator(wrapper);

		STMerger mergerObama = new STMerger(fpFinal);
		SpatialMapper sp = SpatialMapper.repeat;
		TemporalMapper tp = TemporalMapper.repeat;
		mergerObama.addIterator(EIterObama, sp, tp);
		mergerObama.setMergingExpression("mulED(R0,1)");
		DataProcess process = new DataProcess(mergerObama, EIterObama, wrapper, tempDir + "0_Twitter-test", imgBasePath + "0", "0");
		new Thread(process).start();
		/*
		try {

			long timeWindow = 1000*60*60; //*60*24*2;//the last 2 days
			long syncAtMilliSec = 1000;

			double latUnit = 2;
			double longUnit = 2;
			double swLat = 24;
			double swLong = -125;
			double neLat = 50;
			double neLong = -66;

			FrameParameters params = new FrameParameters(timeWindow, syncAtMilliSec, latUnit,longUnit, swLat,swLong , neLat, neLong);
			String url = Config.getProperty("twtrURL");

			//ds1
			TwitterWrapper wrapper = new TwitterWrapper(url, "Flu", params, true);
			//To get the population
			wrapper.getPopulation();

			wrapper.setBagOfWords(new String[]{"obama","president","barack"});
			wrapper.run();

			while (wrapper.hasNext())
			{
				STTPoint a = wrapper.next();
				wrapper.log.info(a.latitude + " " + a.longitude + " " + a.value + " , ");
			}http://stackoverflow.com/questions/16946778/how-can-we-create-a-topic-in-kafka-from-the-ide-using-api/23360100#23360100

		} catch(Exception e) {
			log.error(e.getMessage());
		}
		*/
		System.exit(0);
	}

	public void setBagOfWords(String[] words)
	{
		bagOfWords = words;
	}

	public void run()
	{
		isRunning = true;

		/*try {
			getPopulation();
		} catch (IOException e1) {
			log.error(e1.getMessage());
		}*/

		int numofRows=params.getNumOfRows();
		int numOfColumns=params.getNumOfColumns();
		// Initialize the SinceID matrix
		sinceID = new long[numofRows][numOfColumns];
		for(int i = 0; i < numofRows; ++i)
			for(int j = 0; j < numOfColumns; ++j)
				sinceID[i][j] = 0;

		// Setting the bagOfWords for search
		String queryStr = bagOfWords[0];
		for (int i = 1; i < bagOfWords.length; i++)
			queryStr += (" OR " + bagOfWords[i]);

		// Set end time
		long endTime = (long)Math.ceil(System.currentTimeMillis() / params.timeWindow) * params.timeWindow + params.syncAtMilSec;

		while(isRunning)
		{
			STTPoint point;

			Date start = new Date(endTime - params.timeWindow);

			int numQueries=0;
			// Loop for Latitude Longitude blocks
			for(double i = params.swLat; i < params.neLat; i = i+params.latUnit)
			{
				for(double j = params.swLong; j < params.neLong; j = j+params.longUnit)
				{
					if(!isRunning) break;

					int y = (int)Math.ceil(Math.abs((BigDecimal.valueOf(j)).subtract(BigDecimal.valueOf(params.swLong)).divide(BigDecimal.valueOf(params.longUnit)).doubleValue()));
					int x = (int)Math.ceil(Math.abs((BigDecimal.valueOf(i)).subtract(BigDecimal.valueOf(params.swLat)).divide(BigDecimal.valueOf(params.latUnit)).doubleValue()));

					int ret = 0;
					//if (isPopulated[12-x][y])
					/*if (isPopulated[x][y])
					{*/
						ret = doCollection(i+0.5*params.latUnit, j+0.5*params.longUnit, x, y, params.latUnit, start, queryStr);
						numQueries++;
					/*}*/
					point = new STTPoint(ret, start, new Date(endTime), params.latUnit, params.longUnit, i, j, theme);
					ls.add(point);
					}
			}
			log.info("TwitterWrapper NumQueries made:"+numQueries);

			// Sleeping when window is not up yet
			endTime += params.timeWindow;
			while (System.currentTimeMillis() < endTime)
			{
				try {
					Thread.sleep(endTime - System.currentTimeMillis());
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
			}
		}
	}
	
	/*
	 * This method works with twitter4j version 2.0 which supports twitter API 1.0. 
	 * This API 1.0 is not longer supported. 
	 * By: Siripen, 10/02/13
	 */

	/*
	public int doCollection(double lat, double lng, int x, int y, double latUnit, Date date, String queryStr)
	{		
		// Create the query and initialize the parameters
		Query query = new Query();

		query.setGeoCode(new GeoLocation(lat,lng), 60.0*latUnit, Query.KILOMETERS);
		query.setRpp(100);
		query.setQuery(queryStr);
		query.setSinceId(sinceID[x][y]);
		query.setSince(new SimpleDateFormat("yyyy-MM-dd").format(date));

		int count = 0;
		int pageID = 1;
		boolean more = true;
		boolean firstOne = true;
		while(more)
		{
			int cnt = 0;
			query.setPage(pageID);
			pageID++;

			try {
				QueryResult result = twitter.search(query);
				Iterator<Tweet> iterator = result.getTweets().iterator();
				while(iterator.hasNext())
				{
					Tweet tweet = iterator.next();
					// Update sinceID of this block
					if(firstOne)
					{
						sinceID[x][y] = tweet.getId();
						firstOne = false;
					}
					
					if(this.saveTweets)
					{
						GeoLocation tw_loc = tweet.getGeoLocation();
						double p_lat = 0;
						double p_lon = 0;
						if (tw_loc != null)
						{
							p_lat = tw_loc.getLatitude();
							p_lon = tw_loc.getLongitude();				
						}
						insertTweet(lat, lng, x, y, tweet, p_lat, p_lon);
					}
					cnt++;
					count++;
				}

				// No more tweets in the search result
				if (queryStr.indexOf("Cain")>-1 || queryStr.indexOf("Romney")>-1 || queryStr.indexOf("Perry")>-1 )//political tweets
				{
					if(cnt < 100 ) //this page contains less than 100 ...i.e. incomplete. hence no point asking for next page 
					more = false;	
				}
				else
				{ more=false;}
				
				
			} catch (TwitterException e) {
				try {
					if(e.getMessage().contains("500"))
						return 0;
					if(e.getMessage().contains("420")) {
						Thread.sleep(1000*60*20);
					}
				} catch (InterruptedException e2) {
					log.error(e2.getMessage());
				}
				return -1;
			}
		}
		return count;
	}

	*/

	public int doCollection(double lat, double lng, int x, int y, double latUnit, Date date, String queryStr) {
		// Create the query and initialize the parameters
		Query query = new Query();
		query.setCount(100);
		query.setGeoCode(new GeoLocation(lat, lng), 60.0 * latUnit, twitter4j.Query.Unit.km);
		query.setQuery(queryStr);
		query.setSinceId(sinceID[x][y]);
		query.setSince(new SimpleDateFormat("yyyy-MM-dd").format(date));

		boolean firstOne = true;
		int count = 0;
		QueryResult result;
		try {
			do {
				result = twitter.search(query);
				List<Status> tweets = result.getTweets();

				for (Status tweet : tweets) {
					log.info("@" + tweet.getUser().getScreenName() + " - " + tweet.getText());

					// Update sinceID of this block
					if (firstOne) {
						sinceID[x][y] = tweet.getId();
						firstOne = false;
					}
					if (this.saveTweets) {
						GeoLocation tw_loc = tweet.getGeoLocation();
						double p_lat = 0;
						double p_lon = 0;
						if (tw_loc != null) {
							p_lat = tw_loc.getLatitude();
							p_lon = tw_loc.getLongitude();
						}
						insertTweet(lat, lng, x, y, tweet, p_lat, p_lon);
					}
					count++;
				}
			} while ((query = result.nextQuery()) != null && count < 100);
			//log.info("#Tweets @ (lat,long) " + count + " (" + lat + ", " + lng + ")");

		} catch (TwitterException e) {
			log.error("theme:" + theme + ", error: " + e.getMessage());
			try {
				if (e.getMessage().contains("500"))
					return 0;
				if (e.getMessage().contains("420") || e.getMessage().contains("429") || e.getMessage().contains("limit")) {
					Thread.sleep(1000 * 60 * 15);
					log.info("thread sleep for 15 minutes");
				}
			} catch (InterruptedException e2) {
				System.out.println("why error here?" + e2);
				log.error(e2.getMessage());
			}
			return -1;
		}
		return count;
	}

	public boolean stop()
	{
		isRunning = false;
		Thread.currentThread().interrupt();
		return true;
	}

	public STTPoint next()
	{
		try {
			return ls.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	public boolean hasNext()
	{
		return (ls.peek() != null);
	}

	public void remove() {
		ls.remove();
	}

	public void getPopulation() throws IOException {
		BufferedReader br1 = new BufferedReader(new FileReader(Config.getProperty("tempDir") + "visual/newPopulation.txt"));
		String myline = "";
		//double startLat = 24.5;

		int numPop = 0;
		System.out.println("paramssw=="+params.swLat+"params.neLat "+params.neLat+" now params.latUnit "+params.latUnit);
		System.out.println("J LOOP -===params.swLong=="+params.swLong+"params.neLong "+params.neLong+" now params.longUnit "+params.longUnit);
		for(double i = params.swLat; i < params.neLat; i+=params.latUnit) {
			myline=br1.readLine();
			StringTokenizer vals = new StringTokenizer(myline, " ,");
			log.info("size of vals:"+vals.countTokens());

			for(double j = params.swLong; j <params.neLong ;j+=params.longUnit) {
				String val= vals.nextToken();
				if (val.compareTo("1")==0) {
					isPopulated[(int) ((i-params.swLat)/params.latUnit)][(int) ((j-params.swLong)/params.longUnit)] = true;
					numPop++;
				}
				else
					isPopulated[(int) ((i-params.swLat)/params.latUnit)][(int) ((j-params.swLong)/params.longUnit)] = false;
			}
		}

		log.info("numPop:"+numPop);

		//need to change this code and remove hard coded values
//		for(int i = 0; i < 13; i++)
//		{
//			for(int j = 0; j < 30; j++)
//			{
//				numTweetsLoc[i][j] =0;
//			}
//		}
	}

	/*
	public void setPopulation() throws IOException
	{

		int []vals = new int[13*30];

		for(int i = 0; i < 13; i++)
		{
			for(int j = 0; j < 30; j++)
			{
				vals[i*j] = numTweetsLoc[i][j];
			}
		}
		Arrays.sort(vals);

		int cutOffVal = vals[vals.length-40];
		cutOffVal = 5;
		log.info("cutoff is :"+ cutOffVal);
		FileWriter f0 = new FileWriter("newPopulation.txt");

		for(int i = 0; i < 13; i++)
		{
			for(int j = 0; j < 30; j++)
			{
				if (j<29)
				{
					if(numTweetsLoc[i][j]>=cutOffVal)
					{
						f0.write(" 1 ,");
					}
					else
					{
						f0.write(" 0 ,");
					}
				}
				else
				{
					if(numTweetsLoc[i][j]>=cutOffVal)
					{
						f0.write(" 1 ");
					}
					else
					{ f0.write(" 0 ");
					}

				}

			}
			f0.write("\n");
		}
		f0.close();
	}

	 */
	void createTweetTable(String theme)
	{
		String query1="CREATE TABLE IF NOT EXISTS "+tableName+" ( " +
				"id INT AUTO_INCREMENT," +
				"tweetid BIGINT NOT NULL," +
				"latitude DOUBLE NOT NULL,"+
				"longitude DOUBLE NOT NULL,"+
				"date DATETIME NOT NULL,"+
				"text TEXT NOT NULL,"+
			    "p_latitude DOUBLE NOT NULL,"+
			    "p_longitude DOUBLE NOT NULL,"+
			    "userName TEXT NOT NULL,"+
			    "PRIMARY KEY(id));\n\n" ;
			String query2 = "CREATE INDEX idx_"+theme+"_Latitude ON "+ tableName+"(latitude);\n";
			String query3 = "CREATE INDEX idx_"+theme+"_Longitude ON "+ tableName+"(longitude);\n" ;
			String query4 = "CREATE INDEX idx_"+theme+"_Date ON "+tableName+"(date);";


		try {
			log.info(query1);
			Statement statement = conn.createStatement();
			statement.execute(query1);
			statement.execute(query2);
			statement.execute(query3);
			statement.execute(query4);
		} catch (SQLException e) {
			connection();
		}
	}
	
	
	/*
	 * This method works with twitter4j version 2.0 which supports twitter API 1.0. 
	 * This API 1.0 is not longer supported. 
	 * By: Siripen, 10/02/13
	 */
	/*
	public int insertTweet(double lat, double lng, int x, int y, Tweet tweet, double p_lat, double p_lon)
	{
		String text = textFilter(tweet.getText());
		if(text == null) return -1;

		String date = formatter.format(tweet.getCreatedAt());
		String sql = "INSERT INTO " + tableName + " VALUES (NULL, " +
			tweet.getId() + ", " + lat + ", " + lng + ", '" + 
			date + "', '" + text + "',"+p_lat+","+p_lon+", '"+textFilter(tweet.getFromUser())+"')";
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			log.error(sql);
			log.error(e.getMessage());
			connection();
		}
		return 0;
	}
	*/

	public int insertTweet(double lat, double lng, int x, int y, Status tweet, double p_lat, double p_lon)
	{
		String text = textFilter(tweet.getText());
		if(text == null) return -1;

		String date = formatter.format(tweet.getCreatedAt());
		String sql = "INSERT INTO " + tableName + " VALUES (NULL, " +
				tweet.getId() + ", " + lat + ", " + lng + ", '" +
				date + "', '" + text + "'," + p_lat + "," + p_lon + ", '" + textFilter(tweet.getUser().getName()) + "')";
		try {
			Statement statement = conn.createStatement();
			statement.execute(sql);
		} catch (SQLException e) {
			log.error(sql);
			log.error(e.getMessage());
			connection();
		}
		return 0;
	}
	
	public String textFilter(String text)
	{
		if(text == null || text.length() == 0) return null;

		for(int i = 0; i < text.length(); i++)
			if(Character.UnicodeBlock.of(text.charAt(i)) != Character.UnicodeBlock.BASIC_LATIN)
			{
				return null;
			}

		text = text.replaceAll("\t|\r|\n|\r\n|\f", " ");
		text = text.replaceAll("'", "");
		text = text.replace('\\', ' ');
		return text;
	}
	
	public Connection connection()
	{
	  try {
		  Class.forName(DRIVER_NAME);
		    String url = Config.getProperty(DB_URL);
		    String userName = Config.getProperty(USR_NAME);
		    String pwd = Config.getProperty(PASSWORD);
		    conn = DriverManager.getConnection(url,userName,pwd);
	    // log.info("Connected to Drishti!!");

	  } catch(Exception e) {
	   // log.error(e.getMessage());
	   // log.error("COULD NOT Connect to Drishti!!");
	  }
	      return conn;
	}
}

