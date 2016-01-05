package com.eventshop.eventshoplinux.domain.datasource.emage;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.JsonObject;

public class STTPoint {
	public String theme; // Theme of this STTPoint

	public double value; // Value carried by this point

	public Date start; // Start time
	public Date end; // End time

	public double latUnit; // Resolution of latitude
	public double longUnit; // Resolution of longitude
	public double latitude; // Latitude
	public double longitude; // Longitude
	private SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public STTPoint(double value, Date start, Date end, double latUnit,
			double longUnit, double latitude, double longitude, String theme) {
		this.value = value;

		this.start = start;
		this.end = end;

		this.latUnit = latUnit;
		this.longUnit = longUnit;
		this.latitude = latitude;
		this.longitude = longitude;

		this.theme = theme;
	}

	@Override
	public String toString() {
		String str = "";
		str += "theme = " + this.theme + "\n";
		str += "start = " + this.start + "\n";
		str += "end = " + this.end + "\n";
		str += "latitude = " + this.latitude + "\n";
		str += "longitude = " + this.longitude + "\n";
		str += "latUnit = " + this.latUnit + "\n";
		str += "longUnit = " + this.longUnit + "\n";
		str = "value = " + this.value + "\n";
		return str;
	}

	public JsonObject toJSON() {
		JsonObject jo = new JsonObject();
		jo.addProperty("theme", theme);
		jo.addProperty("start", dateFormat.format(start));
		jo.addProperty("end", dateFormat.format(end));
		jo.addProperty("latitude", latitude);
		jo.addProperty("longitude", longitude);
		jo.addProperty("latUnit", latUnit);
		jo.addProperty("longUnit", longUnit);
		jo.addProperty("value", value);
		return jo;
	}
}
