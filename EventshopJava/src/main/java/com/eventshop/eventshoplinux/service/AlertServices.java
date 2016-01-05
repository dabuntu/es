package com.eventshop.eventshoplinux.service;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;
import com.eventshop.eventshoplinux.model.Alert;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.servlets.AlertProcess;
import com.eventshop.eventshoplinux.servlets.RegisterServlet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@Path("/alert")
public class AlertServices {
	protected Log log = LogFactory.getLog(this.getClass().getName());
	private final static Logger LOGGER = LoggerFactory.getLogger(AlertServices.class);

	@PUT
	@Path("/alerts")
	@Consumes(MediaType.APPLICATION_JSON)
	public String createAlert(Alert alert)
			throws IOException {
		LOGGER.info("create alert aid is " + alert.getaID());
		AlertDAO alertDAO = new AlertDAO();
		Boolean status = alertDAO.registerAlert(alert);
		if (status == true) {
			return "Alert created successfully.";
		} else {
			return "Exception in creating Alert";
		}

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/alerts/{id}")
	public Alert getAlert(@PathParam(value = "id") final String srcId) {
		AlertDAO dao = new AlertDAO();
		int srcIdInt = new Integer(srcId).intValue();
		Alert alert = dao.getAlert(srcIdInt);
		return alert;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/alerts")
	/*
	 * This mathod returns datasource list for Admin/based on userid
	 */
	public Alert[] getAlertList(User user) {
		LOGGER.debug("inside /datasourceservice/datasource post");
		Alert[] alertTempArr = new Alert[10];
		Alert[] alertArr;
		AlertDAO dao = new AlertDAO();
		List<Alert> listAlert = dao.getAllAlertList(user);
		alertArr = listAlert.toArray(alertTempArr);
		return alertArr;
	}

	@GET
	@Path("/enablealert/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject enableAlerts(@PathParam(value = "id") final String aId) {
		
		LOGGER.info("Enabling alertId " + aId);
		JSONObject myObject = new JSONObject();
		int alertId; 
		String aType = null;
		String aName = null;
		String srcId = null;
		String srcMin = null;
		String srcMax = null;
		String srcType= null;
		String solId = null;
		String solMin = null;
		String solMax = null;
		String solSrcType=null;
		Boolean solStatus = false;
		Boolean status = false;
		int cId;
		
		AlertDAO alert = new AlertDAO();
		alertId = Integer.parseInt(aId);
		HashMap<String, String> aDetails = alert.getAlertDetails(alertId);
		aType = aDetails.get("alertType");
		aName = aDetails.get("alertName");
		srcId = aDetails.get("alertSrc");
		srcMin = aDetails.get("alertSrcMin");
		srcMax = aDetails.get("alertSrcMax");
		solId =  aDetails.get("safeSrc");
		solMin = aDetails.get("safeSrcMin");
		solMax = aDetails.get("safeSrcMax");

		//Boolean status = alert.activateAlert(alertId);
		LOGGER.debug(alertId + aType + srcId + srcMin + srcMax);
		
		if (srcId.contains("Q")) {
			srcId = srcId.replace("Q","");
			LOGGER.debug(srcId);
			cId = Integer.parseInt(srcId);
			RegisterServlet rs = new RegisterServlet();
			status = rs.getqStatus(srcId);
			//status = alert.checkQueryStatus(cId);
			srcType="Q";
		} else {
			srcId = srcId.replace("ds","");
			cId = Integer.parseInt(srcId);
			//status = alert.checkDsStatus(cId);
			RegisterServlet rs = new RegisterServlet();
			status = rs.getdsStatus(srcId);
			LOGGER.debug("status = " + status);
			/*if(datasrc.getControl() == 1) {
				status = true;
			}*/
			srcType="ds";
		}
		
		LOGGER.debug("aType = " + aType);
		
		if (Integer.parseInt(aType)==2){
			LOGGER.debug("inside NoSoln");
			if(status) {
				AlertProcess ap = new AlertProcess(alertId, srcId,srcType, srcMin, srcMax);
				Thread t = new Thread(ap);
				t.start();
				try {
				    myObject.put("Alert_Id", aId);
				    myObject.put("Alert_Status", "Enabled");
				} catch (JSONException ex) {
				    LOGGER.debug("Exception in Enable json is " + ex);
				}
			} else {
				try {
				    myObject.put("Alert_Id", aId);
				    myObject.put("Alert_Status", "Disabled");
				    myObject.put("Reason","Source/Query not Found");
				} catch (JSONException ex) {
				    LOGGER.info("Exception in Disable json is " + ex);
				}
			}
				
		}else if (Integer.parseInt(aType)==1){
			LOGGER.debug("inside With Soln");
			if(status) {
				if (solId.contains("Q")) {
					solId = solId.replace("Q","");
					LOGGER.debug(solId);
					
					solStatus = alert.checkQueryStatus(Integer.parseInt(solId));
					solSrcType="Q";
				} else {
					solId = solId.replace("ds","");
					
					solStatus = alert.checkDsStatus(Integer.parseInt(solId));
					solSrcType="ds";
				}		
				if(solStatus){
					AlertProcess ap = new AlertProcess(alertId, srcId,srcType, srcMin, srcMax, solId, solSrcType, solMin, solMax, aDetails.get("boundingbox"));
					Thread t = new Thread(ap);
					t.start();
					try {
					    myObject.put("Alert_Id", aId);
					    myObject.put("Alert_Status", "Enabled");
					} catch (JSONException ex) {
					    LOGGER.info("Exception in Enable json is " + ex);
					}	
				}
				else{
					try {
					    myObject.put("Alert_Id", aId);
					    myObject.put("Alert_Status", "Disabled");
					    myObject.put("Reason","Solution DataSource/Query not Found");
					} catch (JSONException ex) {
					    System.out.println("Exception in Disable json is " + ex);
					}	
				}
				
			} else {
				try {
				    myObject.put("Alert_Id", aId);
				    myObject.put("Alert_Status", "Disabled");
				    myObject.put("Reason","DataSource/Query not Found");
				} catch (JSONException ex) {
				    LOGGER.info("Exception in Disable json is " + ex);
				}
			}
		}
		else{
			try {
				myObject.put("Alert_Id", aId);
				myObject.put("Alert_Status", "Cannot Start");
				myObject.put("Reason","Alert Type not found");
			}catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return myObject;
		
	}
	
	@GET
	@Path("/disablealert/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public JSONObject disableAlerts(@PathParam(value = "id") final String aId) {
			AlertDAO alert = new AlertDAO();
			JSONObject jObj = new JSONObject();
			boolean alertStatus = alert.deactivateAlert(Integer.parseInt(aId));
			try {
				if (alertStatus) {
					jObj.put("Alert_Status", "Disabled");
				} else {
					jObj.put("Alert_Status", "Enabled");
					jObj.put("Reason","exception");
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		return jObj;
		
	}
	@GET
	@Path("/getallalerts")
	@Produces(MediaType.APPLICATION_JSON)
	public Alert[] getRegisteredAlerts(@QueryParam("userId") int uId) {
//	public Alert[] getRegisteredAlerts(@PathParam(value = "id") final String uId) {
		//System.out.println(uId);
		/*int usrId = Integer.parseInt(uId);
		JSONObject alertObj = new JSONObject();
		AlertDAO daObj = new AlertDAO();
		alertObj = daObj.getAllAlertList(usrId);
		return alertObj;*/

//		int usrId = Integer.parseInt(uId);
		User user = new User();
		user.setId(uId);
		Alert[] alertListArray = null;
		AlertDAO alertDAO = new AlertDAO();
		List<Alert> alertList = alertDAO.getAllAlertList(user);
		alertListArray = alertList.toArray(new Alert[alertList.size()]);
		return alertListArray;
		
		
		
	}

}