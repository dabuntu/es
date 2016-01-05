package com.eventshop.eventshoplinux.domain.query;

/*
 * Used for Querylist
 */
public class Query {

	private int qID;
	private String queryName;
	private String status;
	private String boundingbox;
	private String dsmasterId;
	private int control;
	private int timeWindow;
	private double latitudeUnit;
	private double longitudeUnit;

	public int getqID() {
		return qID;
	}

	public void setqID(int qID) {
		this.qID = qID;
	}

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDsmasterId() {
		return dsmasterId;
	}

	public void setDsmasterId(String dsmasterId) {
		this.dsmasterId = dsmasterId;
	}

	public int getControl() {
		return control;
	}

	public void setControl(int control) {
		this.control = control;
	}

	public String getBoundingbox() {
		return boundingbox;
	}

	public void setBoundingbox(String boundingbox) {
		this.boundingbox = boundingbox;
	}

	public int getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(int timeWindow) {
		this.timeWindow = timeWindow;
	}

	public double getLatitudeUnit() {
		return latitudeUnit;
	}

	public void setLatitudeUnit(double latitudeUnit) {
		this.latitudeUnit = latitudeUnit;
	}

	public double getLongitudeUnit() {
		return longitudeUnit;
	}

	public void setLongitudeUnit(double longitudeUnit) {
		this.longitudeUnit = longitudeUnit;
	}

}
