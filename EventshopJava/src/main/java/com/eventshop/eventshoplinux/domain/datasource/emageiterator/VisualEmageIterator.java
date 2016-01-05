package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/*import javax.media.jai.JAI;
 import javax.media.jai.PlanarImage;
 import javax.media.jai.RasterFactory;
 import javax.media.jai.TiledImage;
 import javax.media.jai.iterator.RandomIter;
 import javax.media.jai.iterator.RandomIterFactory;
 */
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;
import com.eventshop.eventshoplinux.util.commonUtil.Config;

public class VisualEmageIterator extends EmageIterator {

	protected Log log = LogFactory.getLog(this.getClass().getName());

	String imgURL;
	double[][] transMat;
	double[][] colorMatrix;
	String maskImgFName;
	int ignoreColorSamplesBeyond;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;

	// FEATURES TODO:
	// 1) Support dates in regular expression of imgURL
	// 2) Ignore Points with colorSamples beyond k; default=colMat.size() i.e.
	// dont ignore.--DONE

	public VisualEmageIterator(FrameParameters fp, String th, String url,
			double[][] TrMat, double[][] ColMat, String maskImgFName,
			int varignoreColorSamplesBeyond) {
		params = fp;
		this.theme = th;
		this.imgURL = url;
		this.transMat = TrMat;
		this.colorMatrix = ColMat;
		this.maskImgFName = maskImgFName;
		this.ignoreColorSamplesBeyond = varignoreColorSamplesBeyond;
		checkDefault();
		printMat(colorMatrix);
		printMat(transMat);
		log.info("mask: " + maskImgFName + ", ignore: "
				+ ignoreColorSamplesBeyond);

		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
	}

