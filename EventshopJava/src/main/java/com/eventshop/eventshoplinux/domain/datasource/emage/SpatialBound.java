package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.awt.geom.Point2D;

public class SpatialBound {
	Point2D sw;
	Point2D ne;

	public Point2D getSw() {
		return sw;
	}

	public void setSw(Point2D sw) {
		this.sw = sw;
	}

	public Point2D getNe() {
		return ne;
	}

	public void setNe(Point2D ne) {
		this.ne = ne;
	}

}