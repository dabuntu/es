package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIterFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
//tsuji:note [a] adding these
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.io.InputStream;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;

public class KMLIterator extends EmageIterator {
	protected static Log log = LogFactory.getLog(KMLIterator.class);
	Polygon myPoly = new Polygon();

	String fileURL;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;
	int ignoreColorSamplesBeyond;

	boolean isPolygon;

	ArrayList<Point> myPoints = new ArrayList<Point>();

	// FEATURES TODO:
	// 1) Support dates in regular expression of imgURL
	// 2) Ignore Points with colorSamples beyond k; default=colMat.size() i.e.
	// dont ignore.--DONE

	public KMLIterator(FrameParameters fp, String th, String url) {
		params = fp;
		this.theme = th;
		this.fileURL = url;

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		isPolygon = false;
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
				e = getTheEmage();
				e.endTime = new Date(curWindowEnd);
				e.startTime = new Date(curWindowEnd - params.timeWindow);
				queue.add(e);
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

	public Emage getTheEmage() {
		try {
			readPointsFromKMLFile();
		} catch (IOException e2) {
			log.error("error in read points form kml file: " + e2.getMessage());
		}
		int nRows = params.getNumOfRows();
		int nCols = params.getNumOfColumns();

		double[][] dataGrid = new double[nRows][nCols];

		for (int i = 0; i < nRows; i++) {
			for (int j = 0; j < nCols; j++) {

				if (isPolygon) {
					if (myPoly.contains(new Point(i, j))) {
						dataGrid[i][j] = 255;
					} else {
						dataGrid[i][j] = 0;
					}
				} else {
					dataGrid[i][j] = 255 / (Math.pow(2, 0.1 * nearestLoc(i, j)));

				}

			}
		}

		try {
			gridToEmage(dataGrid, theme); // purely for visualizing/ debugging
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		Emage e1 = new Emage(params, theme);
		e1.image = dataGrid;
		return e1;
	}

	private double nearestLoc(int i, int j) {
		Point inPoint = new Point(i, j);
		double minDist = 9999999;
		for (int s = 0; s < myPoints.size(); s++) {
			if (inPoint.distance(myPoints.get(s)) < minDist)
				minDist = inPoint.distance(myPoints.get(s));
		}
		return minDist;
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
		SampleModel sm = dummyImage.getSampleModel();
		int nbands = sm.getNumBands();

		// We assume that we can get the pixels values in a integer array.
		double[] pixelTemp = new double[nbands];

		// Get an iterator for the image.
		RandomIterFactory.create(dummyImage, null);
		WritableRaster rasterData = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_BYTE, width, height, nbands, new Point(0, 0));

		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				dataGrid[i][j] = (dataGrid[i][j] <= 255) ? dataGrid[i][j] : 255;

				pixelTemp[0] = dataGrid[i][j];
				pixelTemp[1] = dataGrid[i][j];
				pixelTemp[2] = dataGrid[i][j];

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

	public void readPointsFromKMLFile() throws IOException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		// tsuji:note [b] change this

		log.info("fileURL: " + fileURL);
		URL url = new URL(fileURL);
		InputStream stream = url.openStream();
		Document doc = null;
		try {
			doc = docBuilder.parse(stream);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.error("parse stream url" + e.getMessage());
		}

		// normalize text representation
		doc.getDocumentElement().normalize();
		log.info("Root element of the doc is "
				+ doc.getDocumentElement().getNodeName());
		NodeList listOfPlacemarks = doc.getElementsByTagName("Placemark");
		// NodeList listOfPlacemarks = doc.getElementsByTagName("coordinates");
		int totalPlacemarks = listOfPlacemarks.getLength();
		log.info("Total no of Placemarks : " + totalPlacemarks);
		for (int s = 0; s < listOfPlacemarks.getLength(); s++) {
			log.info("number of placemark " + listOfPlacemarks.getLength());

			Node placemarkNode = listOfPlacemarks.item(s);
			if (placemarkNode.getNodeType() == Node.ELEMENT_NODE) {
				if (placemarkNode.hasChildNodes()) {
					NodeList detailsOfPlacemark = placemarkNode.getChildNodes();
					for (int j = 0; j < detailsOfPlacemark.getLength(); j++) {
						String coordText = detailsOfPlacemark.item(j)
								.getTextContent();
						StringTokenizer vals = new StringTokenizer(coordText,
								" \n");
						int lenTokens = vals.countTokens();
						if (lenTokens > 1) { // its a polygon
							isPolygon = true;
						} else {
							isPolygon = false;
						}
						for (int ln = 0; ln < lenTokens; ln++) {
							StringTokenizer latLongs = new StringTokenizer(
									vals.nextToken(), " ,");
							Double longV = Double.valueOf(latLongs.nextToken());
							Double latV = Double.valueOf(latLongs.nextToken());

							Point myPoint = latLong2Pixel(latV, longV);
							if (isPolygon)
								myPoly.addPoint(myPoint.x, myPoint.y);
							else
								myPoints.add(myPoint);

						}
					}
				}
			} // end of if clause
			else {
				// Node value =
			}
		}// end of for loop with s var
	}

	public static void main(String[] args) {
		try {
			long timeWindow = 3000;// 1000*60*60*24*7; // the last 7 days
			long sync = 1000;
			double latUnit = 0.1;
			double longUnit = 0.1;
			double swLat = 24;
			double swLong = -125;
			double neLat = 50;
			double neLong = -45;
			FrameParameters fp = new FrameParameters(timeWindow, sync, latUnit,
					longUnit, swLat, swLong, neLat, neLong);
			// String theme = Config.getProperty("hurricaneTheme");
			String theme = "hospitalization";
			String url = "http://eventshop.ics.uci.edu:8004/sln/datasource/cali_counties.kml";
			KMLIterator kit = new KMLIterator(fp, theme, url);
			kit.run();
			/*
			 * try { DocumentBuilderFactory docBuilderFactory =
			 * DocumentBuilderFactory.newInstance(); DocumentBuilder docBuilder
			 * = docBuilderFactory.newDocumentBuilder();
			 * 
			 * //tsuji:note [b] change this
			 * 
			 * //URL url = new URL(Config.getProperty("hurricaneURL")); URL url
			 * = new URL(
			 * "http://eventshop.ics.uci.edu:8004/sln/datasource/cali_counties.kml"
			 * ); InputStream stream = url.openStream(); Document doc =
			 * docBuilder.parse(stream);
			 * 
			 * // normalize text representation doc.getDocumentElement
			 * ().normalize (); log.info("Root element of the doc is " +
			 * doc.getDocumentElement().getNodeName()); NodeList
			 * listOfPlacemarks = doc.getElementsByTagName("coordinates"); int
			 * totalPlacemarks = listOfPlacemarks.getLength();
			 * log.info("Total no of Placemarks : " + totalPlacemarks); for(int
			 * s=0; s<listOfPlacemarks.getLength() ; s++){ Node placemarkNode =
			 * listOfPlacemarks.item(s); if(placemarkNode.getNodeType() ==
			 * Node.ELEMENT_NODE){ if (placemarkNode.hasChildNodes()) { NodeList
			 * detailsOfPlacemark=placemarkNode.getChildNodes();
			 * 
			 * for (int j=0; j<detailsOfPlacemark.getLength(); j++) { String
			 * coordText=detailsOfPlacemark.item(j).getTextContent();
			 * StringTokenizer vals = new StringTokenizer(coordText, " \n"); int
			 * lenTokens=vals.countTokens(); if (lenTokens>1)//its a polygon {
			 * for (int ln=0; ln<lenTokens; ln++) {
			 * 
			 * StringTokenizer latLongs=new StringTokenizer(vals.nextToken(),
			 * " ,"); Double longV=Double.valueOf(latLongs.nextToken()); Double
			 * latV=Double.valueOf(latLongs.nextToken());
			 * 
			 * Point myPoint=kit.latLong2Pixel(latV, longV);
			 * kit.myPoly.addPoint(myPoint.x , myPoint.y);
			 * 
			 * } } else {
			 * 
			 * } }
			 * 
			 * }
			 * 
			 * }//end of if clause }//end of for loop with s var
			 * 
			 * kit.getTheEmage();
			 * 
			 * }catch (SAXParseException err) { log.error("** Parsing error" +
			 * ", line " + err.getLineNumber () + ", uri " + err.getSystemId
			 * ()); log.error(" " + err.getMessage ()); }catch (SAXException e)
			 * { Exception x = e.getException (); log.error(((x == null) ? e :
			 * x).getMessage()); }catch (Throwable t) {
			 * log.error(t.getMessage()); }
			 */

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		System.exit(0);
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
}
