package com.eventshop.eventshoplinux.servlets;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.SampleModel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import com.eventshop.eventshoplinux.util.commonUtil.Config;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class AlertProcessor {
	protected Log log = LogFactory.getLog(this.getClass().getName());
	private Connection conn;

	// Twitter4j
	private Twitter twitter;

	FrameParameters params;
	String probEmagePath;
	String solEmagePath;
	String SocialDataSource;

	double minValProb;
	double maxValProb;
	double minValSol;
	double maxValSol;
	String baseMessage;
	String theme;

	String usersTweeted = "";

	public AlertProcessor(String userDS, String srcQuery, String nearestDS,
			double minValProb2, double maxValProb2, double minValSol2,
			double maxValSol2, String msg, RegisterServlet registerServlet) {
		try {
			this.SocialDataSource = registerServlet.context
					+ Constant.RESULT_DS + userDS + "_before.png";

			minValProb = minValProb2;
			maxValProb = maxValProb2;
			minValSol = minValSol2;
			maxValSol = maxValSol2;
			baseMessage = msg;

			if (userDS.contains("ds")) {
				// int ID = Integer.valueOf(userDS.substring(2));
				for (int i = 0; i < registerServlet.sources.size(); ++i) {
					if ((registerServlet.sources.get(i)).srcID
							.equals(userDS.substring(2))) {
						this.theme = registerServlet.sources
								.get(i).srcTheme;
					}
				}
			}

			if (srcQuery.contains("ds")) {
				int ID = Integer.valueOf(srcQuery.substring(2));
				for (int i = 0; i < registerServlet.sources.size(); ++i) {
					if ((registerServlet.sources.get(i)).srcID
							.equals(userDS.substring(2))) {
						this.params = registerServlet.sources
								.get(i).finalParam;
						this.probEmagePath = registerServlet.context
								+ Constant.RESULT_DS + srcQuery + "_before.png";
					}
				}
			}

			if (srcQuery.contains("Q")) {
				if (registerServlet.queryProcesses.containsKey(srcQuery)) {
					params = registerServlet.queryProcesses.get(srcQuery).finalResolution;
					this.probEmagePath = registerServlet.context
							+ Constant.RESULT_Q
							+ registerServlet.queryProcesses
									.get(srcQuery).outputName + "_before.png";

				}
			}

			if (nearestDS.contains("ds")) {

				for (int i = 0; i < registerServlet.sources.size(); ++i) {
					if (registerServlet.sources.get(i).srcID
							.equals(nearestDS.substring(2))) {
						this.params = registerServlet.sources
								.get(i).finalParam;
						this.solEmagePath = registerServlet.context
								+ Constant.RESULT_DS + nearestDS
								+ "_before.png";
					}
				}
			}

			if (nearestDS.contains("Q")) {

				if (registerServlet.queryProcesses.containsKey(nearestDS)) {
					params = registerServlet.queryProcesses.get(nearestDS).finalResolution;
					this.solEmagePath = registerServlet.context
							+ Constant.RESULT_Q
							+ registerServlet.queryProcesses
									.get(nearestDS).outputName + "_before.png";

				}
			}

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)

					// Ev3ntResearcher
					.setOAuthConsumerKey("Fz4TfPYs1utIX3KDBl0TUMbLu")
					.setOAuthConsumerSecret(
							"1Jl7FTW0Ci2iAb5PFSTwlUIaVklVddOgMGQzT4s2IIcVNxUWCq")
					.setOAuthAccessToken(
							"2997971480-0T4Mr6XzbYkBUCHDjcPGBLJSsuyfzdH9THIddAY")
					.setOAuthAccessTokenSecret(
							"wYhvXAHO0mkie5grSuR3zZqEVpBIZcNXpvsuCdQUh5wpk");

			// Hurricane Alert System

			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();

			connection();
		} catch (Exception e) {
			writeToFile(e.getMessage());
			log.error(e.getMessage());
		}
	}

	public void DoAlerts() {
		writeToFile("[" + System.currentTimeMillis() + "] DO ALERTS!!! ");

		PlanarImage probEmage = JAI.create("fileload", probEmagePath);// dummy
																		// loaded.
																		// date
																		// to be
																		// edited.
		double[][] probDataGrid = PlanarImage2DataGrid(probEmage);

		int lengthProb = probDataGrid.length;
		int widthProb = probDataGrid[0].length;

		// filtering based on the range
		for (int i = 0; i < lengthProb; i++) {
			for (int j = 0; j < widthProb; j++) {
				if (probDataGrid[i][j] > minValProb
						&& probDataGrid[i][j] < maxValProb)
					probDataGrid[i][j] = 1;
				else
					probDataGrid[i][j] = 0;
			}
		}

		PlanarImage solEmage = JAI.create("fileload", solEmagePath);// dummy
																	// loaded.
																	// date to
																	// be
																	// edited.
		double[][] solDataGrid = PlanarImage2DataGrid(solEmage);

		int lengthsol = solDataGrid.length;
		int widthsol = solDataGrid[0].length;

		if (lengthsol != lengthProb || widthsol != widthProb) {
			log.info("****ERROR*** The size of prob and soln emages do not match");
			writeToFile("****ERROR*** The size of prob and soln emages do not match");
			return;
		}

		// filtering based on the range
		for (int i = 0; i < lengthsol; i++) {
			for (int j = 0; j < widthsol; j++) {
				if (solDataGrid[i][j] > minValSol
						&& solDataGrid[i][j] < maxValSol)
					solDataGrid[i][j] = 1;
				else
					solDataGrid[i][j] = 0;
			}
		}

		// finding the nearest 'safe' location for
		int maxDistPoss = (int) Math.sqrt(lengthProb * lengthProb + widthProb
				* widthProb);
		int ymin, ymax, xmin, xmax;
		boolean neighbFound;
		for (int i = 0; i < lengthProb; i = i + (int) (2 / params.latUnit)) {
			for (int j = 0; j < widthProb; j = j + (int) (2 / params.longUnit)) {
				if (probDataGrid[i][j] == 1) {
					Point npoint = new Point(-999, -999);
					neighbFound = false;
					for (int step = 0; step < Math.min(maxDistPoss, 40)
							&& !neighbFound; step++) {
						xmin = Math.max(0, i - step);
						ymin = Math.max(0, j - step);
						xmax = Math.min(lengthProb - 1, i + step);
						ymax = Math.min(widthProb - 1, j + step);

						// north rim
						for (int i1 = xmin; i1 <= xmin; i1++) {
							for (int j1 = ymin; j1 < ymax; j1++) {
								if (solDataGrid[i1][j1] == 1) {
									neighbFound = true;
									npoint = new Point(i1, j1);
								}

							}
						}
						// south rim
						for (int i1 = xmax; i1 <= xmax; i1++) {
							for (int j1 = ymin; j1 < ymax; j1++) {
								if (solDataGrid[i1][j1] == 1) {
									neighbFound = true;
									npoint = new Point(i1, j1);
								}

							}
						}
						// west rim
						for (int i1 = xmin; i1 < xmax; i1++) {
							for (int j1 = ymin; j1 <= ymin; j1++) {
								if (solDataGrid[i1][j1] == 1) {
									neighbFound = true;
									npoint = new Point(i1, j1);
								}

							}
						}
						// east rim
						for (int i1 = xmin; i1 < xmax; i1++) {
							for (int j1 = ymax; j1 <= ymax; j1++) {
								if (solDataGrid[i1][j1] == 1) {
									neighbFound = true;
									npoint = new Point(i1, j1);
								}
							}
						}
					}// end of for for loop for finding neighs... i,e
						// neighbFound=true now OR npoint =(-999,-999)

					writeToFile(" neighbFound=" + neighbFound + ", npoint="
							+ npoint.x + npoint.y + ", " + minValSol + ", "
							+ maxValSol);

					log.info("before sending msgs!");
					String allMsg = "";
					if (npoint.x != -999 && npoint.y != -999) {
						// Form the tweet to send out
						Point2D nPointGeoNN = pixel2LatLong(npoint.x, npoint.y,
								params);
						String tweetText = "";
						Point2D nPointGeoUser = pixel2LatLong(i, j, params);
						writeToFile("============" + tweetText
								+ "==================");

						Calendar calendar = Calendar.getInstance();
						// add days to current date using Calendar.add method
						calendar.add(Calendar.DATE, -30);
						SimpleDateFormat dateFormat = new SimpleDateFormat(
								"yyyy-MM-dd");
						String startDate = dateFormat
								.format(calendar.getTime());

						int nPointGeoLat = (int) params.swLat
								+ (int) (Math
										.floor((nPointGeoUser.getX() - params.swLat) / 2) * 2)
								+ 1;// THIS IS HARDCODED WITH ASSUMPTION of
									// TWItter initRes ==2 units
						int nPointGeoLong = (int) params.swLong
								+ (int) (Math
										.floor((nPointGeoUser.getY() - params.swLong) / 2) * 2)
								+ 1;

						String userNames = "";
						try {
							userNames = search(startDate, nPointGeoLat + "",
									nPointGeoLong + "", theme);
						} catch (Exception e) {
							writeToFile(e.getMessage());
							log.error(e.getMessage());
						}
						if (userNames != null && userNames.compareTo("") != 0) {
							StringTokenizer users = new StringTokenizer(
									userNames, " ,");
							int numUsers = Math.min(3, users.countTokens());// limiting
																			// to
																			// 5
																			// per
																			// lat
																			// long
																			// block
							for (int un = 0; un < numUsers; un++) {

								String addressFound = getShelterAddr(
										nPointGeoNN.getX(), +nPointGeoNN.getY());
								if (un == 0 && addressFound.length() > 10)
									tweetText = "(TEST ONLY): Be safe from hurricane http://goo.gl/2l4OT. Shelter near you: "
											+ addressFound;
								if (un == 1 && addressFound.length() > 10)
									tweetText = "Be safe from hurricane http://goo.gl/2l4OT .(TEST ONLY).Shelter near you: "
											+ addressFound;
								if (un == 2 && addressFound.length() > 10)
									tweetText = "Be safe! http://goo.gl/2l4OT .(TEST ONLY). Hurricane shelter near you: "
											+ addressFound;
								if (un == 3 && addressFound.length() > 10)
									tweetText = "(TEST ONLY) Be safe in Hurricane! http://goo.gl/2l4OT .Hurricane shelter near you: "
											+ addressFound;
								if (un == 4 && addressFound.length() > 10)
									tweetText = "Hurricane shelter near you: "
											+ addressFound
											+ "(TEST ONLY). http://goo.gl/2l4OT Be safe in Hurricane!";

								// For each user in each effected location; send
								// out the tweet

								// send out tweet for each user
								String userid = users.nextToken();
								while ((usersTweeted.contains(userid))
										&& users.hasMoreTokens()) {
									userid = users.nextToken();
								}
								// for testing
								String tweetOut = "@ " + userid + " "
										+ tweetText;
								if (tweetOut.length() > 140) {
									tweetOut = tweetOut.substring(0, 130);
								}
								allMsg += tweetOut + "-------------";
								Status status = null;
								usersTweeted = usersTweeted + " , " + userid;
								try {
									if (addressFound.length() > 10)
										status = twitter.updateStatus(tweetOut);
									Thread.sleep(100);
								} catch (Exception e) {
									log.error(e.getMessage());
									writeToFile(e.getMessage());
								}
								log.error("Successfully updated the status to ["
										+ status.getText() + "].");
							}
							try {
								Thread.sleep(60000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								log.error(e.getMessage());
							}

						} else {
							log.info("No users found for location:"
									+ nPointGeoLat + "," + nPointGeoLong);
							allMsg += " No users found for location:"
									+ nPointGeoLat + "," + nPointGeoLong
									+ "++++++++++++++++++ ";
						}
						writeToFile("All messages: " + allMsg);

					}// else no valid near neighbor locations found for sending
						// alerts
					else {
						writeToFile("no valid near neighbor locations found for sending alerts ");
					}
				}
			}
		}
	}

	public void writeToFile(String msg) {
		try {
			// Create file /
			FileWriter fstream = new FileWriter(Config.getProperty("tempDir")
					+ "file_log.txt", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(msg + "\n");
			out.close();
		} catch (Exception e) {// Catch exception if any
			log.error("Error: " + e.getMessage());
		}
	}

	public String getShelterAddr(double lat, double lng) {
		double range = 3.5; // get shelter's address with in 5 lat/long-unit
		double minLat = lat - range;
		double maxLat = lat + range;
		double minLng = lng - range;
		double maxLng = lng + range;

		String sql = "select addr1, city, state, zip from tbl_shelters_sandy where ";

		sql += " lat>=" + minLat + " and lat <= " + maxLat + " and lng >= "
				+ minLng + " and lng <= " + maxLng + " limit 1";

		Statement st;
		String address = "";
		try {
			st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			if (rs.next()) {
				address = rs.getString("addr1") + ", " + rs.getString("city")
						+ ", " + rs.getString("state") + " "
						+ rs.getString("zip");
			} else {
				address = "lat:" + lat + ",lng:" + lng;
				address = "";// Dont want to send out such tweets

			}
		} catch (SQLException e) {
			log.error(e.getMessage());
		}
		log.error("sql: " + sql + ", Address: " + address);
		if (address.length() > 100)
			address.substring(0, 99);
		return address;
	}

	public String search(String dateStart, String Lat, String Lon, String theme)
			throws Exception {
		String result = "";
		String sql;
		String dateCond = "(date >= \"" + dateStart + "\")";
		String locCond = "latitude='" + Lat + "'" + "AND longitude ='" + Lon
				+ "'";

		sql = "SELECT userName FROM tbl_" + theme + "_tweet" + " WHERE "
				+ dateCond + " AND " + locCond;
		log.info(sql);
		writeToFile(sql);
		try {
			Statement st = conn.createStatement();
			ResultSet rs = st.executeQuery(sql);

			while (rs.next()) {
				result += rs.getString(1) + ", ";
			}
		} catch (SQLException e) {
			log.error(e.getMessage());
			connection();
			writeToFile(" Error " + e.getMessage());
		}
		writeToFile(" result usernames: " + result + " ");
		return result;
	}

	public Point2D pixel2LatLong(int x, int y, FrameParameters params) {
		double lat = params.neLat - (x * params.latUnit);
		double lng = (params.swLong + y * params.longUnit);
		return new Point2D.Double(lat, lng);
	}

	public void connection() {
		try {
			Class.forName(Config.getProperty("driverName"));
			String url = Config.getProperty("DBURL");
			conn = DriverManager.getConnection(url,
					Config.getProperty("DBusr"), Config.getProperty("DBpwd"));

			log.info("Connected to Drishti!!");
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error("COULD NOT Connect to Drishti!!");
		}
	}

	public double[][] PlanarImage2DataGrid(PlanarImage img) {
		double[][] BucketArray = new double[img.getHeight()][img.getWidth()];

		int width = img.getWidth();
		int height = img.getHeight();

		// Get the number of bands on the image.
		SampleModel sm = img.getSampleModel();
		int nbands = sm.getNumBands();

		// We assume that we can get the pixels values in a integer array.
		double[] pixel = new double[nbands];

		// Get an iterator for the image.
		RandomIter iterator = RandomIterFactory.create(img, null);

		log.info("Ignoring any binning. Going with gray values");
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				int sumChannels = 0;

				iterator.getPixel(j, i, pixel);
				for (int j2 = 0; j2 < nbands; j2++) {
					sumChannels += pixel[j2];
				}
				BucketArray[i][j] = (sumChannels) / (nbands);

			}
		}

		return BucketArray;
	}

	public static void main(String[] args) {
		
	}
}
