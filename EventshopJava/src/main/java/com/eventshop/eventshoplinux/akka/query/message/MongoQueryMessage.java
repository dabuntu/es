package com.eventshop.eventshoplinux.akka.query.message;

/**
 * Created by nandhiniv on 7/16/15.
 */
public class MongoQueryMessage {

    private int dataSourceID;
    private long timeToFilter;
    private long endTimeToFilter;
    private double nelat;
    private double nelong;
    private double swlat;
    private double swlong;
    private double latUnit;
    private double lonUnit;
    private String spatial_wrapper;


    public MongoQueryMessage(int dataSourceID, long timeToFilter, long endTimeToFilter
            , double nelat, double nelong, double swlat, double swlong, double latUnit, double lonUnit, String spatial_wrapper) {
        this.dataSourceID = dataSourceID;
        this.timeToFilter = timeToFilter;
        this.endTimeToFilter = endTimeToFilter;
        this.nelat = nelat;
        this.nelong = nelong;
        this.swlat = swlat;
        this.swlong = swlong;
        this.latUnit = latUnit;
        this.lonUnit = lonUnit;
        this.spatial_wrapper = spatial_wrapper;
    }

    public int getDataSourceID() {
        return dataSourceID;
    }

    public double getNelat() {
        return nelat;
    }

    public double getNelong() {
        return nelong;
    }

    public double getSwlat() {
        return swlat;
    }

    public double getSwlong() {
        return swlong;
    }

    public long getTimeToFilter() {
        return timeToFilter;
    }

    public long getEndTimeToFilter() {
        return endTimeToFilter;
    }

    public double getLonUnit() {
        return lonUnit;
    }

    public double getLatUnit() {
        return latUnit;
    }

    public String getSpatial_wrapper() {
        return spatial_wrapper;
    }
}
