package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.awt.Point;

public class DataPoint {
	public Double lat;
	public Double lng;
	public Double value;
	public int xEmageIndex;
	public int yEmageIndex;
	public Point point;

	public DataPoint(Double lat, Double lng, Double value, int x, int y) {
		this.lat = lat;
		this.lng = lng;
		this.value = value;
		this.xEmageIndex = x;
		this.yEmageIndex = y;
		this.point = new Point(x, y);
	}

	public DataPoint(Double lat, Double lng, Double value, Point p) {
		this.lat = lat;
		this.lng = lng;
		this.value = value;
		this.point = p;
		this.xEmageIndex = p.x;
		this.yEmageIndex = p.y;
	}
}
