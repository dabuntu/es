package com.eventshop.eventshoplinux.model;

/**
 * Created by abhisekmohanty on 9/6/15.
 */
public class MapSnapshot {
    private int id;
    private String type;
    private String endPoint;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
