package com.eventshop.eventshoplinux.model;

import com.mongodb.Mongo;
import org.codehaus.jettison.json.JSONArray;

import java.util.List;

/**
 * Created by aravindh on 6/24/15.
 */
public class MongoResponses {

    JSONArray list;
    public JSONArray getList() {
        return list;
    }

    public void setList(JSONArray list) {
        this.list = list;
    }




}
