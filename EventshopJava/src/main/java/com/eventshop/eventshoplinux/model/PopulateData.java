package com.eventshop.eventshoplinux.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by nandhiniv on 10/12/15.
 */
@JsonIgnoreProperties
public class PopulateData {

    private int dsID;
    private String data;

    public PopulateData() {

    }

    public PopulateData(int dsID, String data) {
        this.dsID = dsID;
        this.data = data;
    }

    public int getDsID() {
        return dsID;
    }

    public void setDsID(int dsID) {
        this.dsID = dsID;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
