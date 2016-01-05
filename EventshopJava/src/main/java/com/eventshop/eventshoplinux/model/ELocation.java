package com.eventshop.eventshoplinux.model;

/**
 * Created by nandhiniv on 5/7/15.
 */
public class ELocation {
    double lon;
    double lat;

    public ELocation(){

    }
    public ELocation(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}