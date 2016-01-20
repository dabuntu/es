package com.eventshop.eventshoplinux.webservice;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.model.Alert;
import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.model.EnableAlert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by nandhiniv on 6/2/15.
 */


/**
 * This class is a webservice to register, enable and disable alerts.
 *
 * Methods:
 * Public String createAlert(Alert alert)
 * This is used to register a new alert.
 *
 * public String createCustomAlert(Alert alert)
 * This is used to register a new alert with a custom bounding box
 *
 * public boolean enableAlert(EnableAlert enableAlert)
 * This is used to enable an alert
 *
 * public boolean disableAlert(EnableAlert enableAlert)
 * This is used to disable an alert
 *
 * public String resultAlert(String str)
 * This is used to return a result string which is a response to a request
 *
 * private String calculateBoundingBox(ELocation loc, float radius)
 * This is used to calculate a bounding box given a location and radius
 *
 * private double round(double value, int places)
 * This is used to round off a decimal point number to a given decimal place.
 *
 */

@Path("/alertwebservice")
public class AlertWebservice {

    private final static Logger LOGGER = LoggerFactory.getLogger(AlertWebservice.class);
    AlertDAO alertDAO = new AlertDAO();


    /**
     *
     * @param alert
     * A object of alert type. It contains all the fields which exist in Alerts table. These information is needed to register an alert, however certain fields can  be left blank.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registerAlert")
    public String createAlert(Alert alert) {
        LOGGER.info("In Register alerts: " + alert);
        String message;
        String alertMessage = alert.getAlertMessage();
//        System.out.println(alertMessage.replace("{$coordinate}", "REPLACED"));
        alertMessage = alertMessage.replaceAll("\\{[^}]*\\}", "");
        alertMessage = alertMessage.replaceAll("  ", " ");
       // System.out.println("alert message is "+alertMessage);
        if (alertMessage.length() < 50) {
            alertDAO.registerAlert2(alert);
//            String s = "Alert with id : " + (alertDAO.getMaxAlertId()) + " is created.";//TODO Fix alert query
            message = "{\"AlertId\" : " + alertDAO.getMaxAlertId() + "}";
            return message;
        } else {
            message = "{\"Error\" : \"Alert Message should contain less than 50 characters.\" }";
            return message;
        }

    }



    /**
     *
     * @param alert
     * A object of alert type. It contains all the fields which exist in Alerts table. These information is needed to register an alert, however certain fields can  be left blank.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/registerCustomAlert")
    public String createCustomAlert(Alert alert){

        LOGGER.info("In Register alerts: " + alert);
        String message; //Response Message
        String alertMessage = alert.getAlertMessage(); //Alert Operation
        ELocation loc = alert.getLoc();                // Point of interest of alert
        LOGGER.debug("Lat: "+ loc.getLat());
        LOGGER.debug("Long:"+loc.getLon());
        double radius = alert.getRadius();              // Radius of interest
        alert.setBoundingBox(calculateBoundingBox(loc, radius));
//        System.out.println(alertMessage.replace("{$coordinate}", "REPLACED"));
        alertMessage = alertMessage.replaceAll("\\{[^}]*\\}", "");
        alertMessage = alertMessage.replaceAll("  ", " ");
        LOGGER.debug("alert message is "+alertMessage);
        if (alertMessage.length() < 50) {
            boolean regStatus = alertDAO.registerAlert3(alert);
//            String s = "Alert with id : " + (alertDAO.getMaxAlertId()) + " is created.";//TODO Fix alert query
            if (regStatus == true){
                message = "{\"AlertId\" : " + alertDAO.getMaxAlertId() + "}";
                LOGGER.debug("AlertID: "+alertDAO.getMaxAlertId()+" created...");
            }
            else
                message = "Error registering your request. Please try again.";
            return message;
        } else {
            message = "{\"Error\" : \"Alert Message should contain less than 50 characters.\" }";
            return message;
        }
    }


    /**
     *
     * @param enableAlert
     * pass a json which has alertId as the only field.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/enableAlert")
    public boolean enableAlert(EnableAlert enableAlert) {

        AlertDAO alertDAO = new AlertDAO();
        boolean result = alertDAO.activateAlert(enableAlert.getAlertID());
        return result;
    }


    /**
     *
     * @param enableAlert
     * pass a json which has alertId as the only field.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/disableAlert")
    public boolean disableAlert(EnableAlert enableAlert) {

        AlertDAO alertDAO = new AlertDAO();
        boolean result = alertDAO.deactivateAlert(enableAlert.getAlertID());
        return result;
    }



    /**
     *
     * @param str
     * A return message sent as a response to register alerts.
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/resultAlert")
    public String resultAlert(String str) {

        LOGGER.info("Inside Result Alerts ");
        LOGGER.info("Result {}", str);
        return str;

    }

    /**
     *
     * @param loc
     * A latitude and longitude point from which a bounding box can be calculated
     * @param radius
     * radius around the lat long point for which bounding box can be calculated
     * @return
     */
    private String calculateBoundingBox(ELocation loc, double radius) {

        double lat = loc.getLat();
        double lng = loc.getLon();
        double radiusInDegrees= radius/69; //considering miles as the measuring units.
        //double radiusInDegrees = radius/111; //considering km as the measuring units
        LOGGER.debug(round(lat - radiusInDegrees, 5)+","+round(lng - radiusInDegrees, 5)+","+round(lat+radiusInDegrees,5)+","+round(lng + radiusInDegrees, 5));

        return (round(lat - radiusInDegrees, 5)+","+round(lng - radiusInDegrees, 5)+","+round(lat + radiusInDegrees, 5)+","+round(lng + radiusInDegrees, 5));
        // calculation as follows swlat+","+swlong+","+nelat+","+nelong
    }

    /**
     *
     * @param value
     * A decimal point number for which a round off is needed.
     * @param places
     * how many decimal places should the number be rounded off to.
     * @return
     */
    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}





