package com.eventshop.eventshoplinux.model;

/**
 * Created by nandhiniv on 6/10/15.
 */
public class Query {

    int query_id;
    int query_creator_id;
    String query_name;
    String query_desc;
    String query_esql;
    int time_window;
    double latitude_unit;
    double longitude_unit;
    String boundingbox;
    String query_status;
    int qid_parent;

    public int getQuery_id() {
        return query_id;
    }

    public void setQuery_id(int query_id) {
        this.query_id = query_id;
    }

    public int getQuery_creator_id() {
        return query_creator_id;
    }

    public void setQuery_creator_id(int query_creator_id) {
        this.query_creator_id = query_creator_id;
    }

    public String getQuery_name() {
        return query_name;
    }

    public void setQuery_name(String query_name) {
        this.query_name = query_name;
    }

    public String getQuery_desc() {
        return query_desc;
    }

    public void setQuery_desc(String query_desc) {
        this.query_desc = query_desc;
    }

    public String getQuery_esql() {
        return query_esql;
    }

    public void setQuery_esql(String query_esql) {
        this.query_esql = query_esql;
    }

    public int getTime_window() {
        return time_window;
    }

    public void setTime_window(int time_window) {
        this.time_window = time_window;
    }

    public double getLatitude_unit() {
        return latitude_unit;
    }

    public void setLatitude_unit(double latitude_unit) {
        this.latitude_unit = latitude_unit;
    }

    public double getLongitude_unit() {
        return longitude_unit;
    }

    public void setLongitude_unit(double longitude_unit) {
        this.longitude_unit = longitude_unit;
    }

    public String getBoundingbox() {
        return boundingbox;
    }

    public void setBoundingbox(String boundingbox) {
        this.boundingbox = boundingbox;
    }

    public String getQuery_status() {
        return query_status;
    }

    public void setQuery_status(String query_status) {
        this.query_status = query_status;
    }

    public int getQid_parent() {
        return qid_parent;
    }

    public void setQid_parent(int qid_parent) {
        this.qid_parent = qid_parent;
    }
}
