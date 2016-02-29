package com.eventshop.eventshoplinux.constant;

import com.eventshop.eventshoplinux.util.commonUtil.Config;

public class Constant {

	public static final String GET_LINKED_DS = "SELECT linked_ds FROM Query_Master";
	public static final String DELETE_QUERY_MASTER_QRY = "DELETE FROM Query_Master WHERE query_id=?";
	public static final String INSERT_QUERY_MASTER_QRY = "INSERT INTO Query_Master(query_creator_id,query_name,query_esql,time_window,latitude_unit,"
			+ "longitude_unit,boundingbox, linked_ds) values(?,?,?,?,?,?,?,?)";
	public static final String DELETE_WRAPPER_QUERY = "DELETE FROM Wrapper WHERE dsmaster_Id=?";
	public static final String DELETE_DS_RESOLUTION_QUERY = "DELETE FROM Datasource_Resolution WHERE dsmaster_Id=?";
	public static final String DELETE_DS_MASTER_QUERY = "DELETE FROM Datasource_Master WHERE dsmaster_Id=?";
	public static final String INSERT_WRAPPER_QRY = "INSERT INTO Wrapper(wrapper_name,wrapper_key_value,bag_of_words,dsmaster_id) values(?,?,?,?)";
	public static final String INSERT_DATASOURCE_RESOLUTION_QRY = "INSERT INTO Datasource_Resolution(dsmaster_id,time_window,latitude_unit,longitude_unit,boundingbox,syncTime) values(?,?,?,?,?,?)";
	public static final String INSERT_DATASOURCE_QRY = "INSERT INTO Datasource_Master(dsmaster_title,dsmaster_theme,dsmaster_url,dsmaster_format,"
			+ "dsmaster_creator,dsmaster_created_date,dsmaster_updated_date, dsmaster_syntax) values(?,?,?,?,?,now(),now(),?)";
	public static final String INSERT_RULE_QRY= "INSERT INTO RuleQueryMaster (Rule_Name, source_id, Rule_Queries, source_fields, user_id) values (?,?,?,?,?)";
	public static final String INSERT_DEFAULT_RULE_QRY="INSERT INTO RuleQueryMaster (Rule_Name, Rule_Queries, source_id, source_fields, user_id) values (?,?,?,?,?)";
	public static final String UPDATE_RULE_QRY="UPDATE RuleQueryMaster set Rule_Name=?,source_id=?,Rule_Queries=?,source_fields=? where RuleID = ?";
	public static final String GET_ALL_DSID = "SELECT dsmaster_id FROM Datasource_Master";
	public static final String GET_ALL_DSID_ENABLED = "SELECT dsmaster_id FROM Datasource_Master where dsmaster_status = ?";
	public static final String GET_ALL_BOW = "SELECT bag_of_words FROM Wrapper where bag_of_words != \"\"";
	public static final String GET_MAX_ALRT_ID = "SELECT MAX(alert_id) FROM Alerts";
	public static final String DISABLE_ALL_ALERTS= "UPDATE Alerts SET alert_status = 0";
	public static final String GET_ALL_ALERTS_FOR_USER = "SELECT alert_id,alert_name,alert_type,alert_theme,alert_src,safe_src,alert_src_min,alert_src_max,safe_src_min,safe_src_max,alert_status,resultEndpoint,alert_message,lat,lng,radius FROM Alerts WHERE user_id = ?";
	public static final String GET_ALL_ALERTS = "SELECT * FROM Alerts";
	public static final String GET_ALL_ENABLED_ALERTS = "SELECT * FROM Alerts WHERE alert_status = 1";
	public static final String GET_ALL_ENABLED_ALERTS_FOR_ID = "SELECT * FROM Alerts WHERE alert_status = 1 AND (alert_src = ? OR safe_src = ?)";

