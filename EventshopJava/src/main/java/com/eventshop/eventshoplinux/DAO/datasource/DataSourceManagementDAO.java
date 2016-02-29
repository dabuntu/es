package com.eventshop.eventshoplinux.DAO.datasource;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.domain.common.ConversionMatrix;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.common.Result;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.domain.datasource.DataSource.DataFormat;
import com.eventshop.eventshoplinux.domain.datasource.DataSourceListElement;
import com.eventshop.eventshoplinux.domain.datasource.Wrapper;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;
import com.eventshop.eventshoplinux.util.commonUtil.ResultConfig;
import com.sun.jersey.core.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static com.eventshop.eventshoplinux.constant.Constant.*;

public class DataSourceManagementDAO extends BaseDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(DataSourceManagementDAO.class);

	// At the time of creating/adding a datasource, wrapper_type will always be
	// PULL
	/*
	 * public final static String WRAPPER_TYPE="PULL"; public final static
	 * String STATUS_AVAILABLE="Available"; public final static String
	 * STATUS_NOT_AVAILABLE="Connecting"; public final static String
	 * INITIAL_RESOLUTION="inital"; public final static String
	 * FINAL_RESOLUTION="final";
	 */
	double[][] TrMat = {
			{54.169184929, -5.542199557}, // 52.169184929
			{0.496176521, 0.203104483}, // 0.506176521, .196
			{-0.328315814, 0.868318964},// , .87
			{0.001014807, 0.000358314}, {0.000245955, -0.000381097},
			{0.000422211, -0.000662529},
			{-0.00000001, -0.000000585}, // -0.000000121, -0.000000785
			{0.000000180, 0.000000555}, {-0.000000270, 0.000000078},
			{-0.000001276, 0.000001363},};
	double[][] ColMat = {{2.04, 138.45, 2.21}, {155.65, 252.84, 50.32},
			{254.37, 254.27, 2.78}, {254.30, 152.94, 0.09},
			{253.93, 0.08, 0.00}, {190.85, 190.35, 147.51},
			{254.67, 254.98, 254.58},};
	private Log log = LogFactory.getLog(DataSourceManagementDAO.class.getName());

	public Result saveDataSource(DataSource ds) {

		Result result = new Result();
		int inserted = 0;
		String dsmasterSql = ((ds.getSrcID() == null || ds.getSrcID() == "" || ds
				.getSrcID().equals("0")) ? INSERT_DSMSTR_QRY
				: UPDATE_DSMSTR_QRY);
		int updtFlg = ((ds.getSrcID() == null || ds.getSrcID() == "" || ds
				.getSrcID().equals("0")) ? 0 : 1);
		String dsResStr = (updtFlg == 0 ? "dsAdd" : "dsErr");
		try {
			PreparedStatement ps = con.prepareStatement(dsmasterSql);
			ps.setString(1, ds.getSrcName());
			ps.setString(2, ds.getSrcTheme());
			ps.setString(3, ds.getUrl());
			ps.setString(4, ds.getSrcFormat().toString());

			//6,7 are time
			ps.setString(5, ds.getSyntax());
			if (updtFlg == 0) {
				ps.setInt(6, Integer.parseInt(ds.getUserId()));
			} else {
				ps.setLong(6, Long.parseLong(ds.getSrcID()));
			}

			inserted = ps.executeUpdate(); // run the query
			if (inserted != 0 && updtFlg == 0) {
				ps = con.prepareStatement(SELECT_DSMSTR_ID);
				ps.setString(1, ds.getSrcTheme());
				ps.setString(2, ds.getSrcName());
				ps.setString(3, ds.getUserId());
				ResultSet rs = ps.executeQuery();
				rs.next();
				ds.setSrcID(rs.getString(1));
			}

			if (ds.getInitParam() != null) {
				// System.out.println("frame parameters saving hhh");
				if (checkFrameParam(Integer.parseInt(ds.getSrcID()))) {
					updateFrameParameter(ds.getInitParam(),
							Integer.parseInt(ds.getSrcID()), INITIAL_RESOLUTION);
				} else {
					saveFrameParameter(ds.getInitParam(),
							Integer.parseInt(ds.getSrcID()), INITIAL_RESOLUTION);
				}
			}
			if (ds.getWrapper() != null) {
				saveWrapper(ds);
			}

			result.setResultCode(ResultConfig.getProperty(dsAddSuccCode));
			result.setStatus(ResultConfig.getProperty(SUCCESS));
			result.setComment(ResultConfig.getProperty(dsAddSuccComment));

		} catch (SQLException sqle) {
			sqle.printStackTrace();
			result.setResultCode(ResultConfig.getProperty(dsAddErrCode));
			result.setStatus(ResultConfig.getProperty(FAILURE));
			result.setComment(new StringBuffer()
					.append(ResultConfig.getProperty(dsAddErrComment))
					.append(sqle.toString()).toString());
		}

		return result;
	}

	private void saveWrapper(DataSource ds) {

		ResultSet rs = null;
		String query = "";
		String translationMatrix = null;
		String colorMatrix = null;
		// String bag_of_words_str= CommonUtil.listToCSV(ds.getBagOfWords());

		if (ds.getWrapper().getWrprId() != null
				&& !EMPTY_STRING.equals(ds.getWrapper().getWrprId())
				&& !ds.getWrapper().getWrprId().equals("0"))
			query = UPDATE_WRPR_QRY;
		else
			query = INSRT_WRPR_QRY;

		try {

			// PreparedStatement
			// psWrpr=con.prepareStatement("insert into Wrapper(wrapper_name,wrapper_type,wrapper_key_value,bag_of_words,visual_tran_mat,visual_color_mat,visual_mask_mat,visual_ignore_since) values(?,?,?,?,?,?,?,?)");
			PreparedStatement psWrpr = con.prepareStatement(query);
			psWrpr.setString(1, ds.wrapper.getWrprName()); // wrapper_name
			psWrpr.setString(2, (ds.wrapper.getWrprType() == null
					|| ds.wrapper.getWrprType().equals("") ? WRAPPER_TYPE
					: ds.wrapper.getWrprType()));

			psWrpr.setString(3, ds.wrapper.getWrprKeyValue()); // key_value
			psWrpr.setString(4, ds.getWrapper().getWrprBagOfWords()); // bag
			// logged in user for fifth parameter
			if (ds.wrapper != null) {
				// String tranMatPath=ds.getVisualParam().tranMatPath;
				// //visual_tran
				if (ds.wrapper.getWrprVisualTransMat() != null) {
					byte[] bytes = Base64.decode(ds.wrapper
							.getWrprVisualTransMat());
					// FileInputStream fis = new
					// FileInputStream(ds.getVisualParam().tranMatPath);
					psWrpr.setBytes(5, bytes);
					// translationMatrix=CommonUtil.parseFiletoString(ds.getVisualParam().tranMatPath);
				} else {
					psWrpr.setNull(5, java.sql.Types.BLOB);
				}

				// String colorMatPath=ds.getVisualParam().colorMatPath;
				// //colormat we are not storing the path anywhere
				if (ds.wrapper.getWrprVisualColorMat() != null) {
					byte[] bytes = Base64.decode(ds.wrapper
							.getWrprVisualTransMat());
					psWrpr.setBytes(6, bytes);
					// FileInputStream fis=new FileInputStream(maskPath);
					// psWrpr.setBinaryStream(7, fis, fis.available());

				} else {
					psWrpr.setNull(6, java.sql.Types.BLOB);
				}

				// String maskPath=ds.getVisualParam().maskPath;
				if (ds.wrapper.getWrprVisualMaskMat() != null) {
					byte[] bytes = Base64.decode(ds.wrapper
							.getWrprVisualColorMat());
					psWrpr.setBytes(7, bytes);
					// FileInputStream fis=new FileInputStream(colorMatPath);
					// psWrpr.setBinaryStream(6, fis, fis.available());
					// translationMatrix=CommonUtil.parseFiletoString(ds.getVisualParam().tranMatPath);
				} else {
					psWrpr.setNull(7, java.sql.Types.BLOB);
				}

				psWrpr.setInt(8,
						Integer.parseInt(ds.getWrapper().getWrprVisualIgnore())); // visual_ignore
				psWrpr.setString(9, ds.wrapper.getWrprCSVFileURL());

			} else {

				psWrpr.setString(5, null);
				psWrpr.setString(6, null);
				psWrpr.setString(7, null);
				psWrpr.setInt(8, 0);
				psWrpr.setString(9, null);
			}

			// System.out.println("idddd"+ds.getSrcID());
			if (ds.getWrapper().getWrprId() == null
					|| ds.getWrapper().getWrprId().equals("")) {
				// System.out.println("we are null wrapped");
				psWrpr.setInt(10, Integer.parseInt(ds.getSrcID()));
			} else {
				psWrpr.setInt(10, Integer.parseInt(ds.getWrapper().getWrprId()));
			}

			psWrpr.executeUpdate();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private int saveFrameParameter(FrameParameters param, int dsmaster_id,
								   String resolution_type) {

		PreparedStatement ps = null;
		int inserted = 0;
		String queryDatasourceResolution = INSERT_DSRES_QRY;

		try {

			ps = con.prepareStatement(queryDatasourceResolution);
			// take dsmaster_id inserted above and insert at first place
			ps.setInt(1, dsmaster_id);
			// how do we get the datastream name?
			ps.setString(2, null);
			ps.setLong(3, param.getTimeWindow());
			ps.setLong(4, param.getSyncAtMilSec());
			ps.setDouble(5, param.getLatUnit());
			ps.setDouble(6, param.getLongUnit());
			ps.setString(7, param.boundingBoxString());
			ps.setString(8, resolution_type);
			ps.setString(9, param.getTimeType());
			ps.setString(10, param.getDsQuery());
			ps.setBoolean(11, param.getGenEmage());

			inserted = ps.executeUpdate();

		} catch (Exception e) {
			log.error("not working " + e);
			e.printStackTrace();

		}
		return inserted;
	}

	private int updateFrameParameter(FrameParameters param, int dsmaster_id,
									 String resolution_type) {
//		System.out.println("%%%%%%%%%%    " + dsmaster_id);
		PreparedStatement ps = null;
		int updated = 0;

		String queryDatasourceResolution = UPDATE_DSRES_QRY;
		// System.out.println(" parameter details for update param.getTimeWindow()"+param.getTimeWindow()+
		// " param.getLatUnit() "+param.getLatUnit()+" param.getLongUnit()"+param.getLongUnit()+" param.boundingBoxString()"+param.boundingBoxString());
		try {
			ps = con.prepareStatement(queryDatasourceResolution);

			ps.setLong(1, param.getTimeWindow());
			ps.setDouble(2, param.getLatUnit());
			ps.setDouble(3, param.getLongUnit());
			ps.setString(4, param.boundingBoxString());
			ps.setLong(5, param.getSyncAtMilSec());
			ps.setString(6, param.getTimeType());
			ps.setString(7, param.getDsQuery());
			ps.setBoolean(8, param.getGenEmage());
			ps.setInt(9, dsmaster_id);
			ps.setString(10, resolution_type);


			// logged in user for fifth parameter

			updated = ps.executeUpdate();
			// log.info("Record updated ");

		} catch (SQLException e) {
			e.printStackTrace();

			log.error(e.getMessage());

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return updated;
	}

	public DataSource getDS(int dsmaster_id) {
		// log.info("getDataSource() method entered");
		PreparedStatement ps = null;
		ResultSet rs = null;
		DataSource source = new DataSource();
		try {
			ps = con.prepareStatement(SELECT_DSMSTR_ALL_QRY);
			ps.setInt(1, dsmaster_id);
			rs = ps.executeQuery();

			// initial and final frame params are saved as part of
			// Datasource_Resolution table.
			// so, we will get 2 resultset records from above query , one for
			// initial frameparam and other for final frame param.

			if (rs.next()) // get initial frame param
			{

				source.setSrcID(Integer.toString(dsmaster_id));
				source.setSrcName(rs.getString(dsmaster_title));
				source.setUrl(rs.getString(DSMSTR_URL));
				source.setSrcTheme(rs.getString(DSMSTR_THEME));
				int status = CommonUtil.getEmageStatus(dsmaster_id + "");
				if (status == 1) {
					source.setStatus(STATUS_AVAILABLE);
					source.setControl(1);
				} else {
					source.setStatus(STATUS_NOT_AVAILABLE);
					source.setControl(0);
				}

				String format = rs.getString(DSMSTR_FRMT);

				if (STREAM.equals(format))
					source.setSrcFormat(DataSource.DataFormat.stream);
				else if (VISUAL.equals(format))
					source.setSrcFormat(DataSource.DataFormat.visual);
				else if (CSV.equals(format))
					source.setSrcFormat(DataSource.DataFormat.file);

			}
			// if(rs.next()) //get final frame param is not needed for
			// datasource as per Siripen -- sanjukta
			// {
			// populateFrameParam(source.finalParam, rs);
			// }

		} catch (Exception e) {
			log.info("DatasourceManagementDao.getDatasource has issues " + e);

		} finally {

		}

		log.info("getDataSource() method end");
		return source;
	}

	/**
	 * @param dsmaster_id
	 * @return
	 */
	public DataSource getDataSource(int dsmaster_id) {
		// log.info("getDataSource() method entered");
		PreparedStatement ps = null;
		ResultSet rs = null;
		DataSource source = new DataSource();
		try {
			ps = con.prepareStatement(SELECT_DSMSTR_ALL_QRY);
			ps.setInt(1, dsmaster_id);
			rs = ps.executeQuery();

			// initial and final frame params are saved as part of
			// Datasource_Resolution table.
			// so, we will get 2 resultset records from above query , one for
			// initial frameparam and other for final frame param.

			if (rs.next()) // get initial frame param
			{

				source.setSrcID(Integer.toString(dsmaster_id));
				source.setSrcName(rs.getString(dsmaster_title));
				source.setUrl(rs.getString(DSMSTR_URL));
				source.setSrcTheme(rs.getString(DSMSTR_THEME));
				source.setSyntax(rs.getString("dsmaster_syntax"));
				int status = CommonUtil.getEmageStatus(dsmaster_id + "");
				if (status == 1) {
					source.setStatus(STATUS_AVAILABLE);
					source.setControl(1);
				} else {
					source.setStatus(STATUS_NOT_AVAILABLE);
					source.setControl(0);
				}

				String format = rs.getString(DSMSTR_FRMT);

				if (STREAM.equals(format))
					source.setSrcFormat(DataSource.DataFormat.stream);
//				else if (VISUAL.equals(format))
//					source.setSrcFormat(DataSource.DataFormat.visual);
				else if (FILE.equals(format))
					source.setSrcFormat(DataFormat.file);
				else if (REST.equals(format))
					source.setSrcFormat(DataFormat.rest);

			}
			// if(rs.next()) //get final frame param is not needed for
			// datasource as per Siripen -- sanjukta
			// {
			// populateFrameParam(source.finalParam, rs);
			// }

		} catch (Exception e) {
			log.info("DatasourceManagementDao.getDatasource has issues " + e);

		}

		if (source != null) {
			source = populateWrapper(source, dsmaster_id);
			populateFrameParam(source.initParam, dsmaster_id);
		}
		return source;
	}

	public DataSource populateWrapper(DataSource source, int dsmaster_id) {

		List<String> listofWord = new ArrayList<String>();
		Wrapper wrapper = new Wrapper();
		try {
			PreparedStatement preparedStatementWrpr = con
					.prepareStatement(SELECT_WRPR_QRY);
			// System.out.println("SELECT_WRPR_QRY "+SELECT_WRPR_QRY);
			preparedStatementWrpr.setInt(1, dsmaster_id);
			ResultSet rsWrpr = preparedStatementWrpr.executeQuery();
			// SELECT
			// wr.wrapper_id,wr.wrapper_name,wr.wrapper_type,wr.wrapper_key_value,wr.bag_of_words,
			// wr.visual_tran_mat,wr.visual_color_mat,wr.visual_mask_mat,wr.visual_ignore_since
			while (rsWrpr.next()) {
				wrapper.setWrprId(rsWrpr.getString(1));
				wrapper.setWrprName(rsWrpr.getString(2));
				wrapper.setWrprType(rsWrpr.getString(3));
				wrapper.setWrprKeyValue(rsWrpr.getString(4));

				source.setSupportedWrapper(wrapper.getWrprName());
				String bag_of_words_str = rsWrpr.getString(BAG_OF_WORDS);
				listofWord.add(bag_of_words_str);

				String[] bag_of_words_array = bag_of_words_str.split(COMMA);
				ArrayList<String> bag_of_words_list = new ArrayList<String>(
						Arrays.asList(bag_of_words_array));

				source.setBagOfWords(bag_of_words_list);

				wrapper.setWrprBagOfWords(bag_of_words_str);
				wrapper.setWrprVisualTransMat(rsWrpr.getString(6));
				wrapper.setWrprVisualColorMat(rsWrpr.getString(7));
				wrapper.setWrprVisualMaskMat(rsWrpr.getString(8));
				wrapper.setWrprVisualIgnore(rsWrpr.getString(9));
				// System.out.println(source.getSrcFormat());
				if (source.getSrcFormat() != null) {

				if (VISUAL.equalsIgnoreCase(source.getSrcFormat().toString())) {
					InputStream is = rsWrpr.getBinaryStream(VISUAL_TRANS_MAT);

					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));

					ConversionMatrix tranMat = null;
					try {
						tranMat = CommonUtil.parsefileToMatrix(reader);
					} catch (Exception e) {
						if (tranMat == null) {
							tranMat = new ConversionMatrix();
							tranMat.setMatrix(TrMat);
						}
					}
					source.visualParam.setTranslationMatrix(tranMat);

					is = rsWrpr.getBinaryStream(VISUAL_COLR_MAT);

					reader = new BufferedReader(new InputStreamReader(is));

					ConversionMatrix colorMat = null;
					try {
						colorMat = CommonUtil.parsefileToMatrix(reader);
					} catch (Exception e) {
						if (colorMat == null) {
							colorMat = new ConversionMatrix();
							colorMat.setMatrix(ColMat);
						}
					}
					source.visualParam.setColorMatrix(colorMat);

					/*
					 * source.visualParam.setTranMatPath(rs.getString(
					 * "visual_tran_mat")) ;
					 * source.visualParam.setColorMatPath(rs
					 * .getString("visual_color_mat"));
					 */
					// source.visualParam.setMaskPath(rs.getString("visual_mask_mat"));
					source.visualParam.setIgnoreSinceNumber(rsWrpr.getInt(VISUAL_IGNOR_SINCE));
				}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		source.setWrapper(wrapper);
		return source;
	}

	public void populateFrameParam(FrameParameters param, int dsmaster_id) {
		try {
			PreparedStatement psFP = con.prepareStatement(SELECT_DSRES_QRY);
			psFP.setInt(1, dsmaster_id);
			ResultSet rsFP = psFP.executeQuery();

			while (rsFP.next()) {
				param.setTimeWindow(rsFP.getInt(TIME_WINDOW));
				param.setLatUnit(rsFP.getDouble(LATITUDE_UNIT));
				param.setLongUnit(rsFP.getDouble(LONGITUDE_UNIT));
				String boundingbox = rsFP.getString(BOUNDINGBOX);
				String[] boundingboxVal = (boundingbox == null
						|| boundingbox.equals("") ? null : boundingbox
						.split(COMMA));

				double swLat = Double.parseDouble(boundingboxVal[0]);
				double swLong = Double.parseDouble(boundingboxVal[1]);
				double neLat = Double.parseDouble(boundingboxVal[2]);
				double neLong = Double.parseDouble(boundingboxVal[3]);
				param.setDsQuery(rsFP.getString(DS_QUERY));
				param.setGenEmage(rsFP.getBoolean(GEN_EMAGE));
				param.setNeLat(neLat);
				param.setNeLong(neLong);
				param.setSwLat(swLat);
				param.setSwLong(swLong);

				param.setSyncAtMilSec(rsFP.getLong(SYNC_TIME));
				param.setTimeType(rsFP.getString(TIME_TYPE));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<DataSourceListElement> getDataSourceList(User user) {
		log.info("start getDataSourceList by UserID");
		List<DataSourceListElement> listDataSource = new ArrayList<DataSourceListElement>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		String adminDSListSql = SELECT_DSMSR_QRY;
		String userDSListSql = SLT_DSMSR_QRY_BASEON_ID_QRY;
		// "SELECT query_id,query_name,query_status FROM Query_Master WHERE dsmaster_creator=? OR dsmaster_creator=0";
		String qrySql = user.getRoleId() == 1 ? adminDSListSql : adminDSListSql
				+ userDSListSql;

		try {
			ps = con.prepareStatement(qrySql);
			if (user.getRoleId() != 1) { // Normal User DSlist
				ps.setInt(1, user.getId());
			}
			rs = ps.executeQuery();

			while (rs.next()) {
				DataSourceListElement source = new DataSourceListElement();
				int srcId = rs.getInt(dsmaster_id);
				String srcTitle = rs.getString(dsmaster_title);
				source.setCreater(rs.getInt(3)); // added on 16/07/2014
				int status = CommonUtil.getEmageStatus(srcId + "");
				if (status == 1) {
					source.setStatus(STATUS_AVAILABLE);
					source.setControl(1);
				} else {
					source.setStatus(STATUS_NOT_AVAILABLE);
					source.setControl(0);
				}
				if (rs.getString("dsmaster_access") != null
						&& rs.getString("dsmaster_access").equalsIgnoreCase(
						"public"))
					source.setControl(-1);
				source.setSrcID(srcId);
				source.setSrcName(srcTitle);

				listDataSource.add(source);
			}

		} catch (Exception e) {

		} finally {

		}
		log.info("end getDataSourceList by UserID");

		return listDataSource;
	}

	// new stuff to merge with Dec release
	public List<DataSource> getDatasourceForSelectedDS(String[] selectedDSIds) {

		List<DataSource> dsList = new ArrayList<DataSource>();
		DataSource dsElement = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		for (int i = 0; i < selectedDSIds.length; i++) {

			String dsSql = SELECT_DS_QRY;

			try {
				ps = con.prepareStatement(dsSql);
				ps.setString(1, selectedDSIds[i]);
				rs = ps.executeQuery();
				// System.out.println("select query "+SELECT_DS_QRY);
				while (rs.next()) {
					dsElement = new DataSource();
					dsElement.setSrcID(rs.getString(1));
					dsElement.setSrcTheme(rs.getString(2));
					dsElement.setSrcName(rs.getString(3)); // desc
					dsElement.setUrl(rs.getString(4));
					int status = CommonUtil.getEmageStatus(rs.getString(1));

					if (status == 1) {
						dsElement.setStatus(STATUS_AVAILABLE);
						dsElement.setControl(1);
					} else {
						dsElement.setStatus(STATUS_NOT_AVAILABLE);
						dsElement.setControl(0);
					}

					// System.out.println("source format for "+rs.getString(5));
					if (rs.getString(5) != null) {
						dsElement.setSrcFormat(DataFormat.valueOf(rs
								.getString(5)));
					}

					dsElement.setType(rs.getString(6));
					dsElement.setArchive(rs.getInt(7));
					dsElement.setUnit(rs.getInt(8));
					// dsElement.setCreatedDate(rs.getString(9));
					// dsElement.setTimeWindow(new Long(rs.getLong(10)));//
					// 16-07-2014 -- sanjukta
					dsElement.setEmailOfCreator(rs.getString(11));

					dsList.add(dsElement);

				}

			} catch (Exception e) {

				log.error("error in query" + e.getMessage());
			}

		}
		log.info("Completed getDataSrcProfileForSelectedDS()");
		return dsList;
	}

	public List<DataSource> getDataSrcList() {

		List<DataSource> dsList = new ArrayList<DataSource>();
		PreparedStatement ps = null;
		ResultSet rs = null;
		DataSource dsElement = null;
		String qrySql = SELECT_DSMSTR_DSRES_QRY;
		try {
			ps = con.prepareStatement(qrySql);
			rs = ps.executeQuery();

			while (rs.next()) {
				dsElement = new DataSource();
				dsElement.setSrcID(rs.getString(1));
				dsElement.setSrcTheme(rs.getString(2));
				dsElement.setSrcName(rs.getString(3)); // desc
				dsElement.setSrcFormat(DataFormat.valueOf(rs.getString(4)));
				int status = CommonUtil.getEmageStatus(rs.getString(1));
				if (status == 1) {
					dsElement.setStatus(STATUS_AVAILABLE);
					dsElement.setControl(1);
				} else {
					dsElement.setStatus(STATUS_NOT_AVAILABLE);
					dsElement.setControl(0);
				}
				dsElement.setBoundingbox(rs.getString(6));
				dsElement.setAccess(rs.getString(7));
				// dsElement.setTimeWindow(rs.getLong(8));// need to set
				// timeWindow to be in sync with the AdminManagementDao method
				// -- sanjukta 06-08-2014

				dsList.add(dsElement);
			}

		} catch (Exception e) {

			log.error(e.getMessage());
		}
		log.info("Completed getDataSrcList()");
		return dsList;
	}

	public ArrayList<String> getAllDsIds() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String id = null;
		ArrayList<String> dsIds = new ArrayList<String>();

		try {
			ps = con.prepareStatement(GET_ALL_DSID);
			rs = ps.executeQuery();

			while (rs.next())
			{
				id = rs.getString(1);
				dsIds.add(id);
			}
			return  dsIds;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dsIds;
	}

	public ArrayList<String> getAllEnabledDsIds() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		String id = "";
		String status = "1";
		ArrayList<String> dsIds = new ArrayList<String>();

		try {
			ps = con.prepareStatement(GET_ALL_DSID_ENABLED);
			ps.setString(1, status);
			rs = ps.executeQuery();

			while (rs.next()) {
				id = rs.getString(1);
				dsIds.add(id);
			}
			return dsIds;

		} catch (Exception e) {
			e.printStackTrace();
		}

		return dsIds;
	}

	public StringTokenizer getAllBagOfWords() {
		PreparedStatement ps = null;
		ResultSet rs = null;
		StringTokenizer myvals = null;
		List<String> bagOfWords = new ArrayList<String>();
		try {
			ps = con.prepareStatement(GET_ALL_BOW);
			rs = ps.executeQuery();

			while (rs.next())
			{
				String word = rs.getString(1);
				bagOfWords.add(word);
			}
			String abc = bagOfWords.toString();
			myvals = new StringTokenizer(abc, ", ");
			return  myvals;

		} catch (Exception e) {
			LOGGER.info("DatasourceManagementDao.getAllBagOfWords has issues " + e);

		}

		LOGGER.debug("getAllBagOfWords() method end");
		return myvals;
	}

	private boolean checkFrameParam(int dsmaster_id) {
		String query = CHECK_FRAMEPARAM;
		boolean flag = false;
		try {
			PreparedStatement ps = con.prepareStatement(query);
			ps.setInt(1, dsmaster_id);
			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				flag = (rs.getInt(1) > 0 ? true : false);
			}
		} catch (Exception e) {

			log.error(e.getMessage());
		}
		return flag;
	}

	public Result getDataSourceEmage(String dsId) {
		Result result = new Result();
		int status = CommonUtil.getEmageStatus(dsId);
		if (status == 1) {
			// both temp and viz files are found
			log.info("success " + status);
			result.setResultCode(ResultConfig.getProperty(dsEmageSuccCode));
			result.setStatus(ResultConfig.getProperty(SUCCESS));
			result.setComment(ResultConfig.getProperty(dsEmageSuccComment));
		} else {
			log.info("error " + status);
			result.setResultCode(ResultConfig.getProperty(dsEmageErrCode));
			result.setStatus(ResultConfig.getProperty(FAILURE));
			result.setComment(ResultConfig.getProperty(dsEmageErrComment));
		}

		return result;

	}

	public boolean enableDataSource(int dsID) {

		boolean status = true;
		try {
			PreparedStatement ps = con.prepareStatement(ENABLE_DS);
			ps.setInt(1, dsID);
			ps.executeUpdate();
		} catch (Exception e) {
			log.error("Exception in enabling DataSource {}", e);
			e.printStackTrace();
			status = false;
		}
		return status;

	}

	public boolean disableDataSource(int dsID) {
		PreparedStatement ps = null;
		boolean status = true;
		try {
			ps = con.prepareStatement(DISABLE_DS);
			ps.setInt(1, dsID);
			ps.executeUpdate();
		} catch (Exception e) {
			log.error("Exception in disabling DataSource {}", e);
			e.printStackTrace();
			status = false;
		}
		return status;

	}


}
