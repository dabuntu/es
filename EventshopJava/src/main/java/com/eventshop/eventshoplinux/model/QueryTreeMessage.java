package com.eventshop.eventshoplinux.model;

import com.google.gson.JsonObject;

import java.util.List;

/**
 * Created by nandhiniv on 5/28/15.
 */
public class QueryTreeMessage {

    String masterQueryID;
    List<JsonObject> query;

    public QueryTreeMessage(String masterQueryID, List<JsonObject> query) {
        this.masterQueryID = masterQueryID;
        this.query = query;
    }

    public List<JsonObject> getQuery() {
        return query;
    }

    public void setQuery(List<JsonObject> query) {
        this.query = query;
    }

    public String getMasterQueryID() {
        return masterQueryID;
    }

    public void setMasterQueryID(String masterQueryID) {
        this.masterQueryID = masterQueryID;
    }


}
