package com.eventshop.eventshoplinux.util.commonUtil;

import static com.eventshop.eventshoplinux.constant.Constant.CONTEXT;
import static com.eventshop.eventshoplinux.constant.Constant.PATH_DS;
import static com.eventshop.eventshoplinux.constant.Constant.TEMPDIR;
import static com.eventshop.eventshoplinux.constant.Constant.json;

import java.awt.Color;
import java.awt.image.SampleModel;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.domain.common.ConversionMatrix;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.sun.jersey.core.util.Base64;

public class CommonUtil {
	private static Log log = LogFactory.getLog(DataSourceManagementDAO.class
			.getName());

	public static int getEmageStatus(String dsId) {
//		String tempFilePath = Config.getProperty(TEMPDIR) + "ds" + dsId;
//		File tempFile = new File(tempFilePath);
		String vizFilePath = Config.getProperty(CONTEXT) + PATH_DS + dsId
				+ json;
		File vizFile = new File(vizFilePath);
		// log.info("tempFile: " + tempFilePath + ", vizFile: " + vizFilePath);
		if (vizFile.exists()) {
//			if (vizFile.exists()) {
//				return 1; // both temp and viz files are found
//
//			} else {
//				return -1; // only temp is found
//			}
			return 1;
		} else {
			return 0; // no temp file
		}

	}

