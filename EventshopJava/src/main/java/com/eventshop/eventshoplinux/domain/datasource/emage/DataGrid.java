package com.eventshop.eventshoplinux.domain.datasource.emage;

public class DataGrid {
	public double swLat;
	public double swLong;
	public double neLat;
	public double neLong;

	public double[][] data;
	public int row, col;
	public double latUnit, longUnit;

	public DataGrid(double swLat, double swLong, double neLat, double neLong,
			double latUnit, double longUnit) {
		this.swLat = swLat;
		this.swLong = swLong;
		this.neLat = neLat;
		this.neLong = neLong;
		this.latUnit = latUnit;
		this.longUnit = longUnit;
	}

	public void setData(double[][] data) {
		this.data = data;
		this.row = data[0].length;
		this.col = data.length;
	}
}