	public static final String SELECT_ALL_RULE_ID_QRY= "SELECT RuleID,source_id from RuleQueryMaster";
	public static final String DS_STAT_QRY= "SELECT dsmaster_status FROM Datasource_Master WHERE dsmaster_id = ?";
	public static final String QUERY_STAT_QRY= "SELECT query_status FROM Query_Master WHERE query_id = ?";
	public static final String GET_ALERT_DTLS = "SELECT alert_type, alert_name, alert_theme, alert_src, alert_src_min, alert_src_max, safe_src, safe_src_min, safe_src_max, alert_status, resultEndpoint, alert_message, boundingbox, lat, lng, radius   FROM Alerts WHERE alert_id = ?";
	public static final String DISABLE_ALERT_QRY= "UPDATE Alerts SET alert_status = 0 WHERE alert_id = ?";
	public static final String ENABLE_ALERT_QRY= "UPDATE Alerts SET alert_status = 1 WHERE alert_id = ?";
	public static final String INST_SINGLE_ALT_QRY= "INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,resultEndpoint,alert_message,lat,lng,radius) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String INST_SINGLE_ALT_QRY_WITH_ENDPOINT = "INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max,resultEndpoint,alert_message) values (?,?,?,?,?,?,?,?,null,null,null,?,?)";
	public static final String INST_SINGLE_ALT_QRY_WITH_ENDPOINT_AND_BOUNDINGBOX="INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max,resultEndpoint,alert_message,boundingbox,lat,lng,radius) values (?,?,?,?,?,?,?,?,null,null,null,?,?,?,?,?,?)";

	public static final String UPDATE_SOL_ALT_QRY="UPDATE Alerts SET alert_name=?,alert_type=?,alert_theme=?,alert_src=?,alert_src_min=?,alert_src_max=?,safe_src=?,safe_src_min=?,safe_src_max=?,resultEndpoint=?,alert_message=?,lat=?,lng=?,radius=? WHERE alert_id=?";
	public static final String INST_SOL_ALT_QRY="INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max,resultEndpoint,alert_message,lat,lng,radius) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String INST_SOL_ALT_QRY_WITH_ENDPOINT = "INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max, resultEndpoint, alert_message) values (?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String INST_SOL_ALT_QRY_WITH_ENDPOINT_AND_BOUNDINGBOX="INSERT INTO Alerts (alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max,resultEndpoint,alert_message,boundingbox,lat,lng,radius) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

	public static final String INSERT_ALERT= "INSERT INTO Alerts (alert_id,alert_name,alert_type,alert_theme,alert_src,alert_src_min,alert_src_max,alert_status,user_id,safe_src,safe_src_min,safe_src_max) values (?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String Query = "Query";
	public static final String DS = "DS";
	public static final String userId = "userId";
	public static final String dsmaster_id = "dsmaster_id";
	public static final String dsmaster_title = "dsmaster_title";
	public static final String STATUS_NOT_AVAILABLE = "Connecting";
	public static final String STATUS_AVAILABLE = "Available";
	public static final String SELECT_DSMSTR_QRY = "SELECT dm.dsmaster_id, dm.dsmaster_title,dm.dsmaster_creator FROM Datasource_Master dm WHERE dm.dsmaster_creator=?";
	public static final String SLCT_DSMSTR_DSNAMES_QRY = "SELECT dm.dsmaster_title FROM Datasource_Master dm WHERE dm.dsmaster_creator=?";
	public static final String SELECT_USERMSTR_QRY = "SELECT um.user_id,um.user_fullname,um.user_email,um.user_authen_key,rm.role_type,um.user_status,um.user_last_accessd,um.user_role_id FROM tbl_User_Master um,tbl_Role_Master rm WHERE um.user_role_id=rm.role_id";
	public static final String TEMPDIR = "tempDir";
	public static final String SELECT_QRYMSTR = "SELECT query_id,query_name,query_status FROM Query_Master WHERE query_creator_id=?";
	public static final String SLCT_QRYMSTR_QYNAMES_QRY = "SELECT query_name FROM Query_Master WHERE query_creator_id=?";
	public static final String UPDATE_USERMSTR_STATUS_QRY = "UPDATE tbl_User_Master SET user_status=? WHERE user_id=?";
	public static final String NO_DATA = "No data provided";
	public static final String SUCCESS = "success";
	public static final String DB_EXPT = "DB Exception";
	public static final String UPDATE_USERMSTR_QRY = "UPDATE tbl_User_Master um SET um.user_fullname=?,um.user_email=?,um.user_authen_key=?,um.user_role_id=?,um.user_status=? WHERE um.user_id=?";
	public static final String SELECT_DSMSTR_DSRES_QRY = "SELECT ds.dsmaster_id,ds.dsmaster_theme,ds.dsmaster_desc,ds.dsmaster_format,ds.dsmaster_title,dr.boundingbox,ds.dsmaster_access,dr.time_window FROM Datasource_Master ds, Datasource_Resolution dr WHERE ds.dsmaster_id=dr.dsmaster_id"; // passing
	// time_window
	// for
	// registerservlet
	// --sanjukta
	// 06-08-2014
	public static final String PATH_DS = "/temp/ds/";
	public static final String PATH_Q = "/temp/queries/";
	public static final String UNDERSCORE = "_";
	public static final String SELECT_DS_QRY = "SELECT ds.dsmaster_id,ds.dsmaster_theme,ds.dsmaster_desc,ds.dsmaster_url,ds.dsmaster_format,ds.dsmaster_type,um.user_email as Creater,ds.dsmaster_archive,ds.dsmaster_unit,ds.dsmaster_created_date FROM Datasource_Master ds,tbl_User_Master um WHERE ds.dsmaster_creator=um.user_id AND ds.dsmaster_id=?";
	public static final String SELECT_DS_RES = "SELECT dsmaster_id, time_window, latitude_unit, longitude_unit, boundingbox, regrid_function, resolution_type FROM Datasource_Resolution WHERE dsmaster_id=? AND resolution_type='initial' ORDER BY dsresolution_id DESC limit 1";
	public static final String SELECT_DS_QRY_TIMEWIN = "SELECT ds.dsmaster_id,ds.dsmaster_theme,ds.dsmaster_desc,ds.dsmaster_url,ds.dsmaster_format,ds.dsmaster_type,ds.dsmaster_archive,ds.dsmaster_unit,ds.dsmaster_created_date,dr.time_window,um.user_email FROM Datasource_Master ds,Datasource_Resolution dr, tbl_User_Master um WHERE ds.dsmaster-id=dr.dsmaster_id and ds.dsmaster_creator=um.user_id AND ds.dsmaster_id=?";
	public static final String SELECT_USERDS_QRY = "SELECT um.user_id,um.user_fullname FROM tbl_User_Master um, tbl_User_Datasource uds WHERE uds.user_id=um.user_id AND uds.dsmaster_id=?";
	public static final String FAILURE = "failure";
	public static final String DEL_USERDS_QRY = "DELETE FROM tbl_User_Datasource WHERE dsmaster_Id=?";
	public static final String INSERT_USERDS_QRY = "INSERT INTO tbl_User_Datasource (user_id,dsmaster_id) VALUES (?,?)";
	public static final String DEL_DSMSTR_QRY = "DELETE FROM Datasource_Master WHERE dsmaster_Id=?";
	// public static String DEL_DSRES_QRY =
	// "DELETE FROM Datasource_Resolution WHERE dsmaster_Id=?";
	// public static String DEL_DSRES_QRY =
	// "DELETE FROM "+tablName+" WHERE "+colName+"=?";
	public static final String SELECT_QRYMSTR_ADMIN_QRY = "SELECT query_id,query_name,boundingbox,query_status FROM Query_Master WHERE qid_parent IS NULL";
	public static final String SELECT_ENABLED_QUERIES_WITH_DS_ID
			= "SELECT query_id,query_creator_id,query_name,query_desc,query_esql,time_window,latitude_unit,longitude_unit,boundingbox,query_status, qid_parent FROM Query_Master WHERE query_status=? and linked_ds LIKE ?";
	public static final String SELECT_QRYMSTR_ALL = "SELECT * FROM Query_Master";

	public static final String ENABLE_QUERY = "UPDATE Query_Master SET query_status = 1 WHERE query_id = ?";
	public static final String DISABLE_QUERY = "UPDATE Query_Master SET query_status = 0 WHERE query_id = ?";



	public static final String SELECT_QRYMSTR_QRY = "SELECT * FROM Query_Master WHERE qid_parent IS NULL";
	public static final String SELECT_QRY_BYID = "SELECT query_name,boundingbox,query_status,time_window FROM Query_Master WHERE query_id=?";
	public static final String CONTROLFLAG = "R";
	public static final String DEL_QRYMSTR_QRY = "DELETE FROM Query_Master WHERE query_id=?";
	public static final String DRIVER_NAME = "com.mysql.jdbc.Driver";
	public static final String DB_URL = "dbUrl";
	public static final String USR_NAME = "usr";
	public static final String PASSWORD = "pwd";
	public static final String SELECT_USRMSTR_QRY = "SELECT * FROM tbl_User_Master";
	public static final String SELECT_USR_FROM_EMAIL = "SELECT user_id FROM tbl_User_Master WHERE user_email = ?";
	public static final String INSERT_USERMSTR_QRY = "INSERT INTO tbl_User_Master(user_email,user_password,user_fullname,user_gender,user_authen_key,user_role_id,user_created_date,user_last_accessd) values(?,AES_ENCRYPT(?,?),?,?,?,?,now(),now())";
	public static final String ADMIN = "Admin";
	public static final String SELECT_USRMSTR_AUTH_QRY = "SELECT user_authen_key from tbl_User_Master WHERE user_email=?";
	public static final String SELECT_USRMSTR_ARG_QRY = "SELECT user_id FROM tbl_User_Master WHERE user_email=? and user_password=AES_ENCRYPT(?,?)";
	public static final String SELECT_USRMSTR_ADMIN_QRY = "SELECT A.user_id FROM tbl_User_Master A, tbl_Role_Master B WHERE A.user_role_id=B.role_id and B.role_type='";
	public static final String SELECT_USRMSTR_ADMIN_ARG_QRY = "' and A.user_email=? and A.user_password=AES_ENCRYPT(?,?)";
	public static final String USER_ID = "user_id";
	public static final String WRAPPER_TYPE = "PULL";
	public static final String INITIAL_RESOLUTION = "initial";
	public static final String FINAL_RESOLUTION = "final";
	public static final String INSERT_DSMSTR_QRY = "INSERT INTO Datasource_Master(dsmaster_title,dsmaster_theme,dsmaster_url,dsmaster_format,"
			+ "dsmaster_syntax,dsmaster_created_date,dsmaster_updated_date,dsmaster_creator) values(?,?,?,?,?,now(),now(),?)";
	public static final String EMPTY_STRING = "";
	public static final String SELECT_LST_INSID = "SELECT LAST_INSERT_ID()";
	public static final String UPDATE_DSMSTR_QRY = "UPDATE Datasource_Master SET dsmaster_title=?, dsmaster_theme=?, dsmaster_url=?, "
			+ "dsmaster_format=?, dsmaster_updated_date=now(), dsmaster_syntax=? WHERE dsmaster_id=?";
	public static final String INSRT_WRPR_QRY = "INSERT INTO Wrapper(wrapper_name,wrapper_type,wrapper_key_value,bag_of_words,visual_tran_mat,visual_color_mat,visual_mask_mat,visual_ignore_since,csv_file_url,dsmaster_id) values(?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_WRPR_QRY = "UPDATE Wrapper SET wrapper_name=?, wrapper_type=?, wrapper_key_value=?, bag_of_words=?, visual_tran_mat=?, visual_color_mat=?, visual_mask_mat=?, "
			+ "visual_ignore_since=?, csv_file_url=? where wrapper_id=? ";
	public static final String INSERT_DSRES_QRY = "INSERT INTO Datasource_Resolution(dsmaster_id,datastream_name,time_window,syncTime,latitude_unit,"
			+ "longitude_unit,boundingbox, resolution_type, timeType, dsQuery, genEmage) values(?,?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_DSRES_QRY = "UPDATE Datasource_Resolution SET time_window=?, latitude_unit=?, longitude_unit=?, "
			+ "boundingbox=?, syncTime=?, timeType=? , dsQuery=?, genEmage=? WHERE dsmaster_id=? AND resolution_type=?";
	public static final String SELECT_DSMSTR_ALL_QRY = "SELECT dm.dsmaster_title,dm.dsmaster_theme,dm.dsmaster_url,dm.dsmaster_format,dm.dsmaster_creator,dm.dsmaster_created_date,dm.dsmaster_updated_date,dm.dsmaster_syntax"
			+ " FROM Datasource_Master dm  WHERE dm.dsmaster_id=?";
	public static final String SELECT_RULE_QRY= "SELECT * from RuleQueryMaster where RuleID=?";
	public static final String SELECT_ALL_RULE_QRY = "SELECT * from RuleQueryMaster where user_id=?";
 	public static final String SELECT_RULE_DS_QRY= "select dm.dsmaster_id, dm.dsmaster_title, rm.RuleID, rm.Rule_Name from Datasource_Master dm, RuleQueryMaster rm  where dm.dsmaster_id = rm.source_id and  dsmaster_creator = ? and rm.source_type='ds'";

	public static final String SELECT_ENABLED_RULE_OPERATOR_QRY = "SELECT r.Operator_id, r.DataType, r.Operators, r.status from RuleOperator r where r.status=1";

	public static final String SELECT_DSRESWRPR_QRY = "SELECT dm.dsmaster_title,dm.dsmaster_theme,dm.dsmaster_url,dm.dsmaster_format,dm.dsmaster_creator,dm.dsmaster_created_date,dm.dsmaster_updated_date,"
			+ "dr.dsmaster_id,dr.datastream_name,dr.time_window,dr.latitude_unit,dr.longitude_unit,dr.boundingbox,"
			+ "wr.wrapper_name,wr.wrapper_type,wr.wrapper_key_value,wr.bag_of_words,wr.visual_tran_mat,wr.visual_color_mat,wr.visual_mask_mat,wr.visual_ignore_since"
			+ " FROM Datasource_Master dm,Datasource_Resolution dr,Wrapper wr  WHERE dm.dsmaster_id=dr.dsmaster_id AND wr.wrapper_id=dm.dsmaster_id AND dm.dsmaster_id=?";

	public static final String DEFAULT="default";
	public static final String DEFAULT_EXTRACT_FIELD="loc, value, timestamp";
	public static final String SELECT_DSRES_QRY = "SELECT dr.dsmaster_id,dr.datastream_name,dr.time_window,dr.latitude_unit,dr.longitude_unit,dr.boundingbox,dr.resolution_type, dr.syncTime, dr.timeType, dr.dsQuery, dr.genEmage FROM Datasource_Resolution dr WHERE dr.dsmaster_id=?";
	public static final String SELECT_WRPR_QRY = "SELECT wr.wrapper_id,wr.wrapper_name,wr.wrapper_type,wr.wrapper_key_value,wr.bag_of_words,wr.visual_tran_mat,wr.visual_color_mat,wr.visual_mask_mat,wr.visual_ignore_since FROM Wrapper wr WHERE wr.dsmaster_id=? ";
	public static final String SELECT_DSMSTR_ID = "SELECT dsmaster_id FROM Datasource_Master where dsmaster_theme=? and dsmaster_title=? and dsmaster_creator=?";
	public static final String DSMSTR_URL = "dsmaster_url";
	public static final String DSMSTR_THEME = "dsmaster_theme";
	public static final String DSMSTR_FRMT = "dsmaster_format";
	public static final String STREAM = "stream";
	public static final String VISUAL = "visual";
	public static final String REST = "rest";
	public static final String CSV = "csv"; // not sure why we didnt have this
	// -- sanjukta
	public static final String WRAPPER_NAME = "wrapper_name";
	public static final String BAG_OF_WORDS = "bag_of_words";
	public static final String COMMA = ",";
	public static final String VISUAL_TRANS_MAT = "visual_tran_mat";
	public static final String VISUAL_COLR_MAT = "visual_color_mat";
	public static final String VISUAL_IGNOR_SINCE = "visual_ignore_since";
	public static final String TIME_WINDOW = "time_window";
	public static final String SYNC_TIME = "syncTime";
	public static final String TIME_TYPE = "timeType";
	public static final String DS_QUERY="dsQuery";
	public static final String GEN_EMAGE="genEmage";

	public static final String RULE_ID="RuleID";
	public static final String SOURCE_ID="source_id";
	public static final String RULE_NAME="Rule_Name";
	public static final String RULE_QUERIES="Rule_Queries";
	public static final String SOURCE_FIELDS="source_fields";


	public static final String LATITUDE_UNIT = "latitude_unit";
	public static final String LONGITUDE_UNIT = "longitude_unit";
	public static final String BOUNDINGBOX = "boundingbox";
	public static final String SELECT_DSMSR_QRY = "SELECT dm.dsmaster_id, dm.dsmaster_title,dm.dsmaster_creator,dm.dsmaster_url, dm.dsmaster_access FROM Datasource_Master dm";
	public static final String SLT_DSMSR_QRY_BASEON_ID_QRY = " WHERE dsmaster_creator=? or dsmaster_access = \"public\"";
	public static final String SELECT_QRMSTR_QRY = "SELECT query_id,query_name,query_status FROM Query_Master WHERE qid_parent IS NULL";
	// OR query_creator_id='' removed this, nt sure why it was put --sanjukta
	public static final String SLT_QRMSTR_BASEON_ID_QRY = " AND query_creator_id=? ";
	public static final String DATE_CONVERTION = "yyyy-MM-dd HH:mm:ss";
	public static final String SLT_DSMSTR_RUN_QRY = "SELECT dsm.dsmaster_title,dsm.dsmaster_theme,dsm.dsmaster_url,dsm.dsmaster_type,wr.wrapper_name,wr.bag_of_words,wr.visual_mask_mat,wr.visual_color_mat,wr.visual_tran_mat FROM Datasource_Master dsm,Wrapper wr WHERE dsm.dsmaster_id=wr.wrapper_id AND dsm.dsmaster_id=?";
	public static final String SLT_DSRES_RUN_QRY = "SELECT time_window,latitude_unit,longitude_unit,boundingbox FROM Datasource_Resolution WHERE dsmaster_id=?";
	public static final String DS_COL_NAME = "dsmaster_Id";
	public static final String DS_TABLE_NAME = "Datasource_Resolution";
	public static final String WRP_TABLE_NAME = "Wrapper";
	public static final String WRP_COL_NAME = "wrapper_id";
	public static final String DEL = "DELETE FROM ";
	public static final String WHERE = " WHERE ";
	public static final String PARMID = "=?";
	public static final String QRDS_TABLE_NAME = "Query_Datasource";
	public static final String QRDS_COL_NAME = "query_id";
	public static final String QRMS_TABLE_NAME = "Query_Master";
	public static final String QRMS_COL_NAME = "qid_parent";
	public static final String TRANSMATRIX = "tranMatrix";
	public static final String COLRSMATRIX = "colorMatrix";
	public static final String CONTEXT = "context";
	public static final String TWITTER = "Twitter";
	public static final String FLICKER = "Flickr";
	public static final String SIM = "sim";
	public static final String DS_PATH = "ds";
	public static final String TRANS_MATRX_NWLINE = "tranMat:\n ";
	public static final String NWLINE = "\n";
	public static final String COLOR_MATRX_NWLINE = "colorMatStr:\n ";
	public static final String COMMA_BLANK = ", ";
	public static final String RESULT_DS = "temp/ds/";
	public static final String RESULT_Q = "temp/queries/";
	public static final String STOP = "S";
	public static final String RUN = "RUN";
	public static final String ADD = "Add";
	public static final String SLT_QRYMSTR_RUN_QRY = "SELECT time_window,latitude_unit,longitude_unit,boundingbox,timeType FROM Query_Master WHERE query_id=?";
	public static final String INST_QRYMSTR_QRY = "INSERT INTO Query_Master(query_esql,time_window,latitude_unit,"
			+ "longitude_unit,boundingbox,query_status,qid_parent,query_creator_id,query_name) values(?,?,?,?,?,?,?,?,?)";
	public static final String INST_QRYMSTR_DEFAULT_QRY = "INSERT INTO Query_Master(query_esql,time_window,latitude_unit,"
			+ "longitude_unit,boundingbox,query_status,query_creator_id,query_name, timeType, linked_ds) values(?,?,?,?,?,?,?,?,?,?)";
	public static final String SLT_QRYMSTR_QRY = "SELECT query_esql FROM Query_Master WHERE query_id=?";
	public static final String Alert_STAT_QRY="SELECT alert_status from Alerts where alert_id=?";
	public static final String ALERT_INTERVAL="alertInterval";
	public static final String FILTER = "filter";
	public static final String GROUPING = "grouping";
	public static final String AGGREGATION = "aggregation";
	public static final String SPCHAR = "spchar";
	public static final String SPMATCHING = "spmatching";
	public static final String TPCHAR = "tpchar";
	public static final String TPMATCHING = "tpmatching";
	public static final String DSR_DS = "ds";
	public static final String MAP = "map";
	public static final String TEXTUAL = "textual";
	public static final String MATRIX = "matrix";
	public static final String GAUSSIAN = "gaussian";
	public static final String FILE = "file";
	public static final String LINUXEXECFILEEXTN = "";
	public static final String WINEXECFILEEXTN = ".exe";
	public static final String LINUXOS = "linux";
	public static final String WINOS = "windows";
	public static final String RUNNING = "R";
	public static final String STOPPED = "S";
	public static final String SELECT_USERNOTINDS_QRY = "SELECT um.user_id,um.user_fullname FROM tbl_User_Master um WHERE um.user_id NOT IN (?)";
	public static final String CHECK_FRAMEPARAM = "SELECT count(*) from Datasource_Resolution WHERE dsmaster_id=?";
	public static final String ENABLE_DS = "UPDATE Datasource_Master SET dsmaster_status = 1 WHERE dsmaster_id = ?";
	public static final String DISABLE_DS = "UPDATE Datasource_Master SET dsmaster_status = 0 WHERE dsmaster_id = ?";

	// Strings mapping to the resultConfig.props
	public static final String dsAddSuccCode = "dsAddSuccCode";
	public static final String dsAddSuccComment = "dsAddSuccComment";
	public static final String dsAddErrCode = "dsAddErrCode";
	public static final String dsAddErrComment = "dsAddErrComment";
	public static final String dsUpdtSuccCode = "dsUpdtSuccCode";
	public static final String dsUpdtSuccComment = "dsUpdtSuccComment";
	public static final String dsUpdtErrCode = "dsUpdtErrCode";
	public static final String dsUpdtErrComment = "dsUpdtErrComment";
	public static final String dsEmageSuccCode = "dsEmageSuccCode";
	public static final String dsEmageSuccComment = "dsEmageSuccComment";
	public static final String dsEmageErrCode = "dsEmageErrCode";
	public static final String dsEmageErrComment = "dsEmageErrComment";
	public static final String qryAddSuccCode = "qryAddSuccCode";
	public static final String qryAddSuccCmnt = "qryAddSuccCmnt";
	public static final String qryAddErrCode = "qryAddErrCode";
	public static final String json = ".json";

	// resource for pre-sample data source
	public static final String RESOURCEDIR = Config.getProperty(TEMPDIR)
			+ "/resource/";
	public static final String csvArrayURI = RESOURCEDIR + "/csv_array_usa";
	public static final String csvFieldURI = RESOURCEDIR + "/csv_field_1_2_3";
	public static final String pollenURI = RESOURCEDIR + "/pollen_map.gif";
	public static final String pollenTRAN = RESOURCEDIR + "/pollen_trans_mat";
	public static final String pollenCOL = RESOURCEDIR + "/pollen_col_mat";
	public static final String pollenMASK = RESOURCEDIR + "/pollen_mask.png";
	public static final String aqiUI = RESOURCEDIR + "/aqi_map.jpg";
	public static final String aqiTRAN = RESOURCEDIR + "/aqi_trans_mat";
	public static final String aqiCOL = RESOURCEDIR + "/aqi_col_mat";
	public static final String aqiMASK = RESOURCEDIR + "/aqi_mask.png";

	public static final String datasourceJsonLoc = Config.getProperty("datasourceJsonLoc");
}