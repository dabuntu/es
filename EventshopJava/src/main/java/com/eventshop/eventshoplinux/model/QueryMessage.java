package com.eventshop.eventshoplinux.model;

import com.google.gson.JsonObject;

/**
 * Created by nandhiniv on 5/28/15.
 */
public class QueryMessage {

    String masterQueryID;
    JsonObject query;

    public QueryMessage(String masterQueryID, JsonObject query) {
        this.masterQueryID = masterQueryID;
        this.query = query;
    }

    public String getMasterQueryID() {
        return masterQueryID;
    }

    public void setMasterQueryID(String masterQueryID) {
        this.masterQueryID = masterQueryID;
    }

    public JsonObject getQuery() {
        return query;
    }

    public void setQuery(JsonObject query) {
        this.query = query;
    }


}
