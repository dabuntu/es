package com.eventshop.eventshoplinux.domain.datasource.simulator;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class DataPoint {
	public String theme;
	public SpatialPoint spatial;
	public long timevalue;

	@XmlTransient
	public Date time;

	public DataPoint() {
		theme = null;
		spatial = new SpatialPoint();
		time = new Date();
		timevalue = time.getTime();
	}

	public String getTheme() {
		return theme;
	}

	public SpatialPoint getSpatial() {
		return spatial;
	}

	public Date getTime() {
		return time;
	}
}
