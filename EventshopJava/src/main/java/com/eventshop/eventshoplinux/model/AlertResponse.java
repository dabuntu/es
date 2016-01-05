package com.eventshop.eventshoplinux.model;


import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

/**
 * Created by nandhiniv on 6/2/15.
 */
@JsonIgnoreProperties
@JsonWriteNullProperties(false)

public class AlertResponse {

    String latlong;
    String geoAddress;
    double value;
    String solutionLatLong;
    String solutionGeoAddress;
    String alertMessage;


    public AlertResponse(String latlong, String geoAddress, double value, String alertMessage) {
        this.latlong = latlong;
        this.value = value;
        this.geoAddress = geoAddress;
        this.alertMessage = alertMessage;
    }

    public AlertResponse(String latlong, double value, String geoAddress, String solutionLatLong, String alertMessage) {
        this.latlong = latlong;
        this.value = value;
        this.geoAddress = geoAddress;
        this.solutionLatLong = solutionLatLong;
        this.alertMessage = alertMessage;
    }

    public String getSolutionLatLong() {
        return solutionLatLong;
    }

    public void setSolutionLatLong(String solutionLatLong) {
        this.solutionLatLong = solutionLatLong;
    }

    public String getLatlong() {
        return latlong;
    }

    public void setLatlong(String latlong) {
        this.latlong = latlong;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getGeoAddress() {
        return geoAddress;
    }

    public void setGeoAddress(String geoAddress) {
        this.geoAddress = geoAddress;
    }

    public String getSolutionGeoAddress() {
        return solutionGeoAddress;
    }

    public void setSolutionGeoAddress(String solutionGeoAddress) {
        this.solutionGeoAddress = solutionGeoAddress;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    @Override
    public String toString() {
        return "AlertResponse{" +
                "latlong='" + latlong + '\'' +
                ", geoAddress='" + geoAddress + '\'' +
                ", value=" + value +
                ", solutionLatLong='" + solutionLatLong + '\'' +
                ", solutionGeoAddress='" + solutionGeoAddress + '\'' +
                '}';
    }
}
