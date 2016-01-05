package com.eventshop.eventshoplinux.domain.common;

import com.google.gson.JsonObject;

import java.math.BigDecimal;
import java.math.MathContext;

public class FrameParameters {
	public long start; // Start time of this frame (single frame)
	public long end; // End time of this frame

	public long timeWindow; // the size of time window for creating a frame
	public long syncAtMilSec; // Offset to do synchronization
	public String timeType; // Past or future
	public String spatial_wrapper;

	public double latUnit; // Resolution of latitude
	public double longUnit; // Resolution of longitude

	public double swLat; // SW corner of the bounding box
	public double swLong;
	public double neLat; // NE corner of the bounding box
	public double neLong;

	public int numOfColumns; // The derived number of columns
	public int numOfRows; // The derived number of rows


	public String dsQuery; // Customer QUery by user...



	public boolean genEmage;

	public boolean getGenEmage() {
		return genEmage;
	}

	public void setGenEmage(boolean genEmage) {
		this.genEmage = genEmage;
	}

	MathContext context = new MathContext(5);

	public FrameParameters(long timeWindow, long syncAtMilSec, double latUnit,
			double longUnit, double swLat, double swLong, double neLat,
			double neLong) {
		this.timeWindow = timeWindow;
		this.syncAtMilSec = syncAtMilSec;

		this.latUnit = latUnit;
		this.longUnit = longUnit;
		this.swLat = swLat;
		this.swLong = swLong;
		this.neLat = neLat;
		this.neLong = neLong;

		calcRowsColumns();
	}

	public FrameParameters() {

	}

	public void calcRowsColumns() {
		double columns = (BigDecimal.valueOf(swLong))
				.subtract(BigDecimal.valueOf(neLong), context)
				.divide(BigDecimal.valueOf(longUnit), context).doubleValue();
		double rows = (BigDecimal.valueOf(swLat))
				.subtract(BigDecimal.valueOf(neLat), context)
				.divide(BigDecimal.valueOf(latUnit), context).doubleValue();

		this.numOfColumns = (int) Math.ceil(Math.abs(columns));
		this.numOfRows = (int) Math.ceil(Math.abs(rows));

		System.out.println("Columns inside calcRowsColumns = "
				+ this.numOfColumns);
		System.out.println("rows = " + this.numOfRows);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		final FrameParameters other = (FrameParameters) obj;
		if (this.timeWindow != other.timeWindow)
			return false;
		if (this.latUnit != other.latUnit)
			return false;
		if (this.longUnit != other.longUnit)
			return false;
		if (this.swLat != other.swLat)
			return false;
		if (this.swLong != other.swLong)
			return false;
		if (this.neLat != other.neLat)
			return false;
		if (this.neLong != other.neLong)
			return false;
		return true;
	}
//
//	public void setDefaultValue() {
//		this.timeWindow = 1000 * 60 * 5; // *60*24*2;//the last 2 days
//		this.syncAtMilSec = 1000;
//		this.latUnit = 0.1;
//		this.longUnit = 0.1;
//		this.swLat = 24;
//		this.swLong = -125;
//		this.neLat = 50;
//		this.neLong = -66;
//		calNumOfColsRows();
//		// this.numOfColumns = (int)Math.ceil(Math.abs((swLong -
//		// neLong)/longUnit));
//		// this.numOfRows = (int)Math.ceil(Math.abs((swLat - neLat)/latUnit));
//	}

	public String boundingBoxString() {
		String boundboxStr = swLat + "," + swLong + "," + neLat + "," + neLong;
		return boundboxStr;
	}
	public String getDsQuery() {
		return dsQuery;
	}

	public void setDsQuery(String dsQuery) {
		this.dsQuery = dsQuery;
	}


	public String getTimeType() {
		return timeType;
	}

	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public long getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(long timeWindow) {
		this.timeWindow = timeWindow;
	}

	public long getSyncAtMilSec() {
		return syncAtMilSec;
	}

	public void setSyncAtMilSec(long syncAtMilSec) {
		this.syncAtMilSec = syncAtMilSec;
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

	public String getSpatial_wrapper() {
		return spatial_wrapper;
	}

	public void setSpatial_wrapper(String spatial_wrapper) {
		this.spatial_wrapper = spatial_wrapper;
	}

	public void calNumOfColsRows() {
		if (longUnit != 0) {
			double columns = (BigDecimal.valueOf(swLong))
					.subtract(BigDecimal.valueOf(neLong), context)
					.divide(BigDecimal.valueOf(longUnit), context)
					.doubleValue();
			numOfColumns = (int) Math.ceil(Math.abs(columns));
		} else {
			numOfColumns = 0;
		}
		if (latUnit != 0) {
			double rows = (BigDecimal.valueOf(swLat))
					.subtract(BigDecimal.valueOf(neLat), context)
					.divide(BigDecimal.valueOf(latUnit), context).doubleValue();
			this.numOfRows = (int) Math.ceil(Math.abs(rows));
		} else {
			numOfRows = 0;
		}
	}

	public int getNumOfColumns() {
		// System.out.println("swLong: " + swLong + ", neLong: " + neLong +
		// ", unit: " + longUnit);
		if (longUnit != 0) {
			double columns = (BigDecimal.valueOf(swLong))
					.subtract(BigDecimal.valueOf(neLong), context)
					.divide(BigDecimal.valueOf(longUnit), context)
					.doubleValue();
			numOfColumns = (int) Math.ceil(Math.abs(columns));
		} else {
			numOfColumns = 0;
		}

		return numOfColumns;
	}

	public void setNumOfColumns(int numOfColumns) {
		this.numOfColumns = numOfColumns;
	}

	public int getNumOfRows() {
		// System.out.println("swLat: " + swLat + ", neLat: " + neLat +
		// ", unit: " + latUnit);

		if (latUnit != 0) {
			double rows = (BigDecimal.valueOf(swLat))
					.subtract(BigDecimal.valueOf(neLat), context)
					.divide(BigDecimal.valueOf(latUnit), context).doubleValue();
			this.numOfRows = (int) Math.ceil(Math.abs(rows));
		} else {
			numOfRows = 0;
		}
		return numOfRows;
	}

	public void setNumOfRows(int numOfRows) {
		this.numOfRows = numOfRows;
	}

	public JsonObject toJson() {
		JsonObject jo = new JsonObject();
		jo.addProperty("timeWindow", this.timeWindow);
		jo.addProperty("syncAtMilSec", this.syncAtMilSec);
		jo.addProperty("timeType", this.timeType);
		jo.addProperty("spatial_wrapper", this.spatial_wrapper);
		jo.addProperty("latUnit", this.latUnit);
		jo.addProperty("longUnit", this.longUnit);
		jo.addProperty("swLat", this.swLat);
		jo.addProperty("swLong", this.swLong);
		jo.addProperty("neLat", this.neLat);
		jo.addProperty("neLong", this.neLong);
		jo.addProperty("numOfColumns", this.numOfColumns);
		jo.addProperty("numOfRows", this.numOfRows);
		return jo;
	}
}