	public static boolean RenameFile(String oriName, String newName) {
		try {
			File oriFile = new File(oriName);
			if (oriFile.renameTo(new File(newName)))
				return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static String listToCSV(List<String> collectionOfStrings) {
		StringBuilder result = new StringBuilder();
		for (String string : collectionOfStrings) {
			result.append(string);
			result.append(",");
		}
		return result.length() > 0 ? result.substring(0, result.length() - 1)
				: "";
	}

	/*
	 * public static double[][] parsefileToMatrix(String filePath) {
	 * 
	 * double[][] matrix=null; try { BufferedReader reader = new
	 * BufferedReader(new FileReader(filePath)); String line =
	 * reader.readLine(); String[] dim = line.split(","); matrix = new
	 * double[Integer.parseInt(dim[0])][Integer.parseInt(dim[1])]; int row = 0;
	 * 
	 * while((line = reader.readLine()) != null) { String[] numbers =
	 * line.split(","); for(int col = 0; col < numbers.length; ++col)
	 * matrix[row][col] = Double.parseDouble(numbers[col].trim()); row++; }
	 * reader.close();
	 * 
	 * 
	 * } catch(IOException ioe) { ioe.printStackTrace(); }
	 * 
	 * return matrix; }
	 */
	/*
	 * public static void printBlob(InputStream is){ try { String charsetName =
	 * "UTF-8"; char[] buffer = new char[0x1000]; StringBuilder s = new
	 * StringBuilder(); String str = null; Reader r = new InputStreamReader(is,
	 * charsetName); for(int len; (len = r.read(buffer, 0, buffer.length)) !=
	 * -1;) s.append(buffer, 0, len); str = s.toString();
	 * System.out.println(str); } catch(IOException ioe) {
	 * ioe.printStackTrace(); } finally{ try { is.close(); } catch (IOException
	 * e) { e.printStackTrace(); } } }
	 */

	public static ConversionMatrix parsefileToMatrix(BufferedReader bfReader) {

		ConversionMatrix conversionMatrix = new ConversionMatrix();
		double[][] matrix = null;
		try {
			String line = bfReader.readLine();
			String[] dim = line.split(",");
			conversionMatrix.setRow(Integer.parseInt(dim[0]));
			conversionMatrix.setColumn(Integer.parseInt(dim[1]));

			matrix = new double[Integer.parseInt(dim[0])][Integer
					.parseInt(dim[1])];
			int row = 0;

			while ((line = bfReader.readLine()) != null) {
				String[] numbers = line.split(",");
				for (int col = 0; col < numbers.length; ++col)
					matrix[row][col] = Double.parseDouble(numbers[col].trim());
				row++;
			}
			conversionMatrix.setMatrix(matrix);
			bfReader.close();

		} catch (IOException ioe) {
			// conversionMatrix = parsefileToMatrix(filePath);
			ioe.printStackTrace();
		}

		return conversionMatrix;
	}

	public static ConversionMatrix parsefileToMatrix(String filePath) {
		ConversionMatrix conversionMatrix = new ConversionMatrix();
		double[][] matrix = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(filePath));
			String line = reader.readLine();
			String[] dim = line.split(",");
			matrix = new double[Integer.parseInt(dim[0])][Integer
					.parseInt(dim[1])];
			int row = 0;

			while ((line = reader.readLine()) != null) {
				String[] numbers = line.split(",");
				for (int col = 0; col < numbers.length; ++col)
					matrix[row][col] = Double.parseDouble(numbers[col].trim());
				row++;
			}
			reader.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		conversionMatrix.setMatrix(matrix);
		return conversionMatrix;
	}

	public static String matrixtoString(double[][] matrix) {
		String matrixStr = "{";
		for (int x = 0; x < matrix.length; x++) {
			matrixStr += "[";
			for (int y = 0; y < matrix[x].length; y++) {
				matrixStr += matrix[x][y] + ", ";
			}
			matrixStr.substring(0, matrixStr.length() - 1);

			if (x != matrix.length - 1) {
				matrixStr += "},";
			} else {
				matrixStr += "}";
			}
			matrixStr += "\n";
		}
		matrixStr += " }";
		return matrixStr;
	}

	public static String parseFiletoString(String filePath) {
		ConversionMatrix conversionMatrix = parsefileToMatrix(filePath);

		double[][] matrix = conversionMatrix.getMatrix();
		String matrixStr = matrixtoString(matrix);

		return matrixStr;
	}

	public static void convertJSONToFile(String file_string, String file_name)
			throws IOException {
		byte[] bytes = Base64.decode(file_string);
		File file = new File("local_path/" + file_name); // temp_dir from config
		FileOutputStream fop = new FileOutputStream(file);
		fop.write(bytes);
		fop.flush();
		fop.close();
	}

	/*
	 * Add by Siripen 12/02/2013
	 */

	public static void writeToFile(String fileName, String toWrite,
			boolean append) {
		try {
			FileWriter fstream = new FileWriter(fileName, append);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(toWrite);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROr in rectifying AQI image");
		}
	}

	public static void writeJSONFile(String fileName, String toWrite) {
		// append????
		try {
			FileWriter fstream = new FileWriter(fileName + ".json", true);
			BufferedWriter out = new BufferedWriter(fstream);
			out.write(toWrite);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("ERROr in rectifying AQI image");
		}
	}

	public static String readFile(String fileName) {
		BufferedReader reader = null;
		StringBuilder str = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(fileName));
			if (reader != null) {
				String dataRow = reader.readLine();
				while (dataRow != null) {
					str.append(dataRow);
					dataRow = reader.readLine(); // read next line;
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return str.toString();
	}

	public static JsonObject getEmageJSON(String theme, String emagePath,
			String emageDetailPath, FrameParameters fp) {
		JsonObject result = new JsonObject();
		File emageFile = new File(emagePath);
		File emageDetailFile = new File(emageDetailPath);
		try {
			if (!emageFile.exists()) {
				// If emage (image) file doesn't exist, we cannot continue
				System.err.println("ERROR!!! " + emagePath + " doesn't exist");
			} else if (!emageDetailFile.exists()) {
				// If emageDetil (json) file doesn't exist, we cannot continue
				System.err.println("ERROR!!! " + emageDetailPath
						+ " doesn't exist");
			} else {
				// load minVal and maxVal from the emageDetail file
				JsonParser parser = new JsonParser();

				JsonObject obj = (JsonObject) parser.parse(new FileReader(
						emageDetailPath));
				double minVal = Double
						.parseDouble(obj.get("minVal").toString());
				double maxVal = Double
						.parseDouble(obj.get("maxVal").toString());
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						"E MMM d hh:mm:ss yyyy");
				java.util.Date parsedDate = dateFormat.parse(obj
						.get("ts_start").toString());
				java.sql.Timestamp startTime = new java.sql.Timestamp(
						parsedDate.getTime());
				parsedDate = dateFormat.parse(obj.get("ts_end").toString());
				java.sql.Timestamp endTime = new java.sql.Timestamp(
						parsedDate.getTime());

				// Whether to do coloring to different groups of E- mages
				boolean coloring = false;
				if (obj.get("coloring") != null)
					coloring = Boolean.parseBoolean(obj.get("coloring")
							.toString());
				Map<Color, Double> colorMap = null;
				if (coloring) {
					// Color codes given to different groups of E- mages
					colorMap = new HashMap<Color, Double>();
					JsonArray codes = (JsonArray) obj.get("color_codes");
					for (int i = 0; i < codes.size(); i++) {
						Color color;
						try {
							Field field = Class.forName("java.awt.Color")
									.getField(codes.get(i).toString());
							color = (Color) field.get(null);
						} catch (Exception e) {
							color = null; // Not defined
						}
						colorMap.put(color, (double) i);
					}
				}
				// load emage file
				PlanarImage queryEmage = JAI.create("fileload", emagePath); // dummy
																			// loaded.
																			// date
																			// to
																			// be
																			// edited.
				// double[] dataArray = PlanarImage2Array(queryEmage, minVal,
				// maxVal, colorMap);
				double[] dataArray = PlanarImage2DataArray(queryEmage);
				String data = Arrays.toString(dataArray);
				data = data.substring(1);
				data = data.substring(0, data.length() - 2);
				result.addProperty("theme", theme);
				result.addProperty("emagePath", emagePath);
				result.addProperty("startTime", startTime.getTime());
				result.addProperty("endTime", endTime.getTime());
				result.addProperty("latUnit", fp.latUnit);
				result.addProperty("longUnit", fp.longUnit);
				result.addProperty("col", queryEmage.getWidth());
				result.addProperty("row", queryEmage.getHeight());
				result.addProperty("min", minVal);
				result.addProperty("max", maxVal);
				result.addProperty("image", data);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return result;
	}

	public static Double getEmageValueAt(String emagePath,
			String emageDetailPath, FrameParameters fp, double lat, double lon) {
		Double result = null;
		File emageFile = new File(emagePath);
		File emageDetailFile = new File(emageDetailPath);
		try {
			if (!emageFile.exists()) {
				// If emage (image) file doesn't exist, we cannot continue
				System.err.println("ERROR!!! " + emagePath + " doesn't exist");
			} else if (!emageDetailFile.exists()) {
				// If emageDetil (json) file doesn't exist, we cannot continue
				System.err.println("ERROR!!! " + emageDetailPath
						+ " doesn't exist");
			} else {
				// load minVal and maxVal from the emageDetail file
				JsonParser parser = new JsonParser();

				JsonObject obj = (JsonObject) parser.parse(new FileReader(
						emageDetailPath));
				double minVal = Double
						.parseDouble(obj.get("minVal").toString());
				double maxVal = Double
						.parseDouble(obj.get("maxVal").toString());

				// Whether to do coloring to different groups of E- mages
				boolean coloring = false;
				if (obj.get("coloring") != null)
					coloring = Boolean.parseBoolean(obj.get("coloring")
							.toString());
				Map<Color, Double> colorMap = null;
				if (coloring) {
					// Color codes given to different groups of E- mages
					colorMap = new HashMap<Color, Double>();
					JsonArray codes = (JsonArray) obj.get("color_codes");
					for (int i = 0; i < codes.size(); i++) {
						Color color;
						try {
							Field field = Class.forName("java.awt.Color")
									.getField(codes.get(i).toString());
							color = (Color) field.get(null);
						} catch (Exception e) {
							color = null; // Not defined
						}
						colorMap.put(color, (double) i);
					}
				}
				// load emage file
				PlanarImage queryEmage = JAI.create("fileload", emagePath);

				// Get the number of bands on the image. (e.g. 3 bands for RGB
				// image)
				SampleModel sm = queryEmage.getSampleModel();
				int nbands = sm.getNumBands();

				// We assume that we can get the pixels values in a integer
				// array.
				double[] pixel = new double[nbands];

				// Get an iterator for the image.
				int row = (int) Math
						.ceil(Math.abs(lat - fp.swLat) / fp.latUnit);
				int col = (int) Math.ceil(Math.abs(lon - fp.swLong)
						/ fp.longUnit);
				RandomIter iterator = RandomIterFactory
						.create(queryEmage, null);
				iterator.getPixel(col, row, pixel);

				if (nbands == 1) { // Grayscale image
					result = minVal
							+ (((int) pixel[0] * (maxVal - minVal)) / 255);
					System.out.println("Grayscale image");
				} else if (colorMap != null && nbands == 3) { // RGB image
					Color tmp = new Color((int) pixel[0], (int) pixel[1],
							(int) pixel[2]);
					if (colorMap == null || colorMap.size() == 0)
						colorMap = getDefaultColorMap();
					result = colorMap.get(tmp);
					System.out.println("RGB image");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static double[] PlanarImage2DataArray(PlanarImage img) {
		int width = img.getWidth();
		int height = img.getHeight();
		double[] array = new double[height * width];

		// Get the number of bands on the image.
		SampleModel sm = img.getSampleModel();
		int nbands = sm.getNumBands();

		// We assume that we can get the pixels values in a integer array.
		double[] pixel = new double[nbands];

		// Get an iterator for the image.
		RandomIter iterator = RandomIterFactory.create(img, null);

		System.out.println("Ignoring any binning. Going with gray values");
		for (int i = 0; i < height; i++) {
			// System.out.println("Row: "+i);
			for (int j = 0; j < width; j++) {
				int sumChannels = 0;

				iterator.getPixel(j, i, pixel);
				for (int j2 = 0; j2 < nbands; j2++) {
					sumChannels += pixel[j2];
				}
				// BucketArray[i][j] = (sumChannels) / (nbands);
				array[(i * width) + j] = (sumChannels) / (nbands);

				// System.out.print(BucketArray[i][j]+", ");
			}
		}

		return array;
	}

	public static double[] PlanarImage2Array(PlanarImage img, double minVal,
			double maxVal, Map<Color, Double> colorMap) {
		int h = img.getHeight();
		int w = img.getWidth();
		double[] array = new double[h * w];

		// Get the number of bands on the image. (e.g. 3 bands for RGB image)
		SampleModel sm = img.getSampleModel();
		int nbands = sm.getNumBands();

		// We assume that we can get the pixels values in a integer array.
		double[] pixel = new double[nbands];

		// Get an iterator for the image.
		RandomIter iterator = RandomIterFactory.create(img, null);
		double valueRange = maxVal - minVal;
		for (int i = 0; i < h; i++) {
			for (int j = 0; j < w; j++) {
				iterator.getPixel(j, i, pixel);
				// if(nbands == 1){ // Grayscale image
				array[(i * w) + j] = minVal
						+ (((int) pixel[0] * valueRange) / 255);
				System.out.println("Grayscale image, " + valueRange + ", "
						+ (int) pixel[0]);

				// }
				// else if(colorMap != null && nbands == 3){ // RGB image
				// Color tmp = new Color(
				// (int)pixel[0],(int)pixel[1],(int)pixel[2]);
				// if(colorMap == null || colorMap.size() == 0)
				// colorMap = getDefaultColorMap();
				// array[(i*w) + j] = colorMap.get(tmp);
				// System.out.println("RGB image");
				// }
			}
		}
		return array;
	}

	private static Map<Color, Double> getDefaultColorMap() {
		Map<Color, Double> colorMap = new HashMap<Color, Double>();
		colorMap.put(Color.YELLOW, (double) 1); // Yellow
		colorMap.put(Color.RED, (double) 2); // Red
		colorMap.put(new Color(0, 192, 0), (double) 0); // Light Green
		return colorMap;
	}

	public static void printNonZeroCell(double[][] datagrid) {
		for (int i = 0; i < datagrid.length; i++) {
			for (int j = 0; j < datagrid[0].length; j++) {
				if (datagrid[i][j] != 0.0) {
					System.out.println("[" + i + "," + j + "] "
							+ datagrid[i][j]);
				}
			}
		}
	}

	public static void printAllCellList(double[][] datagrid) {
		int rowStep = 1, colStep = 1;
		// we will print at most ten sample
		if (datagrid.length > 10)
			rowStep = datagrid.length / 10;
		if (datagrid[0].length > 10)
			colStep = datagrid[0].length / 10;

		for (int i = 0; i < datagrid.length; i += rowStep) {
			for (int j = 0; j < datagrid[0].length; j += colStep) {
				// System.out.println("[" + i +"," + j + "] " + datagrid[i][j]);
			}
		}
	}

	public static void printAllCellGrid(double[][] datagrid) {
		int rowStep = 1, colStep = 1;
		// we will print at most ten sample
		if (datagrid.length > 10)
			rowStep = datagrid.length / 10;
		if (datagrid[0].length > 10)
			colStep = datagrid[0].length / 10;
		if (rowStep > 1 || colStep > 1)
			// System.out.println("this is a sample of the datagrid, sampling rate rows,cols "
			// + rowStep + "," + colStep);
			for (int i = 0; i < datagrid.length; i += rowStep) {
				for (int j = 0; j < datagrid[0].length; j += colStep) {
					// System.out.println()
					// System.out.format("%8.2f ", datagrid[i][j]);
				}
				// System.out.println();
			}
	}

	public static boolean checkFileExists(String filePath) {
		File file = new File(filePath);
		if (file.exists()) {
			return true;
		}
		return false;
	}

	public static DB connectMongoDB() {
		// Connect to mongodb
		try {
			MongoClient mongo = new MongoClient(
					Config.getProperty("mongoHost"), Integer.parseInt(Config
							.getProperty("mongoPort")));
			// get database, if database doesn't exists, mongodb will create it
			// for you
			return mongo.getDB(Config.getProperty("mongoDB"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		return null;

	}

	// public void postToServlet() {
	// HttpPost post = new
	// HttpPost("http://localhost:8080/ServletExample/SampleServlet");
	// post.setHeader("Content-Type", "application/xml");
	// post.setEntity(new StringEntity(generateNewXML()));
	// HttpClient client = new DefaultHttpClient();
	// HttpResponse response = client.execute(post);
	// }
}
