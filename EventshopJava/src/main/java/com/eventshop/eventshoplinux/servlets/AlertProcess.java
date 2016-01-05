package com.eventshop.eventshoplinux.servlets;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.model.AlertResponse;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.typesafe.config.ConfigException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.ALERT_INTERVAL;



/**
 * Created by aravindh on 5/1/15.
 */

//import com.eventshop.eventshoplinux.servlets.DataSource;

/**
 *
 * This class is used to process Alerts.
 *
 * constructors
 *
 * 1.  AlertProcess(int id1, String alertSrcId1, String srcType, String alertMin1, String alertMax1)
 * 		This is called for alerts with out solution
 *
 * 2 . AlertProcess(int id1, String alertSrcId1, String srcType, String alertMin1, String alertMax1, String alertSolId1, String solSrcType, String alertSolMin1, String alertSolMax1, String boundingbox)
 * 		This is called for alerts with solution
 *
 * Methods
 *
 * run()
 * This method is used to spawn a new alert. This is where alert type is differentiated and excution happens based on the alert type.
 * This method is obsolete after moving to Akka based scheduling.
 *
 * stop()
 * This method is used to kill the thread.
 * This method is obsolete after moving to Akka based scheduling.
 *
 * public List<AlertResponse> checkAlertWithSolution(JsonObject jSrcObj, JsonObject jSolObj, boolean enableQueue, int alertMin, int alertMax,int alertSolMin, int alertSolMax, String message, String boundingbox)
 * This method is used to find alerts which requires solutions.
 * This is called directly from akka actors.
 *
 * public List<AlertResponse> checkAlertWithoutSolution(JsonObject emage, String alertSrcType, boolean enableQueue, int minLimit, int maxLimit, String message, String boundingbox) throws IOException
 * This method is used to find areas which requires alerts. No solutions is provided with this method.
 * This is called directly from akka actors
 *
 * private double[][] getGrid(JsonArray emage, int row, int col)
 * This method converts a 1D jsonArray of values to 2D matrix.
 *
 * private String getLatLongFromGrid(int x, int y,double latUnit, double longUnit, double neLat, double swLong)
 * This method is used to get latitude and longitude from a 2D array.
 *
 * private int[][] calculateRange(double[][] grid, int min, int max)
 * This method is used to convert a 2D matrix with values to a 2D matrix with just 0s and 1s where 1 stands for area of interest (could be a alert zone or safe zone based on what range is specified) and 0s are ignorable area.
 *
 * private String getNearestSolution(int pX, int pY, int[][] solGrid, int maxDist) {
 * This method is used to find the nearest solution for a particular alert area. Algorithm to find shortest safe path goes here.
 *
 * private double[][] getsubBoundingBox(JsonArray image, String[] bb, JsonObject emage)
 * This method is used to cut area which requires alert, from the entire data source.
 *
 * private void pushAlert(int id, String latLong, double latUnit, double longUnit, double currVal) throws IOException
 * This method is used to push alerts to rabbitmq queue.
 *
 * 	private String getLatLong(int currVal, float neLat, float swlong, int rowNum,int colNum, float latUnit, float longUnit)
 * 	This method is used to get Latitude and longitude of a given point
 *
 */
public class AlertProcess implements Runnable {
	private final static Logger LOGGER = LoggerFactory.getLogger(AlertProcess.class);
	protected Log log = LogFactory.getLog(this.getClass().getName());
	boolean isRunning;
	int id;
	String alertSrcId;
	int alertMin;
	int alertMax;
	String alertSrcType;
	String alertEmagePath;
	List<Double> Image = new ArrayList<Double>();
	AlertDAO alert;
	// RabbitMQ Connections
	ConnectionFactory factory;
	Channel channel;
	Connection connection;
	//END
	String alertSolId;
	int alertSolMin;
	int alertSolMax;
	String alertSolType;
	String solEmagePath;
	boolean enableQueue = false;
	String boundingbox;

	public AlertProcess() {

	}

	/**
	 *
	 * @param id1
	 * Alert ID should be passed here.
	 * @param alertSrcId1
	 * Source ID should be passed here. It could be a DataSource ID or Query ID.
	 * @param srcType
	 * Source Type should be passed here. It specifies if the Source is a DataSource or a Query
	 * @param alertMin1
	 * Minimum value of a alert should be passed here.
	 * @param alertMax1
	 * Maximum value of a alert should be passed here.
	 */
	public AlertProcess(int id1, String alertSrcId1, String srcType, String alertMin1, String alertMax1) {
		id = id1;
		isRunning = false;
		alertSrcId = alertSrcId1;
		alertMin = Integer.parseInt(alertMin1);
		alertMax = Integer.parseInt(alertMax1);
		alertSrcType=srcType;
		if (alertSrcType.equals("Q")){
			alertEmagePath = Config.getProperty("queryJsonLoc") + "Q" + alertSrcId + ".json";

		}else if (alertSrcType.equals("ds")){
			alertEmagePath = Config.getProperty("datasourceJsonLoc")+alertSrcId+".json";
		}
		factory = new ConnectionFactory();
	    factory.setHost("rabbitmqHOSt");
	    try {
			connection = factory.newConnection();
			channel= connection.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @param id1
	 * Alert ID should be passed here.
	 * @param alertSrcId1
	 * Source ID should be passed here. It could be a DataSource ID or Query ID.
	 * @param srcType
	 * Source Type should be passed here. It specifies if the Source is a DataSource or a Query
	 * @param alertMin1
	 * Minimum value of a alert should be passed here.
	 * @param alertMax1
	 * Maximum value of a alert should be passed here.
	 * @param alertSolId1
	 * Solution Source ID should be passed here. It could be a DataSource ID or a Query ID
	 * @param solSrcType
	 * Solution source Type should be passed here. It specifies if the Source is a DataSource or a Query
	 * @param alertSolMin1
	 * Minimum value of a safe zone should be passed here.
	 * @param alertSolMax1
	 * maximum value of safe zone should be passed here
	 * @param boundingbox
	 * Area which needs an alert should be passed here. This should be a string with comma seperated values containing swlat, swlong, nelat, nelong
	 */
	public AlertProcess(int id1, String alertSrcId1, String srcType, String alertMin1, String alertMax1, String alertSolId1, String solSrcType, String alertSolMin1, String alertSolMax1, String boundingbox){

		id = id1;
		isRunning = false;
		alertSrcId = alertSrcId1;
		alertMin = Integer.parseInt(alertMin1);
		alertMax = Integer.parseInt(alertMax1);
		alertSrcType=srcType;
		if (alertSrcType.equals("Q")){
			alertEmagePath = Config.getProperty("queryJsonLoc") + "Q" + alertSrcId + ".json";

		}else if (alertSrcType.equals("ds")){
			alertEmagePath = Config.getProperty("datasourceJsonLoc") + alertSrcId + ".json";
		}

		alertSolId= alertSolId1;
		alertSolMin = Integer.parseInt(alertSolMin1);
		alertSolMax = Integer.parseInt(alertSolMax1);
		alertSolType = solSrcType;

		if(alertSolType.equals("Q")){
			solEmagePath = Config.getProperty("queryJsonLoc") + "Q" + alertSolId + ".json";
		}
		else if(alertSolType.equals("ds")){
			solEmagePath = Config.getProperty("datasourceJsonLoc")+alertSolId+".json";
		}
		boundingbox=this.boundingbox;

		factory = new ConnectionFactory();
	    factory.setHost("rabbitmqHOSt");
	    try {
			connection = factory.newConnection();
			channel= connection.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		//Image = getImageArray(alertEmagePath);
		boolean alertStatus;
		if (alertSolId == null){
			isRunning=true;
			alert = new AlertDAO();
			alert.activateAlert(id);
			while(isRunning){
				JsonParser parser = new JsonParser();
				Object obj;
				try {
					obj = parser.parse(new FileReader(alertEmagePath));
					JsonObject jObj = (JsonObject) obj;
					//AlertProcess j = new AlertProcess();
					String message = null;
					checkAlertWithoutSolution(jObj, alertSrcType, enableQueue, alertMin, alertMax, message,boundingbox);
					Thread.currentThread();
					Thread.sleep(Integer.parseInt(Config.getProperty(ALERT_INTERVAL)));

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alertStatus= alert.isActive(id);
				LOGGER.info("Returned "+ alertStatus);
				if (alertStatus==false){
					isRunning=false;
					LOGGER.info("Running set to false");
				}
			}
		}
		else{
			isRunning=true;
			alert = new AlertDAO();
			alert.activateAlert(id);
			JsonParser parser = new JsonParser();
			//Object srcObj;
			//Object solObj;
			JsonObject jSrcObj;
			JsonObject jSolObj;
			while(isRunning){

				try {
					jSrcObj = (JsonObject) parser.parse(new FileReader(alertEmagePath));
					jSolObj = (JsonObject) parser.parse(new FileReader(solEmagePath));
					String message= null;
					checkAlertWithSolution(jSrcObj, jSolObj, enableQueue, alertMin, alertMax, alertSolMin, alertSolMax, message, boundingbox);
					Thread.currentThread();
					Thread.sleep(Integer.parseInt(Config.getProperty(ALERT_INTERVAL)));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				alertStatus= alert.isActive(id);
				LOGGER.info("Returned "+ alertStatus);
				if (alertStatus==false){
					isRunning=false;
					LOGGER.info("Running set to false");
				}
			}
		}
	}

	/**
	 *
	 * @param emage
	 * A json array which holds all the values of a particular area.
	 * @param row
	 * specify the num of rows the return GRID should have.
	 * @param col
	 * specify the num of columns the return GRID should have.
	 * @return
	 */
	private double[][] getGrid(JsonArray emage, int row, int col) {

		double[][] srcGrid = new double[row][col];
		int x,y;

		try{
			for(int i=0;i<emage.size();i++){
				x=(i)/col;
				//incorrect formula below............
				y=(i)%col;
				if (emage.get(i).toString().equals("NaN")){
					srcGrid[x][y]=0;
				}else{
					srcGrid[x][y] = Float.parseFloat(emage.get(i).toString());
				}

			}
			for(int i=0; i<srcGrid.length;i++){
				for(int j=0;j<srcGrid[0].length;j++){
					LOGGER.debug(srcGrid[i][j]+" ");
				}
				LOGGER.debug("\nrowNum:"+i);
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
			return srcGrid;
		// TODO Auto-generated method stub

	}


	public void stop() {
			isRunning = false;
			Thread.currentThread().interrupt();
	}

	/**
	 *
	 * @param jSrcObj
	 * A source object for which alert is required. It should be a JsonObject which has a emage of a datasource/query.
	 * @param jSolObj
	 * A source object which has solution. It should be a JsonObject which has a emage of a datasource/query
	 * @param enableQueue
	 * Boolean type  which specifies if the alert has to be pushed to a rabbitmq queue.
	 * @param alertMin
	 * Minimum value of a alert should be passed here.
	 * @param alertMax
	 * Maximum value of a alert should be passed here.
	 * @param alertSolMin
	 * Minimum value of a solution should be passed here.
	 * @param alertSolMax
	 * Maximum value of a alert should be passed here.
	 * @param message
	 * A string which has a custom message which can sent as a response to a end point or as a tweet.
	 * @param boundingbox
	 * Area which needs an alert should be passed here. This should be a string with comma seperated values containing swlat, swlong, nelat, nelong
	 * @return
	 */
	public List<AlertResponse> checkAlertWithSolution(JsonObject jSrcObj, JsonObject jSolObj, boolean enableQueue, int alertMin, int alertMax,
													  int alertSolMin, int alertSolMax, String message, String boundingbox) {
		// TODO Auto-generated method stub
		GeoApiContext context = new GeoApiContext().setApiKey(Config.getProperty("googleAPIKey2"));
		GeocodingResult[] results;
		GeocodingResult[] solutionResults;

		List<AlertResponse> alertResponseList = new ArrayList<AlertResponse>();
		JsonArray  srcEmage;
		JsonArray solEmage;
		int srcRow,srcCol, solRow, solCol;
		double latUnit, longUnit;
		double neLat,swLong;

		String[] bb= boundingbox.split(",");


		String solution;
		String[] solPoints = new String[2];
		try{
			srcEmage = jSrcObj.getAsJsonArray("image");
			solEmage = jSolObj.getAsJsonArray("image");
			srcRow= Integer.parseInt(jSrcObj.get("row").toString());
			srcCol= Integer.parseInt(jSrcObj.get("col").toString());
			solRow= Integer.parseInt(jSolObj.get("row").toString());
			solCol= Integer.parseInt(jSolObj.get("col").toString());
			double[][] srcGrid, solGrid;
			if (boundingbox.isEmpty() || boundingbox.equals(null)) {
				srcGrid=getGrid(srcEmage,srcRow,srcCol);
				solGrid=getGrid(solEmage,solRow,solCol);
				neLat=Double.parseDouble(jSrcObj.get("neLat").toString());
				swLong=Double.parseDouble(jSrcObj.get("swLong").toString());
				latUnit=Double.parseDouble(jSrcObj.get("latUnit").toString());
				longUnit=Double.parseDouble(jSrcObj.get("longUnit").toString());
			}
			else {
				srcGrid=getsubBoundingBox(srcEmage,bb,jSrcObj );
				solGrid=getsubBoundingBox(solEmage,bb,jSolObj);
				neLat=Double.parseDouble(bb[2]);
				swLong=Double.parseDouble(bb[1]);
				latUnit=Double.parseDouble(jSrcObj.get("latUnit").toString());
				longUnit=Double.parseDouble(jSrcObj.get("longUnit").toString());
			}

			int maxDist = Math.min(((int) Math.sqrt(srcGrid.length * srcGrid.length + srcGrid[0].length * srcGrid[0].length)),40);

			int[][] probGrid = calculateRange(srcGrid,alertMin,alertMax);
			int[][] solProbGrid = calculateRange(solGrid,alertSolMin,alertSolMax);
			String srcLatLong,solLatLong;
			LOGGER.debug("Checking Problem Grid...");
			LOGGER.debug("Length="+probGrid.length);
			LOGGER.debug("Width="+probGrid[0].length);
			for (int i=0;i<probGrid.length;i++) {
				for (int j = 0; j < probGrid[0].length; j++) {
					results = null;
					solutionResults = null;
					if (probGrid[i][j] == 1) {
						LOGGER.debug("Problem found at " + i + "," + j);
						LOGGER.debug("getting lat and long of the problem");
						srcLatLong = getLatLongFromGrid(i, j, latUnit, longUnit, neLat, swLong);
						LOGGER.debug("Trying to find the nearest solution");
						solution = getNearestSolution(i, j, solProbGrid, maxDist);
						if (solution.equals("NotFound")) {
							solLatLong = solution;
						} else {
							solPoints = solution.split(":");
							solLatLong = getLatLongFromGrid(Integer.parseInt(solPoints[0]), Integer.parseInt(solPoints[1]), latUnit, longUnit, neLat, swLong);
						}
						if (enableQueue) {
							pushAlert(id, srcLatLong + "," + solLatLong, latUnit, longUnit, (double) srcGrid[i][j]);
						} else {
							results = GeocodingApi.reverseGeocode(context,
									new LatLng(Double.valueOf(srcLatLong.substring(0, srcLatLong.indexOf(":"))), Double.valueOf(srcLatLong.substring((srcLatLong.indexOf(":") + 1))))).await();

							solutionResults = GeocodingApi.reverseGeocode(context,
									new LatLng(Double.valueOf(srcLatLong.substring(0, solLatLong.indexOf(":"))), Double.valueOf(solLatLong.substring((solLatLong.indexOf(":") + 1))))).await();


							AlertResponse alertResponse = null;
							if (results.length > 0) {
								alertResponse = new AlertResponse(srcLatLong
										, srcGrid[i][j]
										, results[0].formattedAddress
										, solLatLong
										, message);
								if (solutionResults != null) {
									alertResponse.setSolutionGeoAddress(solutionResults[0].formattedAddress);
								}
								alertResponseList.add(alertResponse);


							} else {
								alertResponse = new AlertResponse(srcLatLong
										, srcGrid[i][j]
										, null
										, solLatLong
										, message);
								if (solutionResults != null) {
									alertResponse.setSolutionGeoAddress(solutionResults[0].formattedAddress);
								}
								alertResponseList.add(alertResponse);
							}
						}
						LOGGER.debug("Alert: Src Lat:Lon " + srcLatLong + " , " + "Sol Lat:Lon " + solLatLong + " LatUnit: " + latUnit + " LonUnit:" + longUnit + "Grid: " + srcGrid[i][j]);
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return alertResponseList;
	}

	/**
	 *
	 * @param x
	 * A point of a 2D array, which specifies the row
	 * @param y
	 * A point of a 2D array which specifies the column
	 * @param latUnit
	 * A unit of separation of rows. Used to separate latitude points.
	 * @param longUnit
	 * A unit of separation of columns. used to separate longitude points.
	 * @param neLat
	 * Specify the north east latitude point.
	 * @param swLong
	 * specify the south west longitude point
	 * @return
	 */
	private String getLatLongFromGrid(int x, int y,
			double latUnit, double longUnit, double neLat, double swLong) {
		// TODO Auto-generated method stub
		double latitude= neLat-(x*latUnit);
		double longitude = swLong+(y*longUnit);
		LOGGER.debug("Lat Long of the prob " + latitude + ":" + longitude);
		return (latitude+":"+longitude);
		//-93.4
	}

	/**
	 *
	 * @param grid
	 * A 2D array which holds all the values of the bounding box.
	 * @param min
	 * minimum value of a range. It could be a alert range or solution range.
	 * @param max
	 * maximum value of a range. It could be a alert range or solution range.
	 * @return
	 */
	private int[][] calculateRange(double[][] grid, int min, int max) {
		LOGGER.info("Calculating Range....");
		// TODO Auto-generated method stub
		int[][] calculatedGrid = new int[grid.length][grid[0].length];
		double currVal ;
		for(int i=0;i<grid.length;i++){
			 for(int j=0;j<grid[0].length;j++){
				 currVal = grid[i][j];
				 if(currVal >= min && currVal <= max){
					 calculatedGrid[i][j]=1;
				 }else{
					 calculatedGrid[i][j]=0;
				 }
			 }
		}
		LOGGER.info("Done calcuating Range...");
		return calculatedGrid;

	}

	/**
	 *
	 * @param pX
	 * A point in the grid which specifies the row index of a matrix.
	 * @param pY
	 * A point in the grid which specifies the column index of a matrix
	 * @param solGrid
	 * A 2D matrix which has all the solution points.
	 * @param maxDist
	 * specify the maximum distance with in which a solution can be searched.
	 * @return
	 */
	private String getNearestSolution(int pX, int pY, int[][] solGrid, int maxDist) {
		// TODO Auto-generated method stub
		int maxX=maxDist;
		int maxY=maxDist;
		int pointX=pX;
		int pointY=pY;
		boolean solFound=false;
		int nearestSol = maxX*maxY;
		int numOfHops= maxX*maxY;
		int foundAtX=-999, foundAtY=-999;
		try{
			for(int i=pointX; i>(Math.max((pointX - maxX),0));i--){
				for(int j=pointY; j>(Math.max((pointY-maxY), 0));j--){
					{
						if (solGrid[i][j] == 1){
							solFound=true;
							numOfHops=((pX-i)+(pY-j));
							if (numOfHops < nearestSol){
								nearestSol=numOfHops;
								maxX=pX-i;
								maxY=pY-j;
								foundAtX=i;
								foundAtY=j;
							}
						}
					}
				}
			}

			//Scanning NorthEast Block
			for(int i=pointX; i>(Math.max((pointX-maxX), 0));i--){
				for(int j=pointY; j<(Math.min((pointY+maxY), (solGrid[0].length-1)));j++){
					if (solGrid[i][j] == 1){
						solFound=true;
						numOfHops=((pX-i)+(j-pY));
						if(numOfHops < nearestSol){
							nearestSol=numOfHops;
							maxX=pX-i;
							maxY=j-pY;
							foundAtX=i;
							foundAtY=j;
						}
					}
				}
			}


			//Scanning SouthWest Block

			for(int i=pointX; i<(Math.min((pointX+maxX),(solGrid.length-1)));i++){
				for(int j=pointX; j<(Math.min((pointY+maxY), (solGrid[0].length-1)));j++){
					if(solGrid[i][j] == 1){
						solFound=true;
						numOfHops=((i-pX)+(j-pY));
						if(numOfHops < nearestSol){
							nearestSol=numOfHops;
							maxX=i-pX;
							maxY=j-pY;
							foundAtX=i;
							foundAtY=j;
						}
					}
				}
			}

			// Scanning SoutEast Block 
			for(int i=pointX; i<(Math.min((pointX+maxX),(solGrid.length-1)));i++){
				for(int j=pointY; j>(Math.max((pointY-maxY), 0));j--){
					if(solGrid[i][j]==1){
						solFound=true;
						numOfHops=((i-pX)+(pY-j));
						if(numOfHops<nearestSol){
							nearestSol=numOfHops;
							maxX=i-pX;
							maxY=j-pX;
							foundAtX=i;
							foundAtY=j;

						}
					}
				}
			}

		}catch(Exception e){
			e.printStackTrace();
		}

		if(solFound){
			return (foundAtX+":"+foundAtY);
		}
		else{
			return ("NotFound");
		}
	}

	/**
	 *
	 * @param emage
	 * A emage of a DataSource or Query for which a solution needs to be found.
	 * @param alertSrcType
	 * Specify the type of source here. It could be a query or datasource.
	 * @param enableQueue
	 * Boolean type  which specifies if the alert has to be pushed to a rabbitmq queue.
	 * @param minLimit
	 * Minimum limit for an alert.
	 * @param maxLimit
	 * Maximum limit for an alert.
	 * @param message
	 * A custom message which needs to pushed as a result to an end point or as a tweet.
	 * @param boundingbox
	 * * Area which needs an alert should be passed here. This should be a string with comma seperated values containing swlat, swlong, nelat, nelong
	 * @return
	 * @throws IOException
	 */

	public List<AlertResponse> checkAlertWithoutSolution(JsonObject emage, String alertSrcType, boolean enableQueue, int minLimit, int maxLimit, String message, String boundingbox) throws IOException {
		GeoApiContext context = new GeoApiContext().setApiKey(Config.getProperty("googleAPIKey2"));
		GeocodingResult[] results;
		List<AlertResponse> alertResponseList = new ArrayList<AlertResponse>();
		JsonArray image = emage.getAsJsonArray("image");
		//ArrayList<String> alertList = new ArrayList<String>();
		int countOfMatches=0, count=0;
		float neLat=Float.parseFloat(emage.get("neLat").toString());
		float swlong=Float.parseFloat(emage.get("swLong").toString());
		int rowNum=Integer.parseInt(emage.get("row").toString());
		int colNum=Integer.parseInt(emage.get("col").toString());
		float latUnit=Float.parseFloat(emage.get("latUnit").toString());
		float longUnit=Float.parseFloat(emage.get("longUnit").toString());
		String latlongAlert;
		if (alertSrcType.equals("Q")){
			if(boundingbox.equals(null) || boundingbox.isEmpty()){
				for (Object a : image){
					results = null;
					Float currVal=Float.parseFloat(a.toString());
					if ( currVal > minLimit && currVal < maxLimit){
						latlongAlert=getLatLong(count,neLat,swlong,rowNum, colNum, latUnit, longUnit);
						countOfMatches++;
						if (enableQueue) {
							pushAlert(id, latlongAlert, latUnit, longUnit, (double)currVal);
						} else {
							try {
								results = GeocodingApi.reverseGeocode(context,
										new LatLng(Double.valueOf(latlongAlert.substring(0, latlongAlert.indexOf(":"))), Double.valueOf(latlongAlert.substring((latlongAlert.indexOf(":") + 1))))).await();
								if (results.length > 0) {
									alertResponseList.add(new AlertResponse(latlongAlert
											, results[0].formattedAddress
											, currVal
											, message));
								} else {
									alertResponseList.add(new AlertResponse(latlongAlert
											, null
											, currVal
											, message));
								}
							} catch (Exception e) {
								e.printStackTrace();
							}

						}

					} else {
					}
					count++;
				}
			}else{
				String[] bb=boundingbox.split(",");
				if (bb.length != 4 ){
				}else{
					double[][] subBoundingBox =getsubBoundingBox(image, bb, emage);
					double subNeLat= Double.parseDouble(bb[2]);
					double subNeLong=Double.parseDouble(bb[3]);
					double subSwLat=Double.parseDouble(bb[0]);
					double subSwLong=Double.parseDouble(bb[1]);
					double alertFoundLat;
					double alertFoundLong;
					String latLongAlert="";
					for(int i=0;i<subBoundingBox.length;i++){
						for(int j=0;j<subBoundingBox[0].length;j++){
							if ( subBoundingBox[i][j] > minLimit && subBoundingBox[i][j] < maxLimit){

								//Calculating LatLong for that point
								alertFoundLat=subNeLat-(i*latUnit);
								alertFoundLong=subSwLong+(j*longUnit);
								latLongAlert=alertFoundLat+":"+alertFoundLong;
								//alertList.add(latlongAlert);
								if (enableQueue) {
									pushAlert(id, latLongAlert, latUnit, longUnit, subBoundingBox[i][j]);
								} else {
									try {
										results = GeocodingApi.reverseGeocode(context,
												new LatLng(Double.valueOf(latLongAlert.substring(0, latLongAlert.indexOf(":"))), Double.valueOf(latLongAlert.substring((latLongAlert.indexOf(":") + 1))))).await();
										if (results.length > 0) {
											alertResponseList.add(new AlertResponse(latLongAlert
													, results[0].formattedAddress
													, (float) subBoundingBox[i][j]
													, message));
										} else {
											alertResponseList.add(new AlertResponse(latLongAlert
													, null
													, (float) subBoundingBox[i][j]
													, message));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

								}

							} else {
							}
							count++;
						}
					}
				}

			}

		}
		else if (alertSrcType.equals("ds")){
			if(boundingbox.equals(null) || boundingbox.isEmpty()) {
				for (Object a : image){
					String currVal=a.toString().replace("\"", "");
					float currValF;
					if (!(currVal.equalsIgnoreCase("NaN"))){
						currValF=Float.parseFloat(currVal);
						if ( currValF > minLimit && currValF < maxLimit){
							latlongAlert=getLatLong(count,neLat,swlong,rowNum, colNum, latUnit, longUnit);
							countOfMatches++;
							if (enableQueue) {
								pushAlert(id, latlongAlert, latUnit, longUnit, (double)currValF);
							} else {
								try {
									results = GeocodingApi.reverseGeocode(context,
											new LatLng(Double.valueOf(latlongAlert.substring(0, latlongAlert.indexOf(":"))), Double.valueOf(latlongAlert.substring((latlongAlert.indexOf(":") + 1))))).await();
									if (results.length > 0) {
										alertResponseList.add(new AlertResponse(latlongAlert
												, results[0].formattedAddress
												, currValF
												, message));
									} else {
										alertResponseList.add(new AlertResponse(latlongAlert
												, null
												, currValF
												, message));
									}
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
							//alertList.add(latlongAlert);
						} else {
						}
					}
					count++;
				}
			}else{
				String[] bb=boundingbox.split(",");
				if (bb.length != 4 ){
				}else{
					double[][] subBoundingBox =getsubBoundingBox(image, bb, emage);
					double subNeLat= Double.parseDouble(bb[2]);
					double subNeLong=Double.parseDouble(bb[3]);
					double subSwLat=Double.parseDouble(bb[0]);
					double subSwLong=Double.parseDouble(bb[1]);
					double alertFoundLat;
					double alertFoundLong;
					String latLongAlert="";
					for(int i=0;i<subBoundingBox.length;i++){
						for(int j=0;j<subBoundingBox[0].length;j++){

							if ( subBoundingBox[i][j] > minLimit && subBoundingBox[i][j] < maxLimit){

								//Calculating LatLong for that point
								alertFoundLat=subNeLat+(i*latUnit);
								alertFoundLong=subSwLong+(j*longUnit);
								latLongAlert=alertFoundLat+":"+alertFoundLong;
								//alertList.add(latlongAlert);
								if (enableQueue) {
									pushAlert(id, latLongAlert, latUnit, longUnit, subBoundingBox[i][j]);
								} else {
									try {
										results = GeocodingApi.reverseGeocode(context,
												new LatLng(Double.valueOf(latLongAlert.substring(0, latLongAlert.indexOf(":"))), Double.valueOf(latLongAlert.substring((latLongAlert.indexOf(":") + 1))))).await();
										if (results.length > 0) {
											alertResponseList.add(new AlertResponse(latLongAlert
													, results[0].formattedAddress
													, (float) subBoundingBox[i][j]
													, message));
										} else {
											alertResponseList.add(new AlertResponse(latLongAlert
													, null
													, (float) subBoundingBox[i][j]
													, message));
										}
									} catch (Exception e) {
										e.printStackTrace();
									}

								}

							} else {
							}
							count++;
						}
					}
				}
			}

		}
		return alertResponseList;

	}

	/**
	 *
	 * @param image
	 * A jsonArray which contains all the values of a particular area.
	 * @param bb
	 * Bounding box which a string array containing swlat, swlong, nelat, nelong
	 * @param emage
	 * Original emage from which a smaller bounding box can be derived.
	 * @return
	 */
	private double[][] getsubBoundingBox(JsonArray image, String[] bb, JsonObject emage) {
		int rows= Integer.parseInt(emage.get("row").toString());
		int cols= Integer.parseInt(emage.get("col").toString());

			//Converting 1D array to 2D array.
		double[][] boundingbox= new double[rows][cols];
		double neLat=Double.parseDouble(emage.get("neLat").toString());
		double neLong=Double.parseDouble(emage.get("neLong").toString());
		double swLat=Double.parseDouble(emage.get("swLat").toString());
		double swLong=Double.parseDouble(emage.get("swLong").toString());
		double subNeLat= Double.parseDouble(bb[2]);
		double subNeLong=Double.parseDouble(bb[3]);
		double subSwLat=Double.parseDouble(bb[0]);
		double subSwLong=Double.parseDouble(bb[1]);
		double latUnit= Double.parseDouble(emage.get("latUnit").toString());
		double longUnit=Double.parseDouble(emage.get("longUnit").toString());
//		// subboundingbox boundry alteration

		//nelat alteration
		if(subNeLat > neLat)
			subNeLat=neLat;
		else
			subNeLat = Math.floor(subNeLat / latUnit)*latUnit;


		//neLong alteration
		if(subNeLong > neLong)
			subNeLong=neLong;
		else
			subNeLong= (Math.floor(subNeLong / longUnit))*longUnit;

		//swLat alteration
		if(subSwLat < swLat)
			subSwLat=swLat;
		else
			subSwLat=(Math.ceil(subSwLat / latUnit)) * latUnit;

		//swLong alteration
		if (subSwLong < swLong)
			subSwLong=swLong;
		else
			subSwLong=(Math.ceil(subSwLong / longUnit)) * longUnit;

		int subRow=(int) ((subNeLat-subSwLat)/latUnit);
		int subCol=(int) ((subNeLong-subSwLong)/longUnit);
		double[][] subBoundingBox = new double[subRow][subCol];

		int rowId=0;
		for(int i=0;i<rows;i++){
			for (int j=0;j<cols;j++){
				if(image.get(rowId).toString().equals("NaN")){
					boundingbox[i][j]=0;
				}else{
					boundingbox[i][j]=Double.parseDouble(image.get(rowId).toString());
				}
				rowId++;
			}
		}

		//subBoundingBox
		int subBBRowBeginIndex=(int) ((neLat-subNeLat)/latUnit);
	//	int subBBRowEndIndex=(int) (subBBRowBeginIndex+((subNeLat-subSwLat)/latUnit));
		int subBBRowEndIndex=(int) ((neLat-subSwLat)/latUnit);
		int subBBColBeginIndex=(int) ((subSwLong-swLong)/longUnit);
		int subBBColEndIndex=(int) ((subNeLong-swLong)/longUnit);
		int rCnt=0, cCnt=0;
		for(int i=subBBRowBeginIndex;i<subBBRowEndIndex;i++){
			for(int j=subBBColBeginIndex;j<subBBColEndIndex;j++){
				subBoundingBox[rCnt][cCnt]=boundingbox[i][j];
				cCnt++;
			}
			rCnt++;
			cCnt=0;
		}
		return subBoundingBox;

	}

	/**
	 *
	 * @param id
	 * Alert ID to which an alert needs to be pushed
	 * @param latLong
	 * latitude and longitude point of an alert
	 * @param latUnit
	 * unit of seperation of latitude.
	 * @param longUnit
	 * unit of seperation of longitude
	 * @param currVal
	 * Value of the a particular point.
	 * @throws IOException
	 */
	private void pushAlert(int id, String latLong, double latUnit, double longUnit, double currVal) throws IOException {
		// TODO Auto-generated method stub
	 		channel.queueDeclare(""+id, false, false, false, null);
	 	    String message =latLong+","+latUnit+","+longUnit+","+currVal;
	 	    channel.basicPublish("", ""+id, null, message.getBytes());
			LOGGER.info(" Sent '" + message + "'");
	 	}


	/**
	 *
	 * @param currVal
	 * A position on a 1D array for which Lat Long needs to found.
	 * @param neLat
	 * North East latitude of that bounding box
	 * @param swlong
	 * south west longitude of that bounding box
	 * @param rowNum
	 * num of rows in that bounding box
	 * @param colNum
	 * num of columns in that bounding box
	 * @param latUnit
	 * unit of seperation of latitude
	 * @param longUnit
	 * unit of seperation of longitude
	 * @return
	 */
		private String getLatLong(int currVal, float neLat, float swlong, int rowNum,int colNum, float latUnit, float longUnit) {
			 String latLong;
			 float lat =  neLat-currVal/colNum * latUnit;
			 float lon = swlong + currVal%rowNum * longUnit;
			 latLong=lat+":"+lon;
			 return latLong;
			 // TODO Auto-generated method stub
		}
	
	}
