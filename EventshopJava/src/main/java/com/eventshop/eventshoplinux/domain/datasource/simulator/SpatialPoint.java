package com.eventshop.eventshoplinux.domain.datasource.simulator;

public class SpatialPoint {
	public double lat;
	public double lon;

	public SpatialPoint() {
	}

	public SpatialPoint(double lat, double lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public SpatialPoint(SpatialPoint point) {
		this.lat = point.lat;
		this.lon = point.lon;
	}

	public double getLat() {
		return lat;
	}

	public double getLong() {
		return lon;
	}
}
