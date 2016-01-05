package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.awt.geom.Point2D;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Emage {
	public String theme;

	public Date startTime;
	public Date endTime;

	public double latUnit; // Resolution of latitude
	public double longUnit; // Resolution of longitude

	public double swLat;
	public double swLong;
	public double neLat;
	public double neLong;

	public int numOfColumns = 0;
	public int numOfRows = 0;

	public double[][] image;
	public String emageID;

	public double min = 0;
	public double max = 0;
	public String startTimeStr;
	public String endTimeStr;

	public String getTheme() {
		return theme;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public double getLatUnit() {
		return latUnit;
	}

	public void setLatUnit(double latUnit) {
		this.latUnit = latUnit;
	}

	public double getLongUnit() {
		return longUnit;
	}

	public void setLongUnit(double longUnit) {
		this.longUnit = longUnit;
	}

	public double getSwLat() {
		return swLat;
	}

	public void setSwLat(double swLat) {
		this.swLat = swLat;
	}

	public double getSwLong() {
		return swLong;
	}

	public void setSwLong(double swLong) {
		this.swLong = swLong;
	}

	public double getNeLat() {
		return neLat;
	}

	public void setNeLat(double neLat) {
		this.neLat = neLat;
	}

	public double getNeLong() {
		return neLong;
	}

	public void setNeLong(double neLong) {
		this.neLong = neLong;
	}

	public int getNumOfColumns() {
		return numOfColumns;
	}

	public void setNumOfColumns(int numOfColumns) {
		this.numOfColumns = numOfColumns;
	}

	public int getNumOfRows() {
		return numOfRows;
	}

	public void setNumOfRows(int numOfRows) {
		this.numOfRows = numOfRows;
	}

	public double[][] getImage() {
		return image;
	}

	public void setImage(double[][] image) {
		this.image = image;
	}

	public String getEmageID() {
		return emageID;
	}

	public void setEmageID(String emageID) {
		this.emageID = emageID;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public String getStartTimeStr() {
		return startTimeStr;
	}

	public void setStartTimeStr(String startTimeStr) {
		this.startTimeStr = startTimeStr;
	}

	public String getEndTimeStr() {
		return endTimeStr;
	}

	public void setEndTimeStr(String endTimeStr) {
		this.endTimeStr = endTimeStr;
	}

	public boolean isSetMinMax() {
		return setMinMax;
	}

	public void setSetMinMax(boolean setMinMax) {
		this.setMinMax = setMinMax;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public void setDateFormat(SimpleDateFormat dateFormat) {
		this.dateFormat = dateFormat;
	}

	private boolean setMinMax = false;
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public Emage(FrameParameters params, String theme) {
		this.theme = theme;

		latUnit = params.latUnit;
		longUnit = params.longUnit;

		swLat = params.swLat;
		swLong = params.swLong;
		neLat = params.neLat;
		neLong = params.neLong;

		numOfColumns = params.getNumOfColumns();
		numOfRows = params.getNumOfRows();

		image = new double[numOfRows][numOfColumns];
		for (int i = 0; i < numOfRows; i++) {
			for (int j = 0; j < numOfColumns; j++)
				image[i][j] = 0;
		}
	}

	public FrameParameters getParameters() {
		FrameParameters params = new FrameParameters();
		params.end = 0;// ToDo
		params.latUnit = this.latUnit;
		params.longUnit = this.longUnit;
		params.neLat = this.neLat;
		params.neLong = this.neLong;
		params.numOfColumns = this.numOfColumns;
		params.numOfRows = this.numOfRows;
		params.start = 0;// ToDo
		params.swLat = this.swLat;
		params.swLong = this.swLong;
		params.syncAtMilSec = 0;
		params.timeWindow = 0;

		return params;

	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public void setStart(long start) {
		this.startTime = new Date(start);
	}

	public void setEnd(long end) {
		this.endTime = new Date(end);
	}

	public int setValue(int col, int row, double value) {
		if (col >= numOfColumns || row >= numOfRows)
			return -1;

		image[row][col] = value;
		return 0;
	}

	public void setMinMax(double min, double max) {
		this.min = min;
		this.max = max;
		setMinMax = true;
	}

	public void setMinMax() {
		if (image != null) {
			max = -999999999;// a large NEGATIVE number which should get
								// overwritten
			min = 999999999;
			for (int i = 0; i < image.length; i++) {
				for (int j = 0; j < image[0].length; j++) {
					if (image[i][j] > max)
						max = image[i][j];
					if (image[i][j] < min)
						min = image[i][j];
				}
			}
			if (min == -1)
				min = 0;
		} else {
			min = 0;
			max = 0;
		}
		setMinMax = true;
	}

	public JsonObject toJson() {
		JsonObject emage = new JsonObject();
		emage.addProperty("theme", this.theme);
		emage.addProperty("startTime", this.startTime.getTime());
		emage.addProperty("endTime", this.endTime.getTime());
		emage.addProperty("startTimeStr", dateFormat.format(startTime));
		emage.addProperty("endTimeStr", dateFormat.format(endTime));
		emage.addProperty("latUnit", this.latUnit);
		emage.addProperty("longUnit", this.longUnit);
		emage.addProperty("swLat", this.swLat);
		emage.addProperty("swLong", this.swLong);
		emage.addProperty("neLat", this.neLat);
		emage.addProperty("neLong", this.neLong);
		emage.addProperty("row", this.numOfRows);
		emage.addProperty("col", this.numOfColumns);
		if (!setMinMax)
			this.setMinMax();
		emage.addProperty("min", this.min);
		emage.addProperty("max", this.max);

		// Create JsonArray for image
		String gridStr = "[";
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[0].length; j++) {
				gridStr = gridStr + image[i][j] + ",";
			}
		}
		gridStr = gridStr.substring(0, gridStr.length() - 1) + "]";
		JsonArray gridJson = (new JsonParser()).parse(gridStr).getAsJsonArray();
		emage.add("image", gridJson);

		return emage;
	}

	public JsonObject toGeoJson() {
		JsonObject emage = new JsonObject();
		emage.addProperty("theme", this.theme);
		emage.addProperty("startTime", this.startTime.getTime());
		emage.addProperty("endTime", this.endTime.getTime());
		emage.addProperty("startTimeStr", dateFormat.format(startTime));
		emage.addProperty("endTimeStr", dateFormat.format(endTime));
		emage.addProperty("latUnit", this.latUnit);
		emage.addProperty("longUnit", this.longUnit);
		emage.addProperty("swLat", this.swLat);
		emage.addProperty("swLong", this.swLong);
		emage.addProperty("neLat", this.neLat);
		emage.addProperty("neLong", this.neLong);
		emage.addProperty("row", this.numOfRows);
		emage.addProperty("col", this.numOfColumns);
		if (!setMinMax)
			this.setMinMax();
		emage.addProperty("min", this.min);
		emage.addProperty("max", this.max);

		// Create JsonArray for image
		String gridStr = "[";
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[0].length; j++) {
				gridStr = gridStr + image[i][j] + ",";
			}
		}
		gridStr = gridStr.substring(0, gridStr.length() - 1) + "]";
		JsonArray gridJson = (new JsonParser()).parse(gridStr).getAsJsonArray();
		emage.add("image", gridJson);

		emage.add("sttBoxGeoJson", getSTTBoxGeoJson());

		return emage;
	}

	public JsonObject getSTTPointList() {
		JsonArray sttList = new JsonArray();
		Point2D point = null;
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[0].length; j++) {
				// Only list STT where its value is known and more than 0.0
				if (!Double.isNaN(image[i][j]) && image[i][j] > 0.0) {
					point = pixel2LatLong(i, j);
					JsonObject stt = new JsonObject();
					stt.addProperty("longitude", point.getY());
					stt.addProperty("latitude", point.getX());
					stt.addProperty("result", image[i][j]);
					sttList.add(stt);
				}
			}
		}
		JsonObject sttEmage = new JsonObject();
		sttEmage.add("stt", sttList);
		return sttEmage;
	}

	public JsonObject getSTTBoxList() {
		JsonArray sttList = new JsonArray();
		Point2D point = null;
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[0].length; j++) {
				// Only list STT where its value is known and more than 0.0
				if (!Double.isNaN(image[i][j]) && image[i][j] > 0.0) {
					point = pixel2LatLong(i, j);
					JsonObject stt = new JsonObject();
					// swLat, swLong, neLat, neLong
					stt.addProperty(
							"bbox",
							"[" + point.getX() + "," + point.getY() + ","
									+ (point.getX() + latUnit) + ","
									+ (point.getY() + longUnit) + "]");
					stt.addProperty("result", image[i][j]);
					sttList.add(stt);
				}
			}
		}
		JsonObject sttEmage = new JsonObject();
		sttEmage.add("stt", sttList);
		return sttEmage;
	}

	// {
	// "type": "FeatureCollection",
	// "features": [
	// {"geometry": {
	// "type": "GeometryCollection",
	// "geometries": [
	// {
	// "type": "LineString",
	// "coordinates":
	// [[11.0878902207, 45.1602390564],
	// [15.01953125, 48.1298828125]]
	// },
	// {
	// "type": "Polygon",
	// "coordinates":
	// [[[11.0878902207, 45.1602390564],
	// [14.931640625, 40.9228515625],
	// [0.8251953125, 41.0986328125],
	// [7.63671875, 48.96484375],
	// [11.0878902207, 45.1602390564]]]
	// },
	// {
	// "type":"Point",
	// "coordinates":[15.87646484375, 44.1748046875]
	// }
	// ]
	// },
	// "type": "Feature",
	// "properties": {}}
	// ]
	// };

	// NOTE!!! for GeoJson format, the coordinate is longitude, latitude (not
	// lat/long)
	public JsonObject getSTTBoxGeoJson() {
		String geoJsonOpen = "{\"type\":\"FeatureCollection\", \"features\": [";
		String geoMetries = "";
		String geoJsonEnd = " {}]}";

		StringBuilder sb = new StringBuilder();
		Point2D point = null;
		System.out.print("progress [" + image.length + "]: ");
		for (int i = 0; i < image.length; i++) {
			System.out.print(i + ", ");
			for (int j = 0; j < image[0].length; j++) {
				// Only list STT where its value is known and more than 0.0
				if (!Double.isNaN(image[i][j]) && image[i][j] > 0.0) {
					point = pixel2LatLong(i, j);
					double swLat = point.getX();
					double swLong = point.getY();
					// {"geometry":
					// { "type":"Polygon",
					// "coordinates":[[[-122.4,37.8],[-122.4,38],[-122.2,38],[-122.2,37.8]]]
					// },
					// "properties":{"theme":"PM2_5_daily","value":44.8},
					// "type":"Feature"
					// }
					geoMetries = "{\"geometry\":{\"type\":\"Polygon\", "
							+ "\"coordinates\":[[["
							+ swLong
							+ ","
							+ swLat
							+ "],["
							+ swLong
							+ ","
							+ (swLat + latUnit)
							+ "],["
							+ (swLong + longUnit)
							+ ","
							+ (swLat + latUnit)
							+ "],["
							+ (swLong + longUnit)
							+ ","
							+ swLat
							+ "],["
							+ swLong
							+ ","
							+ swLat
							+ "]]]},"
							+ "\"properties\": {\"theme\": \""
							+ this.theme
							+ "\", \"value\": "
							+ image[i][j]
							+ "}, \"type\":\"Feature\"},";
					sb.append(geoMetries);
				}
			}
		}
		System.out.println();

		if (geoMetries.length() > 1)
			geoMetries = geoMetries.substring(0, geoMetries.length() - 1);
		JsonParser parser = new JsonParser();
		return (JsonObject) parser.parse(geoJsonOpen + sb.toString()
				+ geoJsonEnd);

	}

	/*
	 * Incorrect format!!! public JsonObject getSTTBoxGeoJson(){ String
	 * geoJsonOpen =
	 * "{ \"type\":\"FeatureCollection\", \"features\": [{\"geometry\":{\"type\":\"GeometryCollection\",\"geometries\":["
	 * ; String geoMetries = ""; String geoJsonEnd =
	 * " ]},\"type\":\"Feature\",\"properties\":{}}]}";
	 * 
	 * Point2D point = null; for(int i = 0; i < image.length; i++){ for(int j =
	 * 0; j < image[0].length; j++){ // Only list STT where its value is known
	 * and more than 0.0 if(!Double.isNaN(image[i][j]) && image[i][j] > 0.0) {
	 * point = pixel2LatLong(i,j); double swLat = point.getX(); double swLong =
	 * point.getY(); geoMetries +=
	 * "{\"type\":\"Polygon\", \"coordinates\":[[["+swLong+","+swLat+"]," +
	 * "["+swLong+","+(swLat+latUnit)+"]," +
	 * "["+(swLong+longUnit)+","+(swLat+latUnit)+"]," +
	 * "["+(swLong+longUnit)+","+swLat+"]]]},"; } } } geoMetries = (geoMetries
	 * == null || geoMetries.length() == 0?geoMetries:geoMetries.substring(0,
	 * geoMetries.length() - 1)); JsonParser parser = new JsonParser();
	 * //System.out.println(geoJsonOpen + geoMetries + geoJsonEnd); return
	 * (JsonObject) parser.parse(geoJsonOpen + geoMetries + geoJsonEnd);
	 * 
	 * }
	 */

	// Convert pixel to latitude and longitude (the point at the lower left
	// corner)
	public Point2D pixel2LatLong(int x, int y) {
		double lat = neLat - (x * latUnit);
		double lng = (swLong + y * longUnit);
		return new Point2D.Double(lat, lng);
	}

	// convert pixel to geo-bounding box
	public SpatialBound pixel2geoBound(Point2D pp) {
		SpatialBound sb = new SpatialBound();
		sb.sw.setLocation(((pp.getX() - swLat) * latUnit),
				((pp.getY() - swLong) * longUnit));
		sb.ne.setLocation(((pp.getX() + 1 - swLat) * latUnit),
				((pp.getY() + 1 - swLong) * longUnit));
		return sb;
	}

	public JsonObject getAllFormat() {
		JsonObject emage = new JsonObject();
		emage.addProperty("theme", this.theme);
		emage.addProperty("startTime", this.startTime.getTime());
		emage.addProperty("endTime", this.endTime.getTime());
		emage.addProperty("startTimeStr", dateFormat.format(startTime));
		emage.addProperty("endTimeStr", dateFormat.format(endTime));
		emage.addProperty("latUnit", this.latUnit);
		emage.addProperty("longUnit", this.longUnit);
		emage.addProperty("swLat", this.swLat);
		emage.addProperty("swLong", this.swLong);
		emage.addProperty("neLat", this.neLat);
		emage.addProperty("neLong", this.neLong);
		emage.addProperty("row", this.numOfRows);
		emage.addProperty("col", this.numOfColumns);
		if (!setMinMax)
			this.setMinMax();
		emage.addProperty("min", this.min);
		emage.addProperty("max", this.max);

		// Create JsonArray for image
		String gridStr = "[";
		for (int i = 0; i < image.length; i++) {
			for (int j = 0; j < image[0].length; j++) {
				gridStr = gridStr + image[i][j] + ",";
			}
		}
		gridStr = gridStr.substring(0, gridStr.length() - 1) + "]";
		JsonArray gridJson = (new JsonParser()).parse(gridStr).getAsJsonArray();
		emage.add("image", gridJson);

		// get sttPoint list
		emage.add("sttPoint", this.getSTTPointList());

		// get sttBox list
		emage.add("sttBox", this.getSTTBoxList());

		// get sttBoxGeoJson
		emage.add("sttBoxGeoJson", this.getSTTBoxGeoJson());
		return emage;
	}

	/*
	 * future: store all Emage in database (e.g., blob in mySQL, etc)
	 */
	// public void storeEmage(){
	// File fBlob = new File ("<your_path>");
	// FileInputStream is = new FileInputStream(fBlob);
	// statement.setBinaryStream(1, is, (int) fBlob.length());
	// statement.execute();
	// }

	public static void main(String[] args) {
		FrameParameters fp = new FrameParameters(1 * 6 * 60 * 1000, 0, 2, 2,
				24, -125, 50, -66);
		Emage e = new Emage(fp, "test");
		double[][] grid = { { 1, 2 }, { 0, 3 }, { 3, 4 } };
		e.image = grid;
		e.setStart(System.currentTimeMillis());
		e.setEnd(System.currentTimeMillis() + 1000);
		e.setMinMax();
		System.out.println(e.getAllFormat().toString());
		JsonObject x = e.getSTTPointList();
		System.out.println(x.toString());
		JsonObject b = e.getSTTBoxGeoJson();
		System.out.println(b.toString());
	}
}
