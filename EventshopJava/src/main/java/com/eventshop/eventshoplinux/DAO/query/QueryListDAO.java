package com.eventshop.eventshoplinux.DAO.query;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.query.Query;
import com.eventshop.eventshoplinux.domain.query.QueryDTO;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.*;

/*
 * This class is related for all QueryList DB operation
 */
public class QueryListDAO extends BaseDAO {
	private Log log = LogFactory.getLog(QueryListDAO.class.getName());
	private final static Logger LOGGER = LoggerFactory.getLogger(QueryListDAO.class);

	/*
     * This method returns the queryList based on user logged in
     */
	public List<Query> getUserQuery(User user) {
		log.info("Inside getUserQuery()");
		List<Query> qryList = new ArrayList<Query>();
		PreparedStatement ps = null;
		String statusControl = CONTROLFLAG; // Running
		ResultSet rs = null;
		Query query = null;
		String adminQryListSql = SELECT_QRMSTR_QRY; // Admin Qrylist
		String conditionQryListSql = SLT_QRMSTR_BASEON_ID_QRY;
		// "SELECT query_id,query_name,query_status FROM Query_Master WHERE query_creator_id=? OR query_creator_id=0";
		String qrySql = user.getRoleId() == 1 ? adminQryListSql
				: adminQryListSql + conditionQryListSql;

		try {
			ps = con.prepareStatement(qrySql);
			if (user.getRoleId() != 1) { // Normal User Qrylist
				ps.setInt(1, user.getId());
			}
			rs = ps.executeQuery();

			while (rs.next()) {
				query = new Query();
				query.setqID(rs.getInt(1));
				query.setQueryName(rs.getString(2));
				// query.setStatus(rs.getString(3));

				if (getQueryStatus(rs.getString(1))) {
					query.setControl(1);
					query.setStatus(RUNNING);
				} else {
					query.setControl(0);
					query.setStatus(STOPPED);
				}
				qryList.add(query);

			}

		} catch (Exception e) {
		}
		log.info("Completed getUserQuery()");
		return qryList;
	}

   /*
    * This method will check Query Object existance
    */

