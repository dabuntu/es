package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIterFactory;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.datasource.DataSource.DataFormat;
import com.eventshop.eventshoplinux.domain.datasource.emage.DataPoint;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.STMerger;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.DatasourceType;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.SpatialWrapper;
import com.eventshop.eventshoplinux.service.MongoDB;
import com.eventshop.eventshoplinux.util.commonUtil.Config;

import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;
import com.mongodb.BasicDBList;
import com.mongodb.DBObject;

//tsuji:note [a] adding these
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;

public class SensorMongoDBEmageIterator extends EmageIterator {
	protected static Log log = LogFactory
			.getLog(SensorMongoDBEmageIterator.class);
	String fileURL;
	MongoDB mongo;
	double radious;
	long startTimeStream, endTimeStream;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;
	int ignoreColorSamplesBeyond;
	// int latIndex = -1, lngIndex = -1, valIndex = -1; // no specific column
	// number provided
	ResolutionWrapper resWrapper;
	private SimpleDateFormat formatter = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	ArrayList<DataPoint> myPoints = new ArrayList<DataPoint>();

	boolean isPredict = false;
	double minPredict = 0;
	double maxPredict = 100;

	public SensorMongoDBEmageIterator(FrameParameters fp, String th,
			String url, MongoDB mongo, long startTimeStream,
			long endTimeStream, boolean endTimeUnbound, double radious) {
		params = fp;
		this.theme = th;
		this.fileURL = url;
		this.mongo = mongo;
		this.radious = radious;
		this.startTimeStream = startTimeStream;
		this.endTimeStream = endTimeStream;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		setResolutionWrapper(DatasourceType.point, SpatialWrapper.count);
	}

	/*
	 * support ResolutionWrapper
	 */

	public SensorMongoDBEmageIterator(FrameParameters fp, String th,
			String url, MongoDB mongo, long startTimeStream,
			long endTimeStream, boolean endTimeUnbound, double radious,
			DatasourceType ds, SpatialWrapper sw) {
		params = fp;
		this.theme = th;
		this.fileURL = url;
		this.mongo = mongo;
		this.radious = radious;
		this.startTimeStream = startTimeStream;
		this.endTimeStream = endTimeStream;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		setResolutionWrapper(ds, sw);

	}

	public void setResolutionWrapper(DatasourceType ds, SpatialWrapper sw) {
		resWrapper = new ResolutionWrapper(ds, params, sw);
	}

	public void setResolutionWrapper(ResolutionWrapper rw) {
		resWrapper = rw;
	}

	public void setPredict(boolean p) {
		isPredict = p;
	}

	public void setPredict(boolean p, double min, double max) {
		isPredict = p;
		minPredict = min;
		maxPredict = max;
	}

	@Override
	public void run() {
		// long curWindowEnd = 0;

		// long now = System.currentTimeMillis();
		// curWindowEnd = (long)Math.ceil(System.currentTimeMillis() /
		// params.timeWindow) * params.timeWindow + params.syncAtMilSec;
		// log.info("now: "+now +"   curWindowEnd: "+curWindowEnd);
		int numEmage = 0;

		while (isRunning) {
			Emage e;
			try {
				long startEit = this.startTimeStream
						+ (numEmage * params.timeWindow);
				long endEit = startEit + params.timeWindow;
				long now = System.currentTimeMillis();
				log.info("number of emage " + numEmage);
				if (startEit > this.endTimeStream) {
					isRunning = false;
					break;
				}

				while (endEit > now) {
					if (!isRunning)
						break;

					try {
						log.info("Sleeping for " + (endEit - now + 1) + "ms");
						Thread.sleep(endEit - now + 1);

						log.info("woke up");
						// now = System.currentTimeMillis();
						// myPoints = new ArrayList<DataPoint>(); // we don't
						// accumulate datapoints from previous time window
					} catch (InterruptedException e1) {
						log.error(e1.getMessage());
					}
				}
				e = getTheEmage(startEit, endEit);
				queue.add(e);
				log.info("SensorMongoDBEmageIterator: add emage to the queue");
				myPoints = new ArrayList<DataPoint>(); // we don't accumulate
														// datapoints from
														// previous time window

				numEmage++;
			} catch (Exception e2) {
				log.error(e2.getMessage());
			}
		}
	}

	@Override
	public boolean hasNext() {
		return queue.iterator().hasNext();
	}

	@Override
	public Emage peek() {
		return queue.peek();
	}

	@Override
	public Emage next() {
		try {
			return queue.take();
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return null;
	}

	@Override
	public void stop() {
		isRunning = false;
		Thread.currentThread().interrupt();
	}

	public Emage getTheEmage(long start, long end) {
		readPointsFromMongoDB(start, end);

		double[][] dataGrid;

		if (resWrapper != null) {
			resWrapper.setDataPoints(myPoints);
			resWrapper.doTransformation();
			dataGrid = resWrapper.getEmageGrid();
		} else {
			dataGrid = new double[params.numOfRows][params.numOfColumns];

			int nRows = params.numOfRows;
			int nCols = params.numOfColumns;

			for (int i = 0; i < nRows; i++) {
				for (int j = 0; j < nCols; j++) {
					// dataGrid[i][j] = gridValue(i,j);
					dataGrid[i][j] = 255 / (Math.pow(2, 0.1 * nearestLoc(i, j)));
					// log.info("datagrid ["+i+","+j+"]:" + dataGrid[i][j]);
				}
			}

		}

		// CommonUtil.printAllCellGrid(dataGrid);
		Emage e1 = new Emage(params, theme);
		e1.endTime = new Date(end);
		e1.startTime = new Date(start);
		e1.image = dataGrid;
		// log.info("test emage is generated or not? " +
		// e1.toJson().toString());
		return e1;
	}

	private double nearestLoc(int i, int j) {
		Point inPoint = new Point(i, j);
		double minDist = 9999999;
		for (int s = 0; s < myPoints.size(); s++) {
			if (inPoint.distance(myPoints.get(s).point) < minDist)
				minDist = inPoint.distance(myPoints.get(s).point);
		}
		return minDist;
	}

	private double gridValue(int i, int j) {
		Point inPoint = new Point(i, j);
		double minDist = 9999999;
		double value = 0;
		for (int s = 0; s < myPoints.size(); s++) {
			Point aPoint = new Point(myPoints.get(s).xEmageIndex,
					myPoints.get(s).yEmageIndex);
			if (inPoint.distance(aPoint) < minDist) {
				minDist = inPoint.distance(aPoint);
				value = myPoints.get(s).value;
			}
		}
		return value / (Math.pow(2, 0.1 * minDist));
	}

	public PlanarImage gridToEmage(double[][] dataGrid, String imageName)
			throws IOException {
		// creates a gray-scale image of the values
		int width = dataGrid[0].length;
		int height = dataGrid.length;

		// Get the number of bands on the image.
		URL imgURL = new URL(Config.getProperty("imgURL"));
		PlanarImage dummyImage = JAI.create("URL", imgURL);// dummy loaded. date
															// to be edited.

		int nbands = 3;

		// We assume that we can get the pixels values in a integer array.
		double[] pixelTemp = new double[nbands];

		// Get an iterator for the image.
		RandomIterFactory.create(dummyImage, null);
		WritableRaster rasterData = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_BYTE, width, height, nbands, new Point(0, 0));

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				double val = (dataGrid[i][j] <= 255) ? dataGrid[i][j] : 255;

				pixelTemp[0] = val;
				pixelTemp[1] = val;
				pixelTemp[2] = val;

				rasterData.setPixel(j, i, pixelTemp);
			}
		}

		SampleModel sModel2 = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, width, height, nbands);

		// Try to create a compatible ColorModel - if the number of bands is
		// larger than 4, it will be null.
		ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

		// Create a TiledImage using the sample and color models.
		TiledImage rectImage = new TiledImage(0, 0, width, height, 0, 0,
				sModel2, cModel2);

		// Set the data of the tiled image to be the raster.
		rectImage.setData(rasterData);

		// Save the image on a file.
		try {
			ImageIO.write(rectImage, "jpg", new File(imageName + ".jpg"));

		} catch (IOException e) {
			log.error(e.getMessage());
			log.error("Error in rectifying  image");
		}

		return rectImage;
	}

	public void readPointsFromMongoDB(long start, long end) {

		// Loop for Latitude Longitude blocks
		for (double i = params.swLat; i < params.neLat; i = i + params.latUnit) {
			for (double j = params.swLong; j < params.neLong; j = j
					+ params.longUnit) {
				if (!isRunning)
					break;

				// call mongodb to get sensors near by lat/long
				// center of the each cell in the grid
				double lat = i + 0.5 * params.latUnit;
				double lng = j + 0.5 * params.longUnit;
				double r = radious / 3959.0;

				String qstr = "{$and:[{date:{$gt:'"
						+ formatter.format(new Date(start)) + "',$lt:'"
						+ formatter.format(new Date(end)) + "'}},"
						+ "{'loc':{$geoWithin:{$centerSphere:[[" + lng + ","
						+ lat + "]," + r + "]}}}]}";
				// System.out.println(qstr);
				String fields = "loc,date,value";
				if (mongo.collection == null) {
					log.error("invalid mongo db configulation: collection not found");
					this.isRunning = false;
					break;
				}
				List<DBObject> sensorList = mongo.find(qstr, fields);
				if (sensorList.size() == 0 && isPredict) {
					log.info("In predictive mode");
					// increase radious two times in each loop
					double pr = (radious) / 3959.0;
					while (sensorList.size() == 0) {
						pr = pr * 2;
						String qstr2 = "{$and:[{date:{$gt:'"
								+ formatter.format(new Date(start)) + "',$lt:'"
								+ formatter.format(new Date(end)) + "'}},"
								+ "{'loc':{$geoWithin:{$centerSphere:[[" + lng
								+ "," + lat + "]," + pr + "]}}}]}";
						sensorList = mongo.find(qstr2, fields);
					}
				}
				for (DBObject sensor : sensorList) {
					BasicDBList loc = ((BasicDBList) ((DBObject) sensor
							.get("loc")).get("coordinates"));
					Double value = (Double) sensor.get("value");
					String sensorDate = (String) sensor.get("date");
					Double sensorLat = (Double) loc.get(1);
					Double sensroLng = (Double) loc.get(0);
					Point p = latLong2Pixel(lat, lng);

					System.out.println(value + "," + sensorDate + ",long/lat: "
							+ sensroLng + "/" + sensorLat);
					myPoints.add(new DataPoint(lat, lng, value, p));

				}

			}
		}
	}

	public Point latLong2Pixel(Double latV, Double longV) {
		int nRows = params.getNumOfRows();

		MathContext context = new MathContext(5);
		int x = (nRows - 1)
				- (int) ((BigDecimal.valueOf(latV)).subtract(
						BigDecimal.valueOf(params.swLat), context).divide(
						BigDecimal.valueOf(params.latUnit), context)
						.doubleValue());
		int y = (int) ((BigDecimal.valueOf(longV)).subtract(
				BigDecimal.valueOf(params.swLong), context).divide(
				BigDecimal.valueOf(params.longUnit), context).doubleValue());

		return new Point(x, y);

	}

	public Point2D pixel2LatLong(int x, int y) {

		double lat = params.neLat - (x * params.latUnit);
		double lng = (params.swLong + y * params.longUnit);
		return new Point2D.Double(lat, lng);

	}

	public static void main(String[] args) {
		try {
			/*
			 * long timeWindow = 1000*60*24; //1mins for testing//*60*24*7; //
			 * the last 7 days long sync = 1000; double latUnit = 2; double
			 * longUnit = 2; double swLat = 20; double swLong = 30; double neLat
			 * = 25; double neLong = 35; FrameParameters fp = new
			 * FrameParameters(timeWindow, sync, latUnit,longUnit, swLat,swLong
			 * , neLat, neLong); FrameParameters fpSoCal = new
			 * FrameParameters(1000*60*60*24, 0, 0.2, 0.2, 37.60, -122.40,
			 * 40.00, -120.80); String theme=Config.getProperty("sandyTheme");
			 * //String tempFile =
			 * "https://docs.google.com/spreadsheet/pub?key=0Auv1FPgY5UScdHNia09KM2o4c0N5aThqMGRXYU5jbEE&output=html"
			 * ; //String tempFile =
			 * "https://www.dropbox.com/s/qp1yh7be112wpkn/csvfield.csv"; String
			 * tempFile =
			 * "http://eventshop.ics.uci.edu:8080/sln/datasource/csvfield";
			 * //Emage e1 = new Emage(fp, theme);.
			 * System.out.println("test sensor mongodb "); MongoDB mongo = new
			 * MongoDB("evimdb2"); mongo.setCollection("PM2_5_daily");
			 * SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			 * 
			 * Date st = formatter.parse("2013-08-10"); Date et
			 * =formatter.parse("2013-12-31"); long startTimeStream =
			 * st.getTime(); long endTimeStream = et.getTime();
			 * 
			 * SensorMongoDBEmageIterator sensorEit = new
			 * SensorMongoDBEmageIterator(fpSoCal, theme, tempFile, mongo,
			 * startTimeStream, endTimeStream, false, 10.0,
			 * DatasourceType.point,SpatialWrapper.sum); //sensorEit.run();
			 * System.out.println("end test sensor mongodb ");
			 */

			// 999- PM2.5 (dubplicate)
			// 998 - Ozone
			// 997 - PM2.5 (predict)

			// 888 - PM2.5 missing value
			// 887 - PM2.5 predict
			// 886

			String id = "888";
			String theme = "PM2_5_daily";
			String name = theme;
			boolean predict = false;

			int option = 2;

			if (option == 1) {
				id = "887";
				theme = "PM2_5_daily";
				name = theme;
				predict = true;
			} else if (option == 2) {
				id = "886";
				theme = "Ozone_daily";
				name = theme;
				predict = false;
			}
			DataFormat format = DataFormat.stream;
			String supported = "SensorMongoDB";
			String url = "http://vanilla.ics.uci.edu:8081/eventshoplinux/webservices/mongodb";
			FrameParameters fpSoCal = new FrameParameters(1000 * 60 * 60 * 24,
					0, 0.2, 0.2, 37.60, -122.40, 40.00, -120.80);
			System.out.println("test sensor mongodb ");

			DataSource src = new DataSource(id, theme, name, url, format,
					supported, null, null, fpSoCal, null);

			String tempDir = Config.getProperty("tempDir");
			String filepath = tempDir + "/ds" + src.srcID + "_" + src.srcName;
			String imgBasePath = tempDir + Constant.RESULT_DS;

			MongoDB mongo = new MongoDB("evimdb2");
			mongo.setCollection(theme);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			Date st = formatter.parse("2013-08-05"); // min: "2012-12-31"
			Date et = formatter.parse("2013-08-10"); // max: "2013-12-31"
			long startTimeStream = st.getTime();
			long endTimeStream = et.getTime();

			SensorMongoDBEmageIterator mongoEIter = new SensorMongoDBEmageIterator(
					src.initParam, src.srcTheme, src.url, mongo,
					startTimeStream, endTimeStream, false, 10.0,
					DatasourceType.point, SpatialWrapper.max);
			mongoEIter.setPredict(predict);
			mongoEIter.setSrcID(Long.parseLong(src.srcID));
			STMerger merger = null;
			DataProcess process = new DataProcess(merger, mongoEIter, null,
					filepath, imgBasePath + src.srcID, src.srcID);

			// Start the data collecting process
			new Thread(process).start();
			System.out.println("end sensor mongodb ");

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		// System.exit(0);
	}

}
