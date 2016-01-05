package com.eventshop.eventshoplinux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nandhiniv on 6/2/15.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Alert {
    private int aID;
    private String alertName;
    private String alertType;
    private String theme;
    private String alertSource;
    private String safeSource;
    private String alertMessage;
    private long alertMin;
    private long alertMax;
    private long safeMin;
    private long safeMax;
    private int alertStatus;
    private int user_id;
    private String email;
    private String resultEndpoint;
    private ELocation loc;
    private String boundingBox;
    private double lat;
    private double lng;
    private double radius;



    public double getLat() { return lat; }

    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }

    public void setLng(double lng) { this.lng = lng; }

    public ELocation getLoc() {
        return loc;
    }

    public void setLoc(ELocation loc) {
        this.loc = loc;
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public String getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }

    public int getAlertStatus() {
        return alertStatus;
    }

    public void setAlertStatus(int alertStatus) {
        this.alertStatus = alertStatus;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getaID() {
        return aID;
    }

    public void setaID(int aID) {
        this.aID = aID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlertName() {
        return alertName;
    }

    public void setAlertName(String alertName) {
        this.alertName = alertName;
    }

    public String getAlertType() {
        return alertType;
    }

    public void setAlertType(String alertType) {
        this.alertType = alertType;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public String getAlertSource() {
        return alertSource;
    }

    public void setAlertSource(String alertSource) {
        this.alertSource = alertSource;
    }

    public String getSafeSource() {
        return safeSource;
    }

    public void setSafeSource(String safeSource) {
        this.safeSource = safeSource;
    }

    public long getAlertMin() {
        return alertMin;
    }

    public void setAlertMin(long alertMin) {
        this.alertMin = alertMin;
    }

    public long getAlertMax() {
        return alertMax;
    }

    public void setAlertMax(long alertMax) {
        this.alertMax = alertMax;
    }

    public long getSafeMin() {
        return safeMin;
    }

    public void setSafeMin(long safeMin) {
        this.safeMin = safeMin;
    }

    public long getSafeMax() {
        return safeMax;
    }

    public void setSafeMax(long safeMax) {
        this.safeMax = safeMax;
    }

    public String getResultEndpoint() {
        return resultEndpoint;
    }

    public void setResultEndpoint(String resultEndpoint) {
        this.resultEndpoint = resultEndpoint;
    }

    public String getAlertMessage() { return alertMessage; }

    public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
}