	public boolean chkQryID(int qID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		boolean flag = false;
		try {
			ps = con.prepareStatement(SLT_QRYMSTR_QRY);
			ps.setInt(1, qID);
			rs = ps.executeQuery();
			if (rs.next() != false) {
				flag = true;
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return flag;
	}

	/*
     * This method insert the Query Object to Query_Master table for queryRun
     */
	public int registerQuery(QueryDTO qryDTO, String selectedQueryRun,
							 int noSubQueries) {
		System.out.println("Query Registration.....");
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		ResultSet rs1 = null;
		int lastQryId = 0;
		selectedQueryRun = selectedQueryRun.trim();
		JsonParser jsonParser = new JsonParser();
		JsonArray queryArr = jsonParser.parse(selectedQueryRun).getAsJsonArray();
		List<String> dataSourceList = new ArrayList<>();

		for (int i = 0; i < queryArr.size(); i++) {

			JsonObject query = queryArr.get(i).getAsJsonObject();
			if (query.get("dataSources") != null) {
				JsonArray sources = query.get("dataSources").getAsJsonArray();
				if (sources != null) {
					for (int j = 0; j < sources.size(); j++) {
						String source = sources.get(j).getAsString();
						System.out.println("source:"+source);
						if (source.toLowerCase().startsWith("rule")) {
							String ruleid=source.substring(4,source.length());
							System.out.println("ruleid:"+ruleid);
							try {
								ps1= con.prepareStatement("select source_id from RuleQueryMaster where RuleID="+ruleid);
								rs1 = ps1.executeQuery();

								while (rs1.next()) {
									System.out.println(rs1.getString(1));
									dataSourceList.add(rs1.getString(1));
								}

							} catch (SQLException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		try {
			ps = con.prepareStatement(INST_QRYMSTR_DEFAULT_QRY);
			ps.setString(1, selectedQueryRun.trim());
			ps.setInt(
					2,
					(qryDTO.getTimeWindow() == null
							|| qryDTO.getTimeWindow().equals("") ? 0 : Integer
							.parseInt(qryDTO.getTimeWindow())));// 55
			ps.setDouble(3, qryDTO.getLatitudeUnit());// 3.3
			ps.setDouble(4, qryDTO.getLongitudeUnit());// 2.3
			ps.setString(5, qryDTO.getBoundingBox());
			// ps.setString(6, qryDTO.getQueryStatus());
			// if (getQueryStatus(qryDTO.getqID())) {
			// ps.setString(6,RUNNING);
			// } else {
			ps.setString(6, STOPPED);
			// }

			ps.setInt(7, qryDTO.getQryCreatorId());
			ps.setString(8, qryDTO.getQueryName()); // no entry in UI yet --
			// sanjukta
			ps.setString(9, qryDTO.getTimeType());
			ps.setString(10, dataSourceList.toString());

         /*
          * //child if (firstInsrtedQryId != 0) { ps =
          * connection.prepareStatement(INST_QRYMSTR_QRY);
          * 
          * ps.setString(1, selectedQueryRun.trim()); // esql ps.setInt(2,
          * (qryDTO.getTimeWindow() == null ||
          * qryDTO.getTimeWindow().equals("")
          * ?0:Integer.parseInt(qryDTO.getTimeWindow()))); //
          * hardcoded--Integer.parseInt(qryDTO.getTimeWindow())
          * ps.setDouble(3, qryDTO.getLatitudeUnit()); // hardcoded
          * --qryDTO.getLatitudeUnit() ps.setDouble(4,
          * qryDTO.getLongitudeUnit()); // hardcoded
          * -qryDTO.getLongitudeUnit() ps.setString(5,
          * qryDTO.getBoundingBox()); // hardcoded--not there in grouping
          * //ps.setString(6, qryDTO.getQueryStatus()); //
          * hardcoded--qryDTO.getQueryStatus() if
          * (getQueryStatus(qryDTO.getqID())) { ps.setString(6,RUNNING); }
          * else { ps.setString(6,STOPPED); }
          * 
          * ps.setInt(7, firstInsrtedQryId); ps.setInt(8,
          * qryDTO.getQryCreatorId()); ps.setString(9,qryDTO.getQueryName());
          * // no entry in UI yet -- sanjukta
          * System.out.println("reghister query parenttt"); }else {
          * 
          * ps = connection.prepareStatement(INST_QRYMSTR_DEFAULT_QRY);
          * ps.setString(1, selectedQueryRun.trim()); ps.setInt(2,
          * (qryDTO.getTimeWindow() == null ||
          * qryDTO.getTimeWindow().equals("")
          * ?0:Integer.parseInt(qryDTO.getTimeWindow())));//55
          * ps.setDouble(3, qryDTO.getLatitudeUnit());//3.3 ps.setDouble(4,
          * qryDTO.getLongitudeUnit());//2.3 ps.setString(5,
          * qryDTO.getBoundingBox()); //ps.setString(6,
          * qryDTO.getQueryStatus()); // if (getQueryStatus(qryDTO.getqID()))
          * { // ps.setString(6,RUNNING); // } else {
          * ps.setString(6,STOPPED); // }
          * 
          * ps.setInt(7, qryDTO.getQryCreatorId());
          * ps.setString(8,qryDTO.getQueryName()); // no entry in UI yet --
          * sanjukta System.out.println("reghister query chilldd"); }
          */

			ps.executeUpdate();
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				lastQryId = rs.getInt(1);
			}

		} catch (Exception e) {
			// log.error(e.getMessage());
			LOGGER.info("exceptionnn e11" + e);
		}
		LOGGER.debug("reghister query1 with id " + lastQryId);
		return lastQryId;
	}

   /*
    * This method return the QryEsql
    */

	public String getQryEsql(int qID) {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String qryEsql = "";
		try {
			ps = con.prepareStatement(SLT_QRYMSTR_QRY);
			ps.setInt(1, qID);
			rs = ps.executeQuery();
			while (rs.next()) {
				qryEsql = rs.getString(1);

			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return qryEsql;
	}

   /*
    * This method frame the FrameParametr for query
    */

	public FrameParameters getFrameParameterQry(int qryId) {
		// FrameParameters fp = new FrameParameters(timeWindow, syncAtMilliSec,
		// latUnit,longUnit, swLat,swLong , neLat, neLong);
		log.info("Inside getFrameParameterQry()");
		PreparedStatement ps = null;
		ResultSet rs = null;
		FrameParameters frmParmObj = new FrameParameters();
		String[] boundingBox = null;
		try {
			ps = con.prepareStatement(SLT_QRYMSTR_RUN_QRY);
			ps.setInt(1, qryId);
			rs = ps.executeQuery();
			while (rs.next()) {
				frmParmObj.setTimeWindow(rs.getLong(1));
				frmParmObj.setLatUnit(rs.getDouble(2));
				frmParmObj.setLongUnit(rs.getDouble(3));
				boundingBox = rs.getString(4).split(COMMA);
				frmParmObj.setSwLat(Double.parseDouble(boundingBox[0]));
				frmParmObj.setSwLong(Double.parseDouble(boundingBox[1]));
				frmParmObj.setNeLat(Double.parseDouble(boundingBox[2]));
				frmParmObj.setNeLong(Double.parseDouble(boundingBox[3]));
				frmParmObj.setTimeType(rs.getString(5));

			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Completed getFrameParameterQry()"
				+ frmParmObj.getTimeWindow());
		return frmParmObj;
	}

	// used by registerServlet
	public List<QueryDTO> getQueryList() {
		// include all the setters
		QueryDTO query = null;
		List<QueryDTO> qryList = new ArrayList<QueryDTO>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String statusControl = CONTROLFLAG; // Running
		String sqlQry = SELECT_QRYMSTR_ADMIN_QRY;
		try {
			ps = con.prepareStatement(sqlQry);
			rs = ps.executeQuery();

			while (rs.next()) {
				query = new QueryDTO();
				query.setqID(rs.getString(1));
				// user id not required
				query.setQueryName(rs.getString(3));
				query.setQueryEsql(rs.getString(5));
				query.setTimeWindow(rs.getString(6));
				try {
					query.setLatitudeUnit(Double.parseDouble(rs
							.getString(7)));
					query.setLongitudeUnit(Double.parseDouble(rs
							.getString(8)));
				} catch (Exception e) {
					log.error("Latitude and longitude conversion issue" + e);
				}
				query.setBoundingBox(rs.getString(9));
				if (getQueryStatus(query.getqID())) {
					query.setStatus(RUNNING);
					query.setControl(1);
				} else {
					query.setStatus(STOPPED);
					query.setControl(0);
				}
				qryList.add(query);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getQueryList()");
		return qryList;
	}

	// added by sanjukta
	private boolean getQueryStatus(String qid) {
		// String fileExtn =
		// (Config.getProperty("env").equals(WINOS)?WINEXECFILEEXTN:LINUXEXECFILEEXTN);
		// String fileExtn = LINUXEXECFILEEXTN;
		// String filePath = new
		// StringBuffer().append(Config.getProperty("context")).append(Config.getProperty("queryDir")).append(qid).append(".").append(fileExtn).toString();
		String currentOs = System.getProperty("os.name").toLowerCase();
		String fileExtn = LINUXEXECFILEEXTN;
		if (currentOs.contains("win")) {
			fileExtn = WINEXECFILEEXTN;
		}
		String filePath = Config.getProperty("context")
//          + "proc/Debug/EmageOperators_Q" + qid + "_1" + fileExtn;
				+ "temp/queries/" + "Q" + qid + ".json";
		File file = new File(filePath);
		// log.info("check query status " + filePath + ", " + file.exists());
		return (file.exists());

	}

	// added by Siripen
	// note apparently, only the final "saved query" is inserted.
	// so only one record will be returned from db.
	public List<String> getQueryTree(int qid_parent) {
		String SQL_QUERY_TREE = "select query_esql from Query_Master where query_id = ? or qid_parent = ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		List<String> qryEsql = new ArrayList<String>();
		try {
			ps = con.prepareStatement(SQL_QUERY_TREE);
			ps.setInt(1, qid_parent);
			ps.setInt(2, qid_parent);
			rs = ps.executeQuery();
			while (rs.next()) {
				qryEsql.add(rs.getString(1));
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		LOGGER.debug("querESQL " + qryEsql);
		return qryEsql;
	}

   /*
    * // not being used?? public QueryProcess parseQueryTree(int qid_parent) {
    * QueryProcess aQuery = new QueryProcess(Config.getProperty("context"));
    * //QueryJSONParser parser = new QueryJSONParser(); //List<String>
    * queryTree = this.getQueryTree(qid_parent); //aQuery =
    * parser.parseQuery(queryTree); return aQuery; }
    */

	// used by registerServlet
	public List<com.eventshop.eventshoplinux.model.Query> getAllQuery() {
		// include all the setters
		com.eventshop.eventshoplinux.model.Query query = null;
		List<com.eventshop.eventshoplinux.model.Query> qryList = new ArrayList<com.eventshop.eventshoplinux.model.Query>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlQry = SELECT_QRYMSTR_ALL;
		try {
			ps = con.prepareStatement(sqlQry);
			rs = ps.executeQuery();

			while (rs.next()) {
				query = new com.eventshop.eventshoplinux.model.Query();

				query.setQuery_id(rs.getInt(1));
				query.setQuery_creator_id(rs.getInt(2));
				query.setQuery_name(rs.getString(3));
				query.setQuery_desc(rs.getString(4));
				query.setQuery_esql(rs.getString(5));
				query.setTime_window(rs.getInt(6));
				query.setLatitude_unit(rs.getDouble(7));
				query.setLongitude_unit(rs.getDouble(8));
				query.setBoundingbox(rs.getString(9));
				query.setQuery_status(rs.getString(10));
				query.setQid_parent(rs.getInt(11));

				qryList.add(query);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Completed getQueryList()");
		return qryList;
	}

	public boolean enableQuery(int queryID) {
		PreparedStatement ps = null;
		boolean status = false;
		try {
			ps = con.prepareStatement(ENABLE_QUERY);
			ps.setInt(1, queryID);
			ps.executeUpdate();
			status = true;
		} catch (Exception e) {
			log.error("Exception in enabling Query {}", e);
			e.printStackTrace();
		}
		return status;
	}

	public boolean disableQuery(int queryID) {
		PreparedStatement ps = null;
		boolean status = false;
		try {
			ps = con.prepareStatement(DISABLE_QUERY);
			ps.setInt(1, queryID);
			ps.executeUpdate();
			status = true;
		} catch (Exception e) {
			log.error("Exception in disabling Query {}", e);
			e.printStackTrace();
		}
		return status;
	}

	public List<com.eventshop.eventshoplinux.model.Query> getEnabledQueriesWithDS(int dsID) {
		// include all the setters
		com.eventshop.eventshoplinux.model.Query query = null;
		List<com.eventshop.eventshoplinux.model.Query> qryList = new ArrayList<com.eventshop.eventshoplinux.model.Query>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		String sqlQry = SELECT_ENABLED_QUERIES_WITH_DS_ID;
		try {
			ps = con.prepareStatement(sqlQry);
			ps.setString(1, "1");
			ps.setString(2, "%ds" + dsID + "%");
			rs = ps.executeQuery();
			// query_id,query_creator_id,query_name,query_desc,query_esql,time_window,latitude_unit,longitude_unit,boundingbox,query_status, qid_parent
			while (rs.next()) {
				query = new com.eventshop.eventshoplinux.model.Query();

				query.setQuery_id(rs.getInt(1));
				query.setQuery_creator_id(rs.getInt(2));
				query.setQuery_name(rs.getString(3));
				query.setQuery_desc(rs.getString(4));
				query.setQuery_esql(rs.getString(5));
				query.setTime_window(rs.getInt(6));
				query.setLatitude_unit(rs.getDouble(7));
				query.setLongitude_unit(rs.getDouble(8));
				query.setBoundingbox(rs.getString(9));
				query.setQuery_status(rs.getString(10));
				query.setQid_parent(rs.getInt(11));

				qryList.add(query);
			}

		} catch (Exception e) {
			log.error(e.getMessage());
		}
		log.info("Completed getQueryList()");
		return qryList;

	}
}