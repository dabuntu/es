package com.eventshop.eventshoplinux.DAO.alert;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.DAO.user.UserManagementDAO;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.model.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.*;

public class AlertDAO extends BaseDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(AlertDAO.class);
	
	
	
	public boolean disableAllAlerts() {
		PreparedStatement ps = null;
		boolean status = true;
		LOGGER.info("Disable all Alerts at startup");
		try {
			ps = con.prepareStatement(DISABLE_ALL_ALERTS);
			ps.executeUpdate();
			LOGGER.info("Disabled all alerts successfully");
			
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn in disabling all alerts: " + e);
			e.printStackTrace();
			status = false;
		}
		return status;
	}

	public boolean registerAlert(Alert alert) {

		PreparedStatement ps = null;
		LOGGER.info("register Alert aid is " + alert.getaID());
		int type = 0;
		boolean update = false;
		if (alert.getaID() != 0) {
			update = true;
		}
		if ((alert.getAlertType().equalsIgnoreCase("NoSoln"))) {
			type = 2;
		} else {
			type = 1;
		}
		boolean queryStatus = false;
		LOGGER.info("aid : " + alert.getaID());

		try {
			LOGGER.debug("update is " + update);
			if (update) {
				ps = con.prepareStatement(UPDATE_SOL_ALT_QRY);
				ps.setString(1, alert.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alert.getTheme());
				ps.setString(4, alert.getAlertSource());
				ps.setLong(5, alert.getAlertMin());
				ps.setLong(6, alert.getAlertMax());
				ps.setString(7, alert.getSafeSource());
				ps.setLong(8, alert.getSafeMin());
				ps.setLong(9, alert.getSafeMax());
				ps.setString(10, alert.getResultEndpoint());
				ps.setString(11, alert.getAlertMessage());
				ps.setDouble(12, alert.getLat());
				ps.setDouble(13, alert.getLng());
				ps.setDouble(14, alert.getRadius());
				ps.setInt(15, alert.getaID());
				ps.executeUpdate();

			} else {
				ps = con.prepareStatement(INST_SOL_ALT_QRY);
				ps.setString(1, alert.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alert.getTheme());
				ps.setString(4, alert.getAlertSource());
				ps.setLong(5, alert.getAlertMin());
				ps.setLong(6, alert.getAlertMax());
				ps.setInt(7, 0);
				ps.setInt(8, alert.getUser_id());
				ps.setString(9, alert.getSafeSource());
				ps.setLong(10, alert.getSafeMin());
				ps.setLong(11, alert.getSafeMax());
				ps.setString(12, alert.getResultEndpoint());
				ps.setString(13, alert.getAlertMessage());
				ps.setDouble(14, alert.getLat());
				ps.setDouble(15, alert.getLng());
				ps.setDouble(16, alert.getRadius());
				ps.executeUpdate();
			}

		} catch (Exception e) {
			LOGGER.info("exceptionnn e11" + e);
			e.printStackTrace();
			queryStatus = false;
			return queryStatus;
		}

		queryStatus = true;
		return queryStatus;
	}

	public boolean registerAlert2(Alert alertObj) {

		UserManagementDAO userManagementDAO = new UserManagementDAO();
		int userID = userManagementDAO.getUserID(alertObj.getEmail());

		PreparedStatement ps = null;
//		int aId = getMaxAlertId() + 1;
		boolean queryStatus = false;
		int type = 0;
		if (alertObj.getAlertType() != null && (alertObj.getAlertType().equalsIgnoreCase("solution"))) {
			type = 1;
		} else {
			type = 2;
		}


		try {
			if (type == 2) {
				ps = con.prepareStatement(INST_SINGLE_ALT_QRY_WITH_ENDPOINT);
				ps.setString(1, alertObj.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alertObj.getTheme());
				ps.setString(4, alertObj.getAlertSource());
				ps.setLong(5, alertObj.getAlertMin());
				ps.setLong(6, alertObj.getAlertMax());
				ps.setInt(7, 0);
				ps.setInt(8, userID);
				ps.setString(9, alertObj.getResultEndpoint());
				//System.out.println("in registeralert2 alert message is " + alertObj.getAlertMessage());
				ps.setString(10, alertObj.getAlertMessage());
				ps.executeUpdate();
			} else if (type == 1) {
				//System.out.println("type = 1");

				ps = con.prepareStatement(INST_SOL_ALT_QRY_WITH_ENDPOINT);
				//System.out.println(alertObj.get("AlertId").toString() + " trying...");
				ps.setString(1, alertObj.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alertObj.getTheme());
				ps.setString(4, alertObj.getAlertSource());
				ps.setLong(5, alertObj.getAlertMin());
				ps.setLong(6, alertObj.getAlertMax());
				ps.setInt(7, 0);
				ps.setInt(8, userID);
				ps.setString(9, alertObj.getSafeSource());
				ps.setLong(10, alertObj.getSafeMin());
				ps.setLong(11, alertObj.getSafeMax());
				ps.setString(12, alertObj.getResultEndpoint());
				ps.setString(13, alertObj.getAlertMessage());

				// System.out.println("reghister query chilldd");
				ps.executeUpdate();
			}

		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn e11" + e);
			e.printStackTrace();
			queryStatus = false;
			return queryStatus;
		}

		queryStatus = true;
		return queryStatus;
	}


	public boolean registerAlert3(Alert alertObj) {

		UserManagementDAO userManagementDAO = new UserManagementDAO();
		int userID = userManagementDAO.getUserID(alertObj.getEmail());

		PreparedStatement ps = null;
//		int aId = getMaxAlertId() + 1;
		boolean queryStatus = false;
		int type = 0;
		if (alertObj.getAlertType() != null && (alertObj.getAlertType().equalsIgnoreCase("solution"))) {
			type = 1;
		} else {
			type = 2;
		}


		try {
			if (type == 2) {
				ps = con.prepareStatement(INST_SINGLE_ALT_QRY_WITH_ENDPOINT_AND_BOUNDINGBOX);
//				ps.setInt(1, aId);
				ps.setString(1, alertObj.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alertObj.getTheme());
				ps.setString(4, alertObj.getAlertSource());
				ps.setLong(5, alertObj.getAlertMin());
				ps.setLong(6, alertObj.getAlertMax());
				ps.setInt(7, 0);
				ps.setInt(8, userID);
				ps.setString(9, alertObj.getResultEndpoint());
				ps.setString(10, alertObj.getAlertMessage());
				ps.setString(11, alertObj.getBoundingBox());
				ps.setDouble(12, alertObj.getLoc().getLat());
				ps.setDouble(13, alertObj.getLoc().getLon());
				ps.setDouble(14, alertObj.getRadius());
				ps.executeUpdate();
			} else if (type == 1) {

				ps = con.prepareStatement(INST_SOL_ALT_QRY_WITH_ENDPOINT_AND_BOUNDINGBOX);
				//System.out.println(alertObj.get("AlertId").toString() + " trying...");
//				ps.setInt(1, aId);
				ps.setString(1, alertObj.getAlertName());
				ps.setInt(2, type);
				ps.setString(3, alertObj.getTheme());
				ps.setString(4, alertObj.getAlertSource());
				ps.setLong(5, alertObj.getAlertMin());
				ps.setLong(6, alertObj.getAlertMax());
				ps.setInt(7, 0);
				ps.setInt(8, userID);
				ps.setString(9, alertObj.getSafeSource());
				ps.setLong(10, alertObj.getSafeMin());
				ps.setLong(11, alertObj.getSafeMax());
				ps.setString(12, alertObj.getResultEndpoint());
				ps.setString(13, alertObj.getAlertMessage());
				ps.setString(14, alertObj.getBoundingBox());
				ps.setDouble(15, alertObj.getLoc().getLat());
				ps.setDouble(16, alertObj.getLoc().getLon());
				ps.setDouble(17, alertObj.getRadius());

				// System.out.println("reghister query chilldd");
				ps.executeUpdate();
			}

		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("Error:" + e);
			e.printStackTrace();
			queryStatus = false;
			return queryStatus;
		}

		queryStatus = true;
		return queryStatus;
	}

	
	public int getMaxAlertId() {
		// TODO Auto-generated method stub

		PreparedStatement ps = null;
		ResultSet rs = null;
		int aId = 0;
		LOGGER.debug("getting Max Alert id");
		try {
			ps = con.prepareStatement(GET_MAX_ALRT_ID);
			rs = ps.executeQuery();
			while(rs.next()) {
			aId = rs.getInt(1);
			}
//			aId = aId + 1;
			LOGGER.debug("Alertid is " + aId);
			return aId;
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn in enabling alert " + e);
			e.printStackTrace();
		}

		return aId;
	}

	public boolean activateAlert(int alertId) {
		PreparedStatement ps = null;
		boolean status = true;
		LOGGER.debug("enable Alert");
		try {
			ps = con.prepareStatement(ENABLE_ALERT_QRY);
			LOGGER.debug("Trying to enable Alert_Id " + alertId);
			ps.setInt(1, alertId);
			ps.executeUpdate();
			LOGGER.debug("Alert enabley- mbvd");
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn in enabling alert " + e);
			e.printStackTrace();
			status = false;
		}
		return status;
		
	}

	public boolean deactivateAlert(int alertId){
		PreparedStatement ps = null;
		LOGGER.debug("Update Alert");
		boolean queryStatus = false;

		try {
			ps = con.prepareStatement(DISABLE_ALERT_QRY);
			ps.setInt(1, alertId);
			ps.executeUpdate();
			LOGGER.debug("Disabled alert " + alertId);

		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exception :" + e);
			e.printStackTrace();
			queryStatus = false;
			return queryStatus;
		}

		queryStatus = true;
		return queryStatus;
		
	}

	public HashMap<String, String> getAlertDetails(int alertId) {
		// TODO Auto-generated method stub
		HashMap<String, String> alertDetails = new HashMap<String, String>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = con.prepareStatement(GET_ALERT_DTLS);
			LOGGER.debug(" trying to get details");
			ps.setInt(1, alertId);

			rs = ps.executeQuery();
			while(rs.next()) {
				alertDetails.put("alertType", rs.getString(1));
				alertDetails.put("alertName", rs.getString(2));
				alertDetails.put("alertSrc", rs.getString(3));
				alertDetails.put("alertSrcMin",rs.getString(4));
				alertDetails.put("alertSrcMax", rs.getString(5));
				alertDetails.put("safeSrc", rs.getString(6));
				alertDetails.put("safeSrcMin", rs.getString(7));
				alertDetails.put("safeSrcMax", rs.getString(8));
				alertDetails.put("alert_Status", rs.getString(9));
				alertDetails.put("resultEndpoint", rs.getString(10));
				alertDetails.put("alertMessage", rs.getString(11));
				alertDetails.put("boundingbox", rs.getString(12));
				alertDetails.put("lat",rs.getString(13));
				alertDetails.put("lng",rs.getString(14));
				alertDetails.put("radius",rs.getString(15));
			}

		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exception : " + e);
			e.printStackTrace();
		}
		
		
		return alertDetails;
	}


	public Boolean checkQueryStatus(int id) {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		ResultSet rs = null;
		String stat = null;
		boolean status = false;
		LOGGER.debug("checkQueryStatus");

		try {
			ps = con.prepareStatement(QUERY_STAT_QRY);
			LOGGER.debug("Trying to check query stat " + id);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while(rs.next()){
				stat = rs.getString(1);
				LOGGER.debug(stat);
				if(stat.equalsIgnoreCase("s")){
					status = true;
				}
			}
			return status;
			
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn in checking q stat " + e);
			e.printStackTrace();
		}
		return status;
	}


	public Boolean checkDsStatus(int id) {
		// TODO Auto-generated method stub
		PreparedStatement ps = null;
		ResultSet rs = null;
		String stat = null;
		boolean status = false;
		LOGGER.debug("checkDsStatus");

		try {
			ps = con.prepareStatement(DS_STAT_QRY);
			LOGGER.debug("Trying to check ds stat " + id);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while(rs.next()){
				stat = rs.getString(1);
				if (stat.equals("s")) {
					status = true;
				}
			}
			return status;
			
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn in checking ds stat " + e);
			e.printStackTrace();
		}
		return status;
	}
	
	

	public boolean isActive(int id) {
		// TODO Auto-generated method stub
		try {
			LOGGER.debug("Checking for Alert Status: isActive(int id)");
			PreparedStatement ps = null;
			ResultSet rs = null;
			int stat = 0;
			boolean status = false;
			LOGGER.debug("isAlertEnabled");
			LOGGER.debug("Query: " + Alert_STAT_QRY);
		
			ps = con.prepareStatement(Alert_STAT_QRY);
			LOGGER.debug("Trying to check Alert stat " + id);
			ps.setInt(1, id);
			rs = ps.executeQuery();
			while(rs.next()){
				stat = rs.getInt(1);
				LOGGER.debug("Alert Status = " + stat);
				if(stat == 1){
					status = true;
				}
			}
			LOGGER.debug("Status Returned: " + status);
			return status;
			
		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exception in checking Alert stat " + e);
			e.printStackTrace();
			return false;
		}
		
	}

	public List<Alert> getAllAlertList(User user) {
		// TODO Auto-generated method stub

		LOGGER.debug("Inside getAllAlertList()");
		List<Alert> alrtList = new ArrayList<Alert>();
		PreparedStatement ps = null;
		String statusControl = CONTROLFLAG; // Running
		ResultSet rs = null;
		Alert alert = null;

		try {
			ps = con.prepareStatement(GET_ALL_ALERTS_FOR_USER);
			LOGGER.debug("Trying to get alert details for userId " + user.getId());
			ps.setInt(1, user.getId());
			rs = ps.executeQuery();

			while (rs.next()) {
				alert = new Alert();
				alert.setaID(rs.getInt(1));
				alert.setAlertName(rs.getString(2));
				alert.setAlertType(rs.getString(3));
				alert.setTheme(rs.getString(4));
				alert.setAlertSource(rs.getString(5));
				alert.setSafeSource(rs.getString(6));
				alert.setAlertMin(rs.getLong(7));
				alert.setAlertMax(rs.getLong(8));
				alert.setSafeMin(rs.getLong(9));
				alert.setSafeMax(rs.getLong(10));
				alert.setAlertStatus(rs.getInt(11));
				alert.setResultEndpoint(rs.getString(12));
				alert.setAlertMessage(rs.getString(13));
				alert.setLat(rs.getDouble(14));
				alert.setLng(rs.getDouble(15));
				alert.setRadius(rs.getDouble(16));
				/*if (getQueryStatus(rs.getString(1))) {
					query.setControl(1);
					query.setStatus(RUNNING);
				} else {
					query.setControl(0);
					query.setStatus(STOPPED);
				}*/
				alrtList.add(alert);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("Completed getUserQuery()");
		return alrtList;
	}

	public List<Alert> getAllAlertList() {
		// TODO Auto-generated method stub

		LOGGER.debug("Inside getAllAlertList()");
		List<Alert> alrtList = new ArrayList<Alert>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Alert alert = null;

		try {
			ps = con.prepareStatement(GET_ALL_ALERTS);
			rs = ps.executeQuery();

			while (rs.next()) {
				alert = new Alert();
				alert.setaID(rs.getInt(1));
				alert.setAlertName(rs.getString(2));
				alert.setAlertType(rs.getString(3));
				alert.setTheme(rs.getString(4));
				alert.setAlertSource(rs.getString(5));
				alert.setSafeSource(rs.getString(6));
				alert.setAlertMin(rs.getInt(7));
				alert.setAlertMax(rs.getInt(8));
				alert.setSafeMin(rs.getInt(9));
				alert.setSafeMax(rs.getInt(10));
				alert.setAlertStatus(rs.getInt(11));
				alert.setUser_id(rs.getInt(12));
				alert.setResultEndpoint(rs.getString(13));
				alrtList.add(alert);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("Completed getUserQuery()");
		return alrtList;
	}

	public List<Alert> getAllEnabledAlertList() {
		// TODO Auto-generated method stub

		LOGGER.debug("Inside getAllAlertList()");
		List<Alert> alrtList = new ArrayList<Alert>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Alert alert = null;

		try {
			ps = con.prepareStatement(GET_ALL_ENABLED_ALERTS);
			rs = ps.executeQuery();

			while (rs.next()) {
				alert = new Alert();
				alert.setaID(rs.getInt(1));
				alert.setAlertName(rs.getString(2));
				alert.setAlertType(rs.getString(3));
				alert.setTheme(rs.getString(4));
				alert.setAlertSource(rs.getString(5));
				alert.setSafeSource(rs.getString(6));
				alert.setAlertMin(rs.getInt(7));
				alert.setAlertMax(rs.getInt(8));
				alert.setSafeMin(rs.getInt(9));
				alert.setSafeMax(rs.getInt(10));
				alert.setAlertStatus(rs.getInt(11));
				alert.setUser_id(rs.getInt(12));
				alert.setResultEndpoint(rs.getString(13));
				alert.setAlertMessage(rs.getString(14));
				alrtList.add(alert);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("Completed getUserQuery()");
		return alrtList;
	}

	public List<Alert> getAllEnabledAlertListForID(String id) {
		// TODO Auto-generated method stub

		LOGGER.debug("Inside getAllAlertList()");
		List<Alert> alrtList = new ArrayList<Alert>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		Alert alert = null;

		try {
			ps = con.prepareStatement(GET_ALL_ENABLED_ALERTS_FOR_ID);
			ps.setString(1, id);
			ps.setString(2, id);
			rs = ps.executeQuery();

			while (rs.next()) {
				alert = new Alert();
				alert.setaID(rs.getInt(1));
				alert.setAlertName(rs.getString(2));
				alert.setAlertType(rs.getString(3));
				alert.setTheme(rs.getString(4));
				alert.setAlertSource(rs.getString(5));
				alert.setSafeSource(rs.getString(6));
				alert.setAlertMin(rs.getInt(7));
				alert.setAlertMax(rs.getInt(8));
				alert.setSafeMin(rs.getInt(9));
				alert.setSafeMax(rs.getInt(10));
				alert.setAlertStatus(rs.getInt(11));
				alert.setUser_id(rs.getInt(12));
				alert.setResultEndpoint(rs.getString(13));
				alert.setAlertMessage(rs.getString(14));
				alrtList.add(alert);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOGGER.debug("Completed getUserQuery()");
		return alrtList;
	}


	public Alert getAlert(int alert_id) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		Alert alert = new Alert();
		try {
			ps = con.prepareStatement(GET_ALERT_DTLS);
			ps.setInt(1, alert_id);
			rs = ps.executeQuery();

			if (rs.next()) // get initial frame param
			{

				alert.setaID(alert_id);
				alert.setAlertType(rs.getString(1));
				alert.setAlertName(rs.getString(2));
				alert.setTheme(rs.getString(3));
				alert.setAlertSource(rs.getString(4));
				alert.setAlertMin(rs.getLong(5));
				alert.setAlertMax(rs.getLong(6));
				alert.setSafeSource(rs.getString(7));
				alert.setSafeMin(rs.getLong(8));
				alert.setSafeMax(rs.getLong(9));
				alert.setAlertStatus(rs.getInt(10));
				alert.setResultEndpoint(rs.getString(11));
				alert.setAlertMessage(rs.getString(12));
				alert.setBoundingBox(rs.getString(13));
				alert.setLat(rs.getDouble(14));
				alert.setLng(rs.getDouble(15));
				alert.setRadius(rs.getDouble(16));


			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return alert;
	}

}