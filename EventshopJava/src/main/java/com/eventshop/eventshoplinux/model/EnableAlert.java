package com.eventshop.eventshoplinux.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

/**
 * Created by nandhiniv on 6/8/15.
 */
@JsonIgnoreProperties
public class EnableAlert {

    private int alertID;

    public EnableAlert() {

    }

    public EnableAlert(int alertID) {
        this.alertID = alertID;
    }

    public int getAlertID() {
        return alertID;
    }

    public void setAlertID(int alertID) {
        this.alertID = alertID;
    }
}
