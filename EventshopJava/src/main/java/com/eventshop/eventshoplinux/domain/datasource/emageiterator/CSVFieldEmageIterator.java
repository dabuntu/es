package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import static com.eventshop.eventshoplinux.constant.Constant.TEMPDIR;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.DataPoint;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.DatasourceType;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.SpatialWrapper;
import com.eventshop.eventshoplinux.util.commonUtil.Config;

//tsuji:note [a] adding these
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.io.InputStream;

public class CSVFieldEmageIterator extends EmageIterator {
	protected static Log log = LogFactory.getLog(CSVFieldEmageIterator.class);
	String fileURL;
	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;
	int ignoreColorSamplesBeyond;
	int latIndex = -1, lngIndex = -1, valIndex = -1; // no specific column
														// number provided
	ResolutionWrapper resWrapper;

	ArrayList<DataPoint> myPoints = new ArrayList<DataPoint>();

	public CSVFieldEmageIterator(FrameParameters fp, String th, String url) {
		params = fp;
		this.theme = th;
		this.fileURL = url;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		setResolutionWrapper(DatasourceType.point, SpatialWrapper.count);
	}

	/*
	 * support ResolutionWrapper
	 */

	public CSVFieldEmageIterator(FrameParameters fp, String th, String url,
			String ds, String sw) {
		params = fp;
		this.theme = th;
		this.fileURL = url;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		setResolutionWrapper(DatasourceType.valueOf(ds),
				SpatialWrapper.valueOf(sw));
	}

	public CSVFieldEmageIterator(FrameParameters fp, String th, String url,
			DatasourceType ds, SpatialWrapper sw) {
		params = fp;
		this.theme = th;
		this.fileURL = url;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		setResolutionWrapper(ds, sw);
	}

	public void setResolutionWrapper(DatasourceType ds, SpatialWrapper sw) {
		resWrapper = new ResolutionWrapper(ds, params, sw);
	}

	// Select particular column index for lat, long, and/or value fields (start
	// from 0)
	// latitude and longitude fields are required, value field is optional
	// if value's index is not provide (set = -1), the default value of the
	// value is 1.0
	public void setLatLongValIndex(int latIndex, int lngIndex, int valIndex) {
		this.latIndex = latIndex;
		this.lngIndex = lngIndex;
		this.valIndex = valIndex;
	}

	public void setResolutionWrapper(ResolutionWrapper rw) {
		resWrapper = rw;
	}

