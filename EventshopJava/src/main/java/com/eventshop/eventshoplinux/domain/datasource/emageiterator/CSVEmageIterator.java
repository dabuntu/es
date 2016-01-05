package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.DataGrid;
import com.eventshop.eventshoplinux.domain.datasource.emage.Emage;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.DatasourceType;
import com.eventshop.eventshoplinux.domain.datasource.emage.ResolutionWrapper.SpatialWrapper;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.LinkedBlockingQueue;

/*import stt.FrameParameters;
 import utilities.Config;*/

public class CSVEmageIterator extends EmageIterator {
	protected Log log = LogFactory.getLog(this.getClass().getName());
	String fileURL;

	boolean isRunning;
	LinkedBlockingQueue<Emage> queue;
	int ignoreColorSamplesBeyond;

	ResolutionWrapper resWrapper;
	DataGrid dataGrid;

	// Double swLat, swLong, neLat, neLong; // bounding box of the data in file
	// file

	// FEATURES TODO:
	// 1) Support dates in regular expression of imgURL
	// 2) Ignore Points with colorSamples beyond k; default=colMat.size() i.e.
	// dont ignore.--DONE

	public CSVEmageIterator(FrameParameters fp, String th, String varfileUrl) {
		params = fp; // this is frame parameter of the Emage that the system
						// generated
		this.theme = th;
		this.fileURL = varfileUrl;
		queue = new LinkedBlockingQueue<Emage>();
		isRunning = true;
		initDataGrid(fp.swLat, fp.swLong, fp.neLat, fp.neLong, fp.latUnit,
				fp.longUnit);
	}

	/*
	 * support ResolutionWrapper
	 */

	public void initDataGrid(Double swLat, Double swLong, Double neLat,
			Double neLong, Double latUnit, Double longUnit) {
		dataGrid = new DataGrid(swLat, swLong, neLat, neLong, latUnit, longUnit);
	}

	public void setResolutionWrapper(SpatialWrapper sw) {
		resWrapper = new ResolutionWrapper(DatasourceType.grid, params, sw);
	}

