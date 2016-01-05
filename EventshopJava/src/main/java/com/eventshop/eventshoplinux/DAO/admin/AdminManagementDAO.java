package com.eventshop.eventshoplinux.DAO.admin;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.ConversionMatrix;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.datasource.DataSource.DataFormat;
import com.eventshop.eventshoplinux.domain.datasource.DataSourceListElement;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.query.Query;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.eventshop.eventshoplinux.constant.Constant.*;

//import com.eventshop.eventshoplinux.domain.datasource.DataSourceHelper;

public class AdminManagementDAO extends BaseDAO {

	private Log log = LogFactory.getLog(AdminManagementDAO.class.getName());
	private final static Logger LOGGER = LoggerFactory.getLogger(BaseDAO.class);

	/*
	 * This method returns userlist for Admin
	 */

	public List<User> getUserList() {

		log.info("Inside getUserList()");

		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		User userList = new User();
		List<User> list = new ArrayList<User>();

		String qrySql = SELECT_USERMSTR_QRY;
		// userQuery =
		// (admin?"select A.* from tbl_User_Master A, tbl_role_master B where A.user_role_id=B.role_id and B.role_type='Admin' and A.user_email=? and A.user_password=? ":userQuery);
		try {
			ps = connection.prepareStatement(qrySql);
			rs = ps.executeQuery();

			while (rs.next()) {
				userList = new User();
				userList.setId(rs.getInt(1));
				userList.setUserName(rs.getString(2));
				userList.setEmailId(rs.getString(3));
				userList.setAuthentication(rs.getString(4));
				userList.setRoleType(rs.getString(5));
				userList.setStatus(rs.getString(6));
				userList.setUserLastAccessed(rs.getString(7));
				userList.setRoleId(rs.getInt(8));
				list.add(userList);
			}

		} catch (Exception e) {

		}
		log.info("Completed getUserList()");
		return list;
	}

	/*
	 * This method returns userDetails,Datasource and Querylist by Admin for
	 * selected users
	 */
	public HashMap<Integer, Object> getDataSrcQryLst(String selectedUserIds) {

		HashMap dsQryMap = null;
		HashMap userMap = new HashMap();
		int count = 0;
		String[] selectedUserIdList = selectedUserIds.split(",");

		try {

			for (int i = 0; i < selectedUserIdList.length; i++) {
				int userId = Integer.parseInt(selectedUserIdList[i]);

				// DataSrcList
				String dataSrcList = getUserDataSourceName(userId);

				// QueryList
				String qryList = getUserQueryName(userId);
				// constants to go to constants class
				dsQryMap = new HashMap();
				dsQryMap.put(Query, qryList);
				dsQryMap.put(DS, dataSrcList);
				dsQryMap.put(Constant.userId, userId);
				userMap.put(count, dsQryMap);
				count++;
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getDataSrcQryLst()");
		return userMap;
	}

	/*
	 * This method returns the dataSources based on user logged in
	 */
	public String getUserDataSourceName(int userId) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = connection();
		String qrySql = SLCT_DSMSTR_DSNAMES_QRY;
		String dsNames = EMPTY_STRING;
		try {
			ps = connection.prepareStatement(qrySql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {
				dsNames = new StringBuffer().append(dsNames)
						.append(rs.getString(dsmaster_title)).append(COMMA)
						.toString();

			}
			dsNames = (dsNames.equals(EMPTY_STRING) ? null : dsNames.substring(
					0, dsNames.length() - 1));
		} catch (Exception e) {

			log.error(e.getMessage());
		} finally {

		}
		log.info("Completed getUserDataSourceName()");
		return dsNames;
	}

	/*
	 * This method returns the query based on user logged in
	 */
	public String getUserQueryName(int userId) {
		log.info("Inside getUserQueryName()");
		String qryNames = EMPTY_STRING;
		PreparedStatement ps = null;
		ResultSet rs = null;
		Connection connection = connection();
		String qryListSql = SLCT_QRYMSTR_QYNAMES_QRY;
		try {
			ps = connection.prepareStatement(qryListSql);
			ps.setInt(1, userId);
			rs = ps.executeQuery();

			while (rs.next()) {
				qryNames = new StringBuffer().append(qryNames)
						.append(rs.getString(1)).append(COMMA).toString();

			}
			qryNames = (qryNames.equalsIgnoreCase(EMPTY_STRING) ? null
					: qryNames.substring(0, qryNames.length() - 1));

		} catch (Exception e) {

			log.error(e.getMessage());
		} finally {

		}
		log.info("Completed getUserQueryName()");
		return qryNames;
	}

	/*
	 * This method update the inActivate users to activeUser
	 */
	public String activateDeactivateUserList(List<String> userIds) {
		log.info("Inside activateDeactivateUserList()");
		PreparedStatement ps = null;
		Connection connection = connection();
		String qrySql = UPDATE_USERMSTR_STATUS_QRY;
		String result = NO_DATA;
		int lastIndex = (userIds.size()) - 1;
		String lastElement = userIds.get(lastIndex); // getting the status which
														// is appended at the
														// end of the List

		userIds.remove(lastIndex);
		ListIterator listIterator = userIds.listIterator();

		while (listIterator.hasNext()) {
			int userId = Integer.parseInt((String) listIterator.next());

			try {
				ps = connection.prepareStatement(qrySql);
				ps.setString(1, lastElement);
				ps.setInt(2, userId);
				int recordInsrtStatus = ps.executeUpdate();
				result = (recordInsrtStatus > 0 ? SUCCESS : result);

			} catch (Exception e) {
				result = DB_EXPT + e;
			}
		}
		log.info("Completed activateDeactivateUserList()");
		return result;
	}

	/*
	 * This method update userDetails
	 */
	public String updateUserdetails(User user) {
		log.info("Inside updateUserdetails()");
		PreparedStatement ps = null;
		String queryExeStatus = "";
		Connection connection = connection();
		String qrySql = UPDATE_USERMSTR_QRY;
		try {
			ps = connection.prepareStatement(qrySql);
			ps.setString(1, user.getUserName());
			ps.setString(2, user.getEmailId());
			ps.setString(3, user.getAuthentication());
			ps.setInt(4, user.getRoleId());
			ps.setString(5, user.getStatus());
			ps.setInt(6, user.getId());
			int recordInsrtStatus = ps.executeUpdate();
			if (recordInsrtStatus != 0) {
				queryExeStatus = SUCCESS;
			}

		} catch (Exception e) {

			log.error(e.getMessage());
			queryExeStatus = DB_EXPT + e;
		}
		log.info("Completed updateUserdetails()");

		return queryExeStatus;
	}

	public List<DataSourceListElement> getDataSrcList() {
		log.info("Inside getDataSrcList()");
		List<DataSourceListElement> dsList = new ArrayList<DataSourceListElement>();
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		DataSourceListElement dsElement = null;
		String tempDir = Config.getProperty(TEMPDIR);
		String qrySql = SELECT_DSMSTR_DSRES_QRY;
		try {
			ps = connection.prepareStatement(qrySql);
			rs = ps.executeQuery();

			while (rs.next()) {
				dsElement = new DataSourceListElement();
				dsElement.setSrcID(rs.getInt(1));
				dsElement.setSrcName(rs.getString(2));
				dsElement.setDesc(rs.getString(3));
				dsElement.setFormat(rs.getString(4));
				// String filepath = tempDir + PATH_DS + dsElement.getSrcID() +
				// UNDERSCORE + rs.getString(5);
				String filepath = tempDir + "ds" + dsElement.getSrcID() + json;
				File file = new File(filepath);
				boolean exists = file.exists();

				if (exists) {
					dsElement.setStatus(STATUS_AVAILABLE);
					dsElement.setControl(1);
					dsElement.setUrl(filepath);
				} else {
					dsElement.setStatus(STATUS_NOT_AVAILABLE);
					dsElement.setControl(0);
				}
				dsElement.setBoundingbox(rs.getString(6));
				dsElement.setAccess(rs.getString(7));
				dsElement.setTimeWindow(rs.getLong(8)); // need to set
														// timeWindow for
														// registerServlet --
														// sanjukta 06-08-2014
				dsList.add(dsElement);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getDataSrcList()");
		return dsList;
	}

	public List<DataSourceListElement> getDataSrcProfileForSelectedDS(
			List<String> selectedDSIds) {
		log.info("Inside getDataSrcProfileForSelectedDS()");
		List<DataSourceListElement> dsList = new ArrayList<DataSourceListElement>();
		DataSourceListElement dsElement = null;
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		ListIterator listIterator = selectedDSIds.listIterator();
		while (listIterator.hasNext()) {
			int userId = Integer.parseInt((String) listIterator.next());
			String qrySql = SELECT_DS_QRY;

			try {
				ps = connection.prepareStatement(qrySql);
				ps.setInt(1, userId);
				rs = ps.executeQuery();

				while (rs.next()) {
					dsElement = new DataSourceListElement();
					dsElement.setSrcID(rs.getInt(1));
					dsElement.setSrcName(rs.getString(2));
					dsElement.setDesc(rs.getString(3));
					dsElement.setUrl(rs.getString(4));
					dsElement.setFormat(rs.getString(5));
					dsElement.setType(rs.getString(6));
					dsElement.setEmailOfCreater(rs.getString(7));
					dsElement.setArchive(rs.getInt(8));
					dsElement.setUnit(rs.getInt(9));
					dsElement.setCreatedDate(rs.getString(10));
					dsList.add(dsElement);

				}

			} catch (Exception e) {

				log.error(e.getMessage());
			}

		}
		log.info("Completed getDataSrcProfileForSelectedDS()");
		return dsList;
	}

	public List<User> getDSUsers(String dsMasterId) {
		log.info("Inside getDSAccessibilityList()");
		// List<Object> dsMappedUserArray = null;
		List<User> dsUsers = new ArrayList<User>();
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String dsMappedUserSqlQry = SELECT_USERDS_QRY;

		try {

			ps = connection.prepareStatement(dsMappedUserSqlQry);
			ps.setString(1, dsMasterId);
			rs = ps.executeQuery();
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt(1));
				user.setUserName(rs.getString(2));
				dsUsers.add(user);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getDSAccessibilityList()");
		return dsUsers;
	}

	public List<Integer> getDSAccessibilityList(int dsMasterId) {
		log.info("Inside getDSAccessibilityList()");
		// List<Object> dsMappedUserArray = null;
		List<Integer> dsAssociatedUserId = new ArrayList<Integer>();
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String dsMappedUserSqlQry = SELECT_USERDS_QRY;

		try {

			ps = connection.prepareStatement(dsMappedUserSqlQry);
			ps.setInt(1, dsMasterId);
			rs = ps.executeQuery();
			while (rs.next()) {
				dsAssociatedUserId.add(rs.getInt(1));

			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getDSAccessibilityList()");
		return dsAssociatedUserId;
	}

	public List<User> getUsersNotInDS(String userIds) {
		log.info("Inside getDSAccessibilityList()");
		// List<Object> dsMappedUserArray = null;
		List<User> dsUsers = new ArrayList<User>();
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String dsMappedUserSqlQry = SELECT_USERNOTINDS_QRY;

		try {

			ps = connection.prepareStatement(dsMappedUserSqlQry);
			ps.setString(1, userIds);
			rs = ps.executeQuery();
			while (rs.next()) {
				User user = new User();
				user.setId(rs.getInt(1));
				user.setUserName(rs.getString(2));
				dsUsers.add(user);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getUsersNotInDS()");
		return dsUsers;
	}

	public String updateUserDataSource(List<String> selectedDSIds) {
		log.info("Inside updateUserDataSource()");
		Connection connection = connection();
		PreparedStatement ps = null;
		String result = FAILURE;
		int lastIndex = (selectedDSIds.size()) - 1;
		int dsId = Integer.parseInt(selectedDSIds.get(lastIndex)); // getting
																	// the dsId
																	// which is
																	// appended
																	// at the
																	// end of
																	// the List

		String deleteQry = DEL_USERDS_QRY;
		try {
			ps = connection.prepareStatement(deleteQry);
			ps.setInt(1, dsId);
			ps.executeUpdate();
		} catch (Exception e) {

			log.error(e.getMessage());
		}

		selectedDSIds.remove(lastIndex); // removing the last index
		ListIterator listIterator = selectedDSIds.listIterator();
		while (listIterator.hasNext()) {
			int userId = Integer.parseInt((String) listIterator.next());
			String qrySql = INSERT_USERDS_QRY;

			try {
				ps = connection.prepareStatement(qrySql);
				ps.setInt(1, userId);
				ps.setInt(2, dsId);
				int recordInsrtStatus = ps.executeUpdate();
				result = (recordInsrtStatus > 0 ? SUCCESS : result);

			} catch (Exception e) {

				log.error(e.getMessage());
			}

		}
		log.info("Completed updateUserDataSource()");
		return result;
	}

	public String deleteDS(List<String> dsIds) {
		log.info("Inside deleteDS()");
		Connection connection = connection();
		PreparedStatement ps = null;
		String result = FAILURE;
		ListIterator listIterator = dsIds.listIterator();
		while (listIterator.hasNext()) {
			int dsId = Integer.parseInt((String) listIterator.next());
			String sqlQry = DEL_DSMSTR_QRY;
			String statusDSRes = deleteReferences(DS_TABLE_NAME, DS_COL_NAME,
					dsId);
			String statusWrap = deleteReferences(WRP_TABLE_NAME, WRP_COL_NAME,
					dsId);
			if (statusDSRes.equals(SUCCESS) && statusWrap.equals(SUCCESS)) {
				try {
					ps = connection.prepareStatement(sqlQry);
					ps.setInt(1, dsId);
					int recordInsrtStatus = ps.executeUpdate();
					result = (recordInsrtStatus > 0 ? SUCCESS : result);

				} catch (Exception e) {

					log.error(e.getMessage());
				}
			}

		}
		log.info("Completed deleteDS()");
		return result;
	}

	public String deleteReferences(String tablName, String colName, int id) {
		log.info("Inside deleteReferences()");
		Connection connection = connection();
		PreparedStatement ps = null;
		String result = FAILURE;
		String sqlReferenceQry = DEL + tablName + WHERE + colName + PARMID;
		// "DELETE FROM "+tablName+" WHERE "+colName+"=?";
		try {
			ps = connection.prepareStatement(sqlReferenceQry);
			ps.setInt(1, id);
			int recordInsrtStatus = ps.executeUpdate();
			result = (recordInsrtStatus > 0 ? SUCCESS : result);

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed deleteReferences()");
		return result;
	}

	public List<Query> getQueryList() {
		log.info("Inside getQueryList()");
		Query qryElements = null;
		List<Query> qryList = new ArrayList<Query>();
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String statusControl = CONTROLFLAG; // Running
		String sqlQry = SELECT_QRYMSTR_ADMIN_QRY;
		try {
			ps = connection.prepareStatement(sqlQry);
			rs = ps.executeQuery();

			while (rs.next()) {
				qryElements = new Query();
				qryElements.setqID(rs.getInt(1));
				qryElements.setQueryName(rs.getString(2));
				qryElements.setBoundingbox(rs.getString(3));
				qryElements.setStatus(rs.getString(4));
				if (qryElements.getStatus().equals(statusControl)) {
					qryElements.setControl(1);
				} else {
					qryElements.setControl(0);
				}
				qryList.add(qryElements);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getQueryList()");
		return qryList;
	}

	/*
	 * Delete querylist
	 */
	public String deleteQueryList(List<String> queryIds) {
		log.info("Inside deleteQueryList()");
		Connection connection = connection();
		PreparedStatement ps = null;
		String result = FAILURE;
		ListIterator listIterator = queryIds.listIterator();
		while (listIterator.hasNext()) {
			int qryId = Integer.parseInt((String) listIterator.next());
			String sqlQry = DEL_QRYMSTR_QRY;
			deleteReferences(QRDS_TABLE_NAME, QRDS_COL_NAME, qryId);
			deleteReferences(QRMS_TABLE_NAME, QRMS_COL_NAME, qryId);
			try {
				ps = connection.prepareStatement(sqlQry);
				ps.setInt(1, qryId);
				int recordInsrtStatus = ps.executeUpdate();
				result = (recordInsrtStatus > 0 ? SUCCESS : result);

			} catch (Exception e) {

				log.error(e.getMessage());
			}
		}
		log.info("Completed deleteQueryList()");
		return result;
	}

	public List<DataSource> getDatasource(List<String> selectedDSIds) {
		log.info("Inside getDatasource()");
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet res = null;
		DataSource dsrc = null;
		// DataSourceHelper dsHelper = new DataSourceHelper();
		// String result = FAILURE;
		ArrayList<DataSource> dsList = new ArrayList<DataSource>();
		ListIterator listIterator = selectedDSIds.listIterator();
		while (listIterator.hasNext()) {
			int dsId = Integer.parseInt((String) listIterator.next());
			String[] bag_of_words = null;
			try {
				ps = connection.prepareStatement(SLT_DSMSTR_RUN_QRY);
				ps.setInt(1, dsId);
				res = ps.executeQuery();
				// SLT_DSMSTR_RUN_QRY =
				// "SELECT dsm.dsmaster_title,dsm.dsmaster_theme,dsm.dsmaster_url,dsm.dsmaster_format,wr.wrapper_name,wr.bag_of_words FROM Datasource_Master dsm,Wrapper wr WHERE dsm.dsmaster_id=wr.wrapper_id AND dsm.dsmaster_id=?";

				while (res.next()) {

					dsrc = new DataSource();
					dsrc.setSrcName(res.getString(1));
					dsrc.setSrcTheme(res.getString(2));
					dsrc.setUrl(res.getString(3));
					DataFormat dtFormat = null;
					String dfFormatStr = res.getString(4);
					if (dfFormatStr != null && !dfFormatStr.equals("")) {
						dtFormat = DataFormat.valueOf(dfFormatStr);
					}
					dsrc.setSrcFormat(dtFormat);
					dsrc.setSupportedWrapper(res.getString(5));
					bag_of_words = res.getString(6).split(COMMA);
					ArrayList<String> bagofWords = new ArrayList<String>();
					bagofWords.addAll(Arrays.asList(bag_of_words));
					dsrc.setBagOfWords(bagofWords);
					String tempDir = Config.getProperty(TEMPDIR);
					if (VISUAL.equals(res.getString(4))) {
						String filepathTransMatrix = tempDir + PATH_DS + dsId
								+ UNDERSCORE + TRANSMATRIX; // wrong path
						String filepathColorsMatrx = tempDir + PATH_DS + dsId
								+ UNDERSCORE + COLRSMATRIX; // wrong path

						dsrc.visualParam.setTranMatPath(filepathTransMatrix);
						dsrc.visualParam.setColorMatPath(filepathColorsMatrx);

						InputStream is = res.getBinaryStream(VISUAL_TRANS_MAT);
						BufferedReader reader = null;
						try {
							// for us to just make it work putting the if
							if (is != null) {
								reader = new BufferedReader(
										new InputStreamReader(is));
								ConversionMatrix tranMat = CommonUtil
										.parsefileToMatrix(reader);
								dsrc.visualParam.setTranslationMatrix(tranMat);
							}
						} catch (Exception e) {
							LOGGER.info("visual_trans_mat does not exist"
											+ e);
						}
						try {
							if (is != null) {
								is = res.getBinaryStream(VISUAL_COLR_MAT);
								reader = new BufferedReader(
										new InputStreamReader(is));
								ConversionMatrix colorMat = CommonUtil
										.parsefileToMatrix(reader);
								dsrc.visualParam.setColorMatrix(colorMat);
							}
						} catch (Exception e) {
							LOGGER.info("visual_colr_mat does not exist"
									+ e);
						}

					}
					dsrc.setSrcID(Integer.toString(dsId));
					FrameParameters frmObj = getFrameParameterDS(dsId);
					dsrc.setInitParam(frmObj);

				}

				dsList.add(dsrc);

			} catch (Exception e) {

				log.error(e.getMessage());
			}
		}
		log.info("Completed getDatasource()");
		return dsList;
	}

	public FrameParameters getFrameParameterDS(int dsId) {
		// FrameParameters fp = new FrameParameters(timeWindow, syncAtMilliSec,
		// latUnit,longUnit, swLat,swLong , neLat, neLong);
		log.info("Inside getFrameParameterDS()");
		Connection connection = connection();
		PreparedStatement ps = null;
		ResultSet rs = null;
		FrameParameters frmParmObj = new FrameParameters();
		String[] boundingBox = null;
		try {
			ps = connection.prepareStatement(SLT_DSRES_RUN_QRY);
			ps.setInt(1, dsId);
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

			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getFrameParameterDS()");
		return frmParmObj;
	}

}