	@Override
	public void run() {
		long curWindowEnd = 0;

		long now = System.currentTimeMillis();
		curWindowEnd = (long) Math.ceil(System.currentTimeMillis()
				/ params.timeWindow)
				* params.timeWindow + params.syncAtMilSec;
		log.info("now: " + now + "   curWindowEnd: " + curWindowEnd);

		while (isRunning) {
			Emage e;
			try {
				long startEit = System.currentTimeMillis();
				e = getTheEmage(curWindowEnd);
				log.info("CSVFieldEmageIterator (" + e.theme
						+ ") process time: "
						+ (System.currentTimeMillis() - startEit) + " ms");
				// move to getTheEmage method
				// e.endTime = new Date(curWindowEnd);
				// e.startTime = new Date(curWindowEnd-params.timeWindow);
				queue.add(e);
				// System.out.println(e.toJson());
				log.info("CSVFieldEmageIterator: add emage to the queue");

				curWindowEnd += params.timeWindow;

				now = System.currentTimeMillis();
				while (now < curWindowEnd) {
					if (!isRunning)
						break;

					try {
						log.info("Sleeping for " + (curWindowEnd - now + 1)
								+ "ms");
						Thread.sleep(curWindowEnd - now + 1);

						log.info("woke up");
						now = System.currentTimeMillis();
						myPoints = new ArrayList<DataPoint>(); // we don't
																// accumulate
																// datapoints
																// from previous
																// time window
					} catch (InterruptedException e1) {
						log.error(e1.getMessage());
					}
				}
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

	public Emage getTheEmage(long curWindowEnd) {
		long startGetEmage = System.currentTimeMillis();
		try {
			if (this.latIndex == -1)
				readPointsFromCSVFile();
			else
				readPointsFromCSVFile(this.latIndex, this.lngIndex,
						this.valIndex);
		} catch (IOException e2) {
			log.info(e2.getMessage());
			e2.printStackTrace();
		}

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
		log.info("CSVFieldEmageIterator readpointsfromfile process time: "
				+ (System.currentTimeMillis() - startGetEmage) + "ms");
		/*
		 * for(int p = 0; p < myPoints.size(); p++){ Point aPoint =
		 * latLong2Pixel(myPoints.get(p).lat, myPoints.get(p).lng);
		 * //dataGrid[aPoint.x][aPoint.y] += myPoints.get(p).value; for(int i =
		 * 0; i < nRows; i++){ for(int j = 0; j < nCols; j++){ Point bPoint =
		 * new Point(i, j); dataGrid[i][j] += myPoints.get(p).value
		 * /(Math.pow(2,0.1*bPoint.distance(aPoint))); } } }
		 */

		/*
		 * try { gridToEmage(dataGrid, theme); //purely for visualizing/
		 * debugging } catch (IOException e) { e.printStackTrace(); }
		 */

		// CommonUtil.printAllCellGrid(dataGrid);
		Emage e1 = new Emage(params, theme);
		e1.endTime = new Date(curWindowEnd);
		e1.startTime = new Date(curWindowEnd - params.timeWindow);
		e1.image = dataGrid;
		// log.info("test emage is generated or not? " +
		// e1.toJson().toString());
		return e1;
	}

	/*
	 * before having ResolutionWrapper public Emage getTheEmage( ) { try {
	 * readPointsFromCSVFile(); } catch (IOException e2) {
	 * log.error(e2.getMessage()); } int nRows=params.getNumOfRows(); int
	 * nCols=params.getNumOfColumns();
	 * 
	 * double [][] dataGrid= new double[nRows][nCols];
	 * 
	 * 
	 * for(int i = 0; i < nRows; i++){ for(int j = 0; j < nCols; j++){
	 * 
	 * dataGrid[i][j]= 255/(Math.pow(2,0.1*nearestLoc(i,j))); } }
	 * 
	 * 
	 * try { gridToEmage(dataGrid, theme); //purely for visualizing/ debugging }
	 * catch (IOException e) { log.error(e.getMessage()); }
	 * 
	 * Emage e1 = new Emage(params, theme); e1.image = dataGrid; return e1 ; }
	 */
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
				System.out.println("minimum distance for the point is "
						+ minDist);
				value = myPoints.get(s).value;
				System.out.println("Value for the point in consideration is "
						+ value);

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
			System.out
					.println(Config.getProperty(TEMPDIR) + imageName + ".jpg");
			ImageIO.write(rectImage, "jpg",
					new File(Config.getProperty(TEMPDIR) + imageName + ".jpg"));

		} catch (IOException e) {
			log.error(e.getMessage());
			log.error("Error in rectifying  image");
		}

		return rectImage;
	}

	// Provide column index of lat, long, and value field inside the CSV file
	// (start with 0)
	// The latitude and longitude fields are required, but the value field is
	// optional.
	// If value field is not provide (valueIndex == -1), the default value is
	// 1.0.
	public void readPointsFromCSVFile(int latIndex, int lngIndex, int valueIndex)
			throws IOException {
		URL url = new URL(fileURL);
		int x = 0;
		InputStream stream = url.openStream();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
		} catch (Exception e) {
			e.printStackTrace();
		}
		String dataRow = reader.readLine();
		if (dataRow != null)
			dataRow = reader.readLine(); // ignore the first line. It is the
											// header
		while (dataRow != null) {
			x = x + 1;
			System.out.println(x);
			String[] dataArray = dataRow.split(",");
			if (dataArray.length >= 2) {
				Double lat = Double.parseDouble(dataArray[latIndex]);
				Double lng = Double.parseDouble(dataArray[lngIndex]);
				Point p = latLong2Pixel(lat, lng);
				if (valueIndex != -1) {
					myPoints.add(new DataPoint(lat, lng, Double
							.parseDouble(dataArray[valueIndex]), p));
					System.out.println("lat: " + lat + "long: " + lng
							+ "value3: "
							+ Double.parseDouble(dataArray[valueIndex]) + "p: "
							+ p);
					// log.info("lat,long,value:" + lat +"," + lng + "," +
					// dataArray[valueIndex] );
				} else {
					myPoints.add(new DataPoint(lat, lng, 1.0, p));
					// log.info("lat,long,value:" + lat +"," + lng + "," + 1.0
					// );
				}

			} else {
				System.out.println("Invalid format: " + dataRow);
			}
			dataRow = reader.readLine(); // read next line
		}
		reader.close();

	}

	// The latitude, longitude, and value fields are required
	// The CSV file need follow this format "lat, long, value"

	public void readPointsFromCSVFile() throws IOException {
		URL url = new URL(fileURL);

		InputStream stream = url.openStream();

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		String dataRow = reader.readLine();
		if (dataRow != null)
			dataRow = reader.readLine(); // ignore the first line. It is the
											// header
		while (dataRow != null) {
			String[] dataArray = dataRow.split(",");
			if (dataArray.length == 3) {
				Double lat = Double.parseDouble(dataArray[0]);
				Double lng = Double.parseDouble(dataArray[1]);
				Double val = Double.parseDouble(dataArray[2]);
				Point p = latLong2Pixel(lat, lng);

				myPoints.add(new DataPoint(lat, lng, val, p));
				// log.info("valid format:" + lat + "," + lng + "," + val + ","
				// + p.x + "," + p.y);
			} else {
				log.info("Invalid format: " + dataRow);
			}
			dataRow = reader.readLine(); // read next line
		}
		reader.close();
	}

	public Point latLong2Pixel(Double latV, Double longV) {
		int nRows = params.getNumOfRows();

		MathContext context = new MathContext(5);
		System.out.println("nrows = " + nRows);
		int x = (nRows - 1)
				- (int) ((BigDecimal.valueOf(latV)).subtract(
						BigDecimal.valueOf(params.swLat), context).divide(
						BigDecimal.valueOf(params.latUnit), context)
						.doubleValue());

		int y = (int) ((BigDecimal.valueOf(longV)).subtract(
				BigDecimal.valueOf(params.swLong), context).divide(
				BigDecimal.valueOf(params.longUnit), context).doubleValue());
		/*
		 * BigDecimal z = (BigDecimal.valueOf(params.swLong).divide(
		 * BigDecimal.valueOf(params.longUnit))); int a = (int)
		 * ((BigDecimal.valueOf(longV)).subtract(z).doubleValue());
		 * 
		 * int m = (int) ((BigDecimal.valueOf(80.23252183)).subtract(
		 * BigDecimal.valueOf(77.3791981), context).divide(
		 * BigDecimal.valueOf(0.01), context).doubleValue());
		 * System.out.println("m is " + m); System.out.println("z is " + z +
		 * "a is " + a); System.out.println("y is " + y);
		 */

		return new Point(x, y);

	}

	public Point2D pixel2LatLong(int x, int y) {

		double lat = params.neLat - (x * params.latUnit);
		double lng = (params.swLong + y * params.longUnit);
		return new Point2D.Double(lat, lng);

	}

	public static void main(String[] args) {
		try {
			long timeWindow = 1000 * 60; // 1mins for testing//*60*24*7; // the
											// last 7 days
			long sync = 1000;
			double latUnit = 2;
			double longUnit = 2;
			double swLat = 20;
			double swLong = 30;
			double neLat = 25;
			double neLong = 35;
			FrameParameters fp = new FrameParameters(timeWindow, sync, latUnit,
					longUnit, swLat, swLong, neLat, neLong);
			String theme = Config.getProperty("sandyTheme");
			// String tempFile =
			// "https://docs.google.com/spreadsheet/pub?key=0Auv1FPgY5UScdHNia09KM2o4c0N5aThqMGRXYU5jbEE&output=html";
			// String tempFile =
			// "https://www.dropbox.com/s/qp1yh7be112wpkn/csvfield.csv";
			String tempFile = "http://eventshop.ics.uci.edu:8004/sln/datasource/csvfield";
			// Emage e1 = new Emage(fp, theme);
			CSVFieldEmageIterator csvEit = new CSVFieldEmageIterator(fp, theme,
					tempFile, DatasourceType.point, SpatialWrapper.count);
			csvEit.run();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		// System.exit(0);
	}

}