	@Override
	public void run() {
		long curWindowEnd = 0;

		long now = System.currentTimeMillis();
		log.info("params:" + params.toJson().toString());
		curWindowEnd = (long) Math.ceil(now / params.timeWindow)
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
				// log.info("now: " + now + ", curWinEnd: " + curWindowEnd);
				while (now < curWindowEnd) {
					if (!isRunning)
						break;

					try {
						log.info(Thread.currentThread().getId()
								+ " Sleeping for " + (curWindowEnd - now + 1)
								+ "ms");
						Thread.sleep(curWindowEnd - now + 1);

						log.info(Thread.currentThread().getId() + " Woke up");
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

	public Emage getTheEmage() {
		Emage emage = new Emage(params, theme);
		// read data from the file and store in emageGrid
		double[][] emageGrid = readDataFromCSVFile(fileURL);

		// if the rows or columns in data file is mismatch with the expected
		// rows or columns of Emage
		// the gridTransformation is required
		if (emageGrid == null) {
			log.error("ERROR !! something wrong during reading file file");
		} else if (emageGrid.length != params.numOfRows
				|| emageGrid[0].length != params.numOfColumns) {
			if (resWrapper != null) {
				dataGrid.setData(emageGrid);
				resWrapper.setDataGrid(dataGrid);
				resWrapper.doTransformation();
				emageGrid = resWrapper.getEmageGrid();
				emage.image = emageGrid;
				// try {
				// gridToEmage(dataGrid, theme); //purely for visualizing/
				// debugging
				// } catch (IOException e) {
				// log.error(e.getMessage());
				// }
			} else {
				log.error("ERROR !! numbers of rows and/or columns in the data file mismatch, the spatial transformation method required."
						+ "number of rows,cols in frame parameters: "
						+ params.numOfRows
						+ ","
						+ params.numOfColumns
						+ "number or rows,cols in data file: "
						+ emageGrid[0].length + "," + emageGrid.length);
			}
		} else {
			CommonUtil.printAllCellGrid(emageGrid);
			emage.image = emageGrid;
		}
		return emage;
	}

	private double[][] readDataFromCSVFile(String fileURL) {
		// we assume that this file contain array of data
		// each row has the same amount of columns
		// read through the file and put data in the list
		List<Double> tempData = new ArrayList<Double>();
		BufferedReader reader = null;

		try {
			URL url = new URL(fileURL);
			InputStream stream = url.openStream();

			// This is for file reader locally
			// reader = new BufferedReader(new FileReader(fileURL));
			// This is for any file protocol
			// for local file system, use [URL url = new
			// URL("file:/c:/data/test.txt");]
			log.info("fileURL" + fileURL);
			reader = new BufferedReader(new InputStreamReader(stream));
			String myline = "";
			StringTokenizer vals;

			// Loop for Lat Long blocks
			int row = 0;
			int col = 0;
			while ((myline = reader.readLine()) != null) {
				if (myline.contains(",")) {
					vals = new StringTokenizer(myline, ","); // split the value
				} else {
					vals = new StringTokenizer(myline, " ");
				}
				col = vals.countTokens();
				// log.info("loop for lat/long blocks, row: " + row + ", col: "
				// + col);
				for (int i = 0; i < col; i++) {
					String val = vals.nextToken();
					// log.info("val:" + val);
					if (val.contains("e")) { // if the number is presented in
												// exponential notion
						int splitPoint = val.indexOf('e');
						double num = Double.parseDouble(val.substring(0,
								splitPoint - 1));
						double pw = Double.parseDouble(val
								.substring(splitPoint + 1));
						tempData.add(num * Math.pow(10, pw));
					} else {
						tempData.add(Double.parseDouble(val));
					}
				}
				row++;

			}
			log.info("number of rows: " + row + " number of cols: " + col);
			// create 2D array of data from the list
			double[][] data = new double[row][col];
			for (int i = 0; i < row; i++) {
				for (int j = 0; j < col; j++) {
					data[i][j] = tempData.get(i * col + j);
				}
			}
			return data;
		} catch (FileNotFoundException e) {
			log.error(fileURL + "\n" + e.getMessage());
		} catch (IOException e2) {
			log.error(fileURL + "\n" + e2.getMessage());
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
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

	// public PlanarImage gridToEmage(DataGrid dataGrid, String imageName)
	// throws IOException
	// {
	// //creates a gray-scale image of the values
	// int width = dataGrid.data[0].length;
	// int height = dataGrid.data.length;
	//
	// // Get the number of bands on the image.
	// URL imgURL= new URL(Config.getProperty("imgURL") );
	// PlanarImage dummyImage = JAI.create("URL", imgURL);//dummy loaded. date
	// to be edited.
	// SampleModel sm = dummyImage.getSampleModel();
	// int nbands = sm.getNumBands();
	// // We assume that we can get the pixels values in a integer array.
	// double[] pixelTemp = new double[nbands];
	//
	// // Get an iterator for the image.
	// RandomIterFactory.create(dummyImage, null);
	// WritableRaster rasterData =
	// RasterFactory.createBandedRaster(DataBuffer.TYPE_BYTE,
	// width,height,nbands,new Point(0,0));
	//
	// for (int i=0; i<height; i++)
	// {
	// for (int j=0; j<width; j++)
	// {
	// dataGrid.data[i][j] = (dataGrid.data[i][j] <= 10000)?
	// dataGrid.data[i][j]: 10000;
	//
	// pixelTemp[0]=dataGrid.data[i][j];
	// pixelTemp[1]=dataGrid.data[i][j];
	// pixelTemp[2]=dataGrid.data[i][j];
	//
	// rasterData.setPixel(j,i,pixelTemp);
	// }
	// }
	//
	// SampleModel sModel2 =
	// RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,width,height,nbands);
	//
	// // Try to create a compatible ColorModel - if the number of bands is
	// // larger than 4, it will be null.
	// ColorModel cModel2 = PlanarImage.createColorModel(sModel2);
	//
	// // Create a TiledImage using the sample and color models.
	// TiledImage rectImage = new
	// TiledImage(0,0,width,height,0,0,sModel2,cModel2);
	//
	// // Set the data of the tiled image to be the raster.
	// rectImage.setData(rasterData);
	//
	// // Save the image on a file.
	// try {
	// ImageIO.write(rectImage, "jpg", new File(imageName+".jpg"));
	// log.info("New image created: "+ imageName);
	// } catch (IOException e) {
	// log.error(e.getMessage());
	// log.error("ERROr in rectifying  image");
	// }
	//
	// return rectImage;
	// }
}