	public void printMat(double[][] mat) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < mat.length; i++) { // row
			for (int j = 0; j < mat[0].length; j++) { // col
				sb.append(mat[i][j] + "\t");
			}
			sb.append("\n");
		}
		log.info(sb.toString());
	}

	public void checkDefault() {
		if (ignoreColorSamplesBeyond == 0)
			ignoreColorSamplesBeyond = colorMatrix.length;

		if (maskImgFName == null && this.theme.contains("pollen"))
			maskImgFName = Constant.pollenMASK;
		if (maskImgFName == null && this.theme.contains("aqi"))
			maskImgFName = Constant.aqiMASK;

		if (this.theme.equalsIgnoreCase("aqi")) {
			double[][] TrMatAQI = { { 40.462234530, 36.608742073 },
					{ 0.748263373, 0.222251869 },
					{ -0.489894685, 1.208570277 },
					{ 0.001507530, 0.000538853 },
					{ 0.000193739, -0.000330355 },
					{ 0.000799529, -0.000634850 },
					{ -0.000000258, -0.000000979 },
					{ 0.000000080, 0.000000062 },
					{ -0.000000198, -0.000000100 },
					{ -0.000002263, 0.000001772 }, };
			this.transMat = TrMatAQI;

			double[][] ColMatAQI = { { 46.22, 113.17, 64.82 },
					{ 221.88, 232.50, 32.62 }, { 249.71, 102.66, 4.23 },
					{ 248.93, 2.89, 3.44 }, { 153.07, 1.88, 76.04 },
					{ 130.20, 8.82, 43.29 }, { 178.00, 178.00, 178.00 },
					{ 191.00, 231.00, 255.00 }, { 0, 0, 0 }, };
			this.colorMatrix = ColMatAQI;
		}

	}

	@Override
	public void runFromWrapper() {
		log.info("VisualEmage: runFromWrapper");

		// Is an emage generated?
		boolean emageGenerated = false;

		// Is this the first entry into the following while loop?
		boolean first;

		// To store when the last window ends
		long lastWindowEnd = 0;

		Emage emage = null;
		MathContext context = new MathContext(5);
		while (isRunning) {
			first = true;
			// If there is a carry over Emage?
			// Copy it and set it to be null
			if (lastEmage != null) {
				emage = lastEmage;
				lastEmage = null;
				first = false;
			}

			while (iter.hasNext()) {
				if (isRunning)
					break;

				STTPoint point = iter.next();

				// If this is the first time to enter the loop
				// create an Emage and set parameters accordingly
				if (first) {
					if (point.start.getTime() >= lastWindowEnd) {
						// an Emage is generated
						emage = new Emage(params, iter.theme);
						emage.setStart(point.start.getTime());
						emage.setEnd(point.end.getTime());

						emageGenerated = true;
						first = false;

						lastWindowEnd = point.end.getTime();
					} else
						continue;
				} else {
					// If the STTPoint of a new Emage is found
					// Create a new Emage, and update the value
					if (point.start.getTime() >= emage.endTime.getTime()) {
						lastEmage = new Emage(params, iter.theme);
						lastEmage.setStart(point.start.getTime());
						lastEmage.setEnd(point.end.getTime());

						int row = (int) Math.floor(Math.abs((BigDecimal
								.valueOf(point.latitude))
								.subtract(BigDecimal.valueOf(params.swLat),
										context)
								.divide(BigDecimal.valueOf(params.latUnit),
										context).doubleValue()));
						int col = (int) Math.floor(Math.abs((BigDecimal
								.valueOf(point.longitude))
								.subtract(BigDecimal.valueOf(params.swLong),
										context)
								.divide(BigDecimal.valueOf(params.longUnit),
										context).doubleValue()));
						lastEmage.setValue(col, row, point.value);

						break;
					}
				}

				int row = (int) Math.floor(Math.abs((BigDecimal
						.valueOf(point.latitude))
						.subtract(BigDecimal.valueOf(params.swLat), context)
						.divide(BigDecimal.valueOf(params.latUnit), context)
						.doubleValue()));
				int col = (int) Math.floor(Math.abs((BigDecimal
						.valueOf(point.longitude))
						.subtract(BigDecimal.valueOf(params.swLong), context)
						.divide(BigDecimal.valueOf(params.longUnit), context)
						.doubleValue()));

				emage.setValue(col, row, point.value);
			}

			if (emageGenerated) {
				queue.add(emage);
				emageGenerated = false;
			}
		}
	}

	public PlanarImage createRectifiedImage(PlanarImage Image) {
		int nRows = params.getNumOfRows();
		int nCols = params.getNumOfColumns();
		Point rectifiedPoint;

		// Get the image dimensions of the unrectified image
		int width = Image.getWidth();
		int height = Image.getHeight();
		log.info(nRows + ", " + nCols + "\t" + width + ", " + height);
		// Get an iterator for the image.
		RandomIter iterator = RandomIterFactory.create(Image, null);

		// Get the number of bands on the image.
		SampleModel smO = Image.getSampleModel();
		int nbandsO = smO.getNumBands();

		// We assume that we can get the pixels values in a integer array.
		double[] pixelO = new double[nbandsO];

		// Get an iterator for the image.
		WritableRaster rasterPollenO = RasterFactory.createBandedRaster(
				DataBuffer.TYPE_BYTE, nCols, nRows, nbandsO, new Point(0, 0));

		for (int i = 0; i < nCols; i++) {
			for (int j = 0; j < nRows; j++) {
				rectifiedPoint = this.getMatchingCoord(new Point(i + 1, j + 1));
				if (rectifiedPoint.x >= 1 && rectifiedPoint.x < width
						&& rectifiedPoint.y >= 1 && rectifiedPoint.y < height) {
					iterator.getPixel(rectifiedPoint.x - 1,
							rectifiedPoint.y - 1, pixelO);
					rasterPollenO.setPixel(i, j, pixelO);
					// log.info("setpixel: " + i + ", " + j + ", value " +
					// pixelO[0] + ", " + pixelO[1] + ", " + pixelO[2] + ", " +
					// pixelO[3]);
				} else {

				}
			}
		}

		SampleModel sModel2 = RasterFactory.createBandedSampleModel(
				DataBuffer.TYPE_BYTE, nCols, nRows, nbandsO);

		// Try to create a compatible ColorModel - if the number of bands is
		// larger than 4, it will be null.
		ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

		// Create a TiledImage using the sample and color models.
		TiledImage rectPollenImage = new TiledImage(0, 0, nCols, nRows, 0, 0,
				sModel2, cModel2);

		// log.info(rasterPollenO.getMinX() + ", " + rasterPollenO.getMinY() );
		// Set the data of the tiled image to be the raster.
		rectPollenImage.setData(rasterPollenO);

		// Save the image to a file.
		try {
			ImageIO.write(rectPollenImage, "jpg", new File("rect" + theme
					+ ".jpg"));
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return rectPollenImage;
	}

	public Point getMatchingCoord(Point unrectPoint) {
		double x = unrectPoint.x;
		double y = unrectPoint.y;
		double u = 0.0, v = 0.0;
		u = 1.0 * transMat[0][0] + x * transMat[1][0] + y * transMat[2][0] + x
				* y * transMat[3][0] + x * x * transMat[4][0] + y * y
				* transMat[5][0] + y * x * x * transMat[6][0] + x * y
				* transMat[7][0] + x * x * x * transMat[8][0] + y * y * y
				* transMat[9][0];

		v = 1.0 * transMat[0][1] + x * transMat[1][1] + y * transMat[2][1] + x
				* y * transMat[3][1] + x * x * transMat[4][1] + y * y
				* transMat[5][1] + y * x * x * transMat[6][1] + x * y
				* transMat[7][1] + x * x * x * transMat[8][1] + y * y * y
				* transMat[9][1];

		Point matchedPoint = new Point((int) Math.round(u), (int) Math.round(v));
		// log.debug(unrectPoint.x + ", " + unrectPoint.y + "=>" +
		// matchedPoint.x + ", " + matchedPoint.y);
		return matchedPoint;
	}

	public PlanarImage PostProcessEmage(PlanarImage img) {
		if (maskImgFName != null) {
			int width = img.getWidth();
			int height = img.getHeight();

			// Get the number of bands on the image.
			SampleModel sm = img.getSampleModel();
			int nbands = sm.getNumBands();

			// We assume that we can get the pixels values in a integer array.
			double[] pixel = new double[nbands];

			// Get an iterator for the image.
			RandomIter iterator = RandomIterFactory.create(img, null);
			WritableRaster rasterImage = RasterFactory.createBandedRaster(
					DataBuffer.TYPE_BYTE, width, height, nbands,
					new Point(0, 0));

			double[] pixelNeighb = new double[nbands];
			double[] pixelblack = new double[nbands];
			for (int i = 0; i < nbands; i++) {
				pixelNeighb[i] = 0;
				pixelblack[i] = 0;
			}

			PlanarImage mask = JAI.create("FileLoad", maskImgFName);
			RandomIter iteratorMask = RandomIterFactory.create(mask, null);
			double[] pixelMask = new double[mask.getSampleModel().getNumBands()];

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					iteratorMask.getPixel(i, j, pixelMask);
					if (!isBlack(pixelMask)) {
						iterator.getPixel(i, j, pixel);
						if (isWhite(pixel)) {
							;
						} else {
							rasterImage.setPixel(i, j, pixel);
						}
					} else {
						rasterImage.setPixel(i, j, pixelblack);
					}
				}
			}
			SampleModel sModel2 = RasterFactory.createBandedSampleModel(
					DataBuffer.TYPE_BYTE, width, height, nbands);

			// Try to create a compatible ColorModel - if the number of bands is
			// larger than 4, it will be null.
			ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

			// Create a TiledImage using the sample and color models.
			TiledImage processedImage = new TiledImage(0, 0, width, height, 0,
					0, sModel2, cModel2);

			// Set the data of the tiled image to be the raster.
			processedImage.setData(rasterImage);

			// Save the image to a file.
			try {
				ImageIO.write(processedImage, "jpg", new File("proc_" + theme
						+ ".jpg"));
			} catch (IOException e) {
				log.error(e.getMessage());
			}

			// Save the image on a file.
			return processedImage;
		} else {
			// Save the image to a file.
			try {
				ImageIO.write(img, "jpg", new File("NOTproc_" + theme + ".jpg"));
			} catch (IOException e) {
				log.error(e.getMessage());
			}
			return img;
		}
	}

	public double maxInGrid(double[][] matrix) {
		Double max = 0.0;

		for (int i = 0; i < matrix.length; i++) {

			for (int j = 0; j < matrix[0].length; j++) {
				if (matrix[i][j] > max)
					max = matrix[i][j];
			}
		}
		return max;
	}

	public double[] findNeighPixel(PlanarImage im, int x, int y, int width,
			int height, SampleModel sm, int nbands, RandomIter iterator) {
		double[] SumPixels = new double[nbands];
		double[] pixel = new double[nbands];
		int numPointsUsed = 0;
		int numNeighborsToConsider = 30;
		for (int h = Math.max(y - numNeighborsToConsider, 0); h < Math.min(y
				+ numNeighborsToConsider, height); h++) {
			for (int w = Math.max(x - numNeighborsToConsider, 0); w < Math.min(
					x + numNeighborsToConsider, width); w++) {
				// Get the array of values for the pixel on the w,h coordinate.
				iterator.getPixel(w, h, pixel);
				// Add the values.
				if (!isWhite(pixel) && !isBlack(pixel)) {
					for (int i = 0; i < nbands; i++) {
						SumPixels[i] += pixel[i];
					}
					numPointsUsed++;
				}
			}
		}
		if (numPointsUsed > 0) {
			for (int i = 0; i < nbands; i++) {
				pixel[i] = SumPixels[i] / numPointsUsed;
			}
		} else {
			pixel = new double[] { 0, 0, 0 };

		}

		return pixel;
	}

	public boolean isWhite(double[] pixel) {
		boolean whiteFlag = true;
		for (int i = 0; i < pixel.length; i++) {
			if (pixel[i] < 190)
				whiteFlag = false;

		}
		return whiteFlag;
	}

	public boolean isBlack(double[] pixel) {
		boolean blackFlag = true;
		for (int i = 0; i < pixel.length; i++) {
			if (pixel[i] > 20)
				blackFlag = false;

		}
		return blackFlag;
	}

	public double[][] PlanarImage2DataGrid(PlanarImage img, String EmageType) {
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

		if (ignoreColorSamplesBeyond > -1) {

			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {

					iterator.getPixel(i, j, pixel);

					double curDiff = 999999999;
					double diff = 0;
					int curBucket = -1;
					for (int i2 = 0; i2 < colorMatrix.length; i2++) {
						for (int j2 = 0; j2 < Math.min(nbands,
								colorMatrix[0].length); j2++) {
							diff += (pixel[j2] - colorMatrix[i2][j2])
									* (pixel[j2] - colorMatrix[i2][j2]);
						}
						if (diff < curDiff) {
							curDiff = diff;
							curBucket = i2;
						}
						// log.info("i: " + i2 + ", val: " + diff);
						diff = 0;
					}
					if (curBucket >= ignoreColorSamplesBeyond)
						curBucket = -1;
					BucketArray[j][i] = (curBucket + 1);// *255 /
														// (colorMatrix.length);
														// actually show the
														// categories not
														// normalized to 255
														// now.
					// if(curBucket >= 0) log.info(curBucket + "," +
					// BucketArray[j][i]);
				}
			}
		} else {
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

		}
		return BucketArray;
	}

	@Override
	public void run() {
		long curWindowEnd = 0;

		long now = System.currentTimeMillis();
		curWindowEnd = (long) Math.ceil(now / params.timeWindow)
				* params.timeWindow + params.syncAtMilSec;

		while (isRunning) {
			Emage e = getTheEmage();
			e.endTime = new Date(curWindowEnd);
			e.startTime = new Date(curWindowEnd - params.timeWindow);
			queue.add(e);
			curWindowEnd += params.timeWindow;

			now = System.currentTimeMillis();
			while (now < curWindowEnd) {
				if (!isRunning)
					break;

				try {

					Thread.sleep(curWindowEnd - now + 1);

					now = System.currentTimeMillis();
				} catch (InterruptedException e1) {
					log.error(e1.getMessage());
				}
			}
		}
	}

	public Emage getTheEmage() {
		int numOfRows = params.getNumOfRows();
		int numOfColumns = params.getNumOfColumns();
		double[][] dataGrid = new double[numOfRows][numOfColumns];
		PlanarImage im1;

		try {
			System.out.println("getTheEmage: imgURL " + this.imgURL);
			// im1 = JAI.create("url", new URL(this.imgURL));

			BufferedImage bim1 = ImageIO.read(new URL(this.imgURL));
			im1 = PlanarImage.wrapRenderedImage(bim1);

			File tempFile = new File(theme + "_1.jpg");
			ImageIO.write(im1, "JPEG", tempFile);
			log.info("write image to file " + tempFile.getAbsolutePath());
		} catch (MalformedURLException e2) {
			e2.printStackTrace();
			log.error(e2.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		PlanarImage im2 = JAI.create("FileLoad", theme + "_1.jpg");

		PlanarImage rectImage = this.createRectifiedImage(im2);
		PlanarImage procImage = PostProcessEmage(rectImage);

		dataGrid = PlanarImage2DataGrid(procImage, theme + "_1.jpg");
		// for(int i = 0; i < dataGrid.length; i++)
		// for(int j = 0; j < dataGrid[0].length; j++){
		// System.out.print(", " + dataGrid[i][j]);
		// }
		try {
			gridToEmage(dataGrid, theme); // purely for visualizing/ debugging
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		// printMat(dataGrid);
		Emage e1 = new Emage(params, theme);
		e1.image = dataGrid;
		return e1;
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

		SampleModel sModel2 = javax.media.jai.RasterFactory
				.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height,
						nbands);

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
			log.info("debug save image : " + imageName + ".jpg");

		} catch (IOException e) {
			log.error(e.getMessage());
			log.error("Error in rectifying  image");
		}

		return rectImage;
	}

}
