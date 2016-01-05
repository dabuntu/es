package com.eventshop.eventshoplinux.akka.query;

import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhisekmohanty on 29/6/15.
 */
public class QueryActorMessage {
    private String masterQueryID;
    private JsonObject query;
    private List<Emage> emageList = new ArrayList<Emage>();

    public QueryActorMessage(String masterQueryID, JsonObject query, List<Emage> emageList) {
        this.masterQueryID = masterQueryID;
        this.query = query;
        this.emageList = emageList;
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

    public List<Emage> getEmageList() {
        return emageList;
    }

    public void setEmageList(List<Emage> emageList) {
        this.emageList = emageList;
    }

}
