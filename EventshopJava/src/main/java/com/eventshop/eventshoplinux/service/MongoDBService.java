package com.eventshop.eventshoplinux.service;

import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mongodb.DBObject;

@Path("/mongodb")
public class MongoDBService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{db}")
	/*
	 * This method returns all collections
	 */
	public Set<String> getCollections(@PathParam(value = "db") String dbName) {
		MongoDB myDb = new MongoDB(dbName);
		return myDb.getCollection();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{db}/{col}/findone")
	/*
	 * This method returns first record in the collection
	 */
	public DBObject findOne(@PathParam(value = "db") String dbName,
			@PathParam(value = "col") String colName) {
		MongoDB myDb = new MongoDB(dbName);
		myDb.setCollection(colName);
		return myDb.findOne();
	}

	/* shouldn't use this if the amount of documents are large. */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{db}/{col}/findall")
	/*
	 * This method returns first record in the collection
	 */
	public List<DBObject> findAll(@PathParam(value = "db") String dbName,
			@PathParam(value = "col") String colName) {
		MongoDB myDb = new MongoDB(dbName);
		myDb.setCollection(colName);
		return myDb.find("");
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{db}/{col}/timerange/{startTime}/{endTime}/{timeField}")
	/*
	 * This method returns first record in the collection
	 */
	public List<DBObject> findInTimeRange(
			@PathParam(value = "db") String dbName,
			@PathParam(value = "col") String colName,
			@PathParam(value = "startTime") String startTime, // e.g.,
																// 2013-08-05
			@PathParam(value = "endTime") String endTime, // e.g., 2013-09-05
			@PathParam(value = "timeField") String timeField) {
		MongoDB myDb = new MongoDB(dbName);
		myDb.setCollection(colName);
		String qstr = "{$and:[{" + timeField + ":{'$gt':'" + startTime + "'}},"
				+ "{" + timeField + ":{'$lt':'" + endTime + "'}}]}";
		return myDb.find(qstr);
	}

	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/{db}/{col}/find")
	// public List<DBObject> find(
	// @PathParam("db") String dbName,
	// @PathParam("col") String colName,
	// @QueryParam("sdate") String startDate){
	//
	// System.out.println("startDate: " + startDate + ", " + dbName + ", " +
	// colName);
	// return null;
	// }
	//
	//
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{db}/{col}/find")
	public List<DBObject> find(
			@PathParam(value = "db") String dbName,
			@PathParam(value = "col") String colName,
			@FormParam("sdate") String startDate, // e.g., "2013-08-05"
			@FormParam("edate") String endDate, @FormParam("lat") Double lat,
			@FormParam("lng") Double lng, @FormParam("r") Double mile,
			@FormParam("f") String fields) {

		Double radious = 100.0 / 3959.0;
		if (mile != null)
			radious = mile / 3959.0;

		String qstr = "{$and:[{date:{$gt:'" + startDate + "',$lt:'" + endDate
				+ "'}},{'loc':{$geoWithin:{$centerSphere:[[" + lng + "," + lat
				+ "]," + radious + "]}}}]}";
		System.out.println(qstr);
		System.out.println(fields);
		MongoDB myDb = new MongoDB(dbName);
		myDb.setCollection(colName);
		if (fields != null && fields != "")
			return myDb.find(qstr, fields);
		else
			return myDb.find(qstr);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{host}/{port}/{db}/{col}/find")
	public List<DBObject> find(
			@PathParam(value = "host") String dbHost,
			@PathParam(value = "port") int dbPort,
			@PathParam(value = "db") String dbName,
			@PathParam(value = "col") String colName,
			@FormParam("sdate") String startDate, // e.g., "2013-08-05"
			@FormParam("edate") String endDate, @FormParam("lat") Double lat,
			@FormParam("lng") Double lng, @FormParam("r") Double mile,
			@FormParam("f") String fields) {

		Double radious = 100.0 / 3959.0;
		if (mile != null)
			radious = mile / 3959.0;

		String qstr = "{$and:[{date:{$gt:'" + startDate + "',$lt:'" + endDate
				+ "'}},{'loc':{$geoWithin:{$centerSphere:[[" + lng + "," + lat
				+ "]," + radious + "]}}}]}";
		System.out.println(qstr);
		System.out.println(fields);
		MongoDB myDb = new MongoDB(dbHost, dbPort, dbName);
		myDb.setCollection(colName);
		if (fields != null && fields != "")
			return myDb.find(qstr, fields);
		else
			return myDb.find(qstr);
	}

	// @DELETE
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/datasources")
	/*
	 * This method used for deleting Datasource Details
	 */
	// public String deleteDS(@JsonProperty("selectedDSIds") HashMap
	// selectedDSIds) {
	// List<String> selectedList = (List<String>) selectedDSIds
	// .get("selectedDSIds");
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// String status = adminDAO.deleteDS(selectedList);
	// return status;
	// }

	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/datasources")

	/*
	 * This method will RUN & STOP the DS
	 */

	// public String runDatasources(
	// @JsonProperty("selectedDSRunIds") HashMap selectedDSRunIds) {
	// //ui has to pass whether it is run or stop and datasource id
	// List<String> selectedDSList = (List<String>) selectedDSRunIds
	// .get("selectedDSRunIds");
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// int lastIndex = (selectedDSList.size()) - 1;
	// String playStatus = selectedDSList.get(lastIndex);
	// selectedDSList.remove(lastIndex); // removing the last index
	// DataSourceHelper dataPrsHelper = null;
	// List<DataSource> datasrcList = adminDAO.getDatasource(selectedDSList);
	// dataPrsHelper = new DataSourceHelper();
	// dataPrsHelper.runDatasources(datasrcList,playStatus);
	// return "Success";
	//
	// }

	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/datasources/users/{dsMasterId}")
	// /*
	// * This method will give the user list associated to the DS
	// */
	// public List getDSUsers(
	// @PathParam(value = "dsMasterId") String dsMasterId) {
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// List<User> dsUsers = (List)adminDAO.getDSUsers(dsMasterId);
	// String userIds = "";
	// for(int i=0;i<dsUsers.size();i++) {
	// userIds = new
	// StringBuffer().append(userIds).append(dsUsers.get(i).getId()).append(',').toString();
	// }
	//
	// userIds = userIds.substring(0, userIds.length()-1); // need to check
	// length
	// List<User> dsOtherUsers = (List)adminDAO.getUsersNotInDS(userIds);
	// List list = new ArrayList();
	// list.add(dsUsers);
	// list.add(dsOtherUsers);
	// return list;
	// }
	//
	// @GET
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/queries")
	// /*
	// * This method returns all queries for admin
	// */
	// public Query[] getQueries() {
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// Query[] qryListArray = new Query[] {};
	// List<Query> queryList = adminDAO.getQueryList();
	// Query[] qryList;
	// qryList = queryList.toArray(qryListArray);
	// return qryList;
	// }
	//
	// @DELETE
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/queries")
	// /*
	// * This method used for deleting queries
	// */
	// public String deleteQueries(
	// @JsonProperty("selectedQueryIds") HashMap selectedQueryIds) {
	// List<String> selectedList = (List<String>) selectedQueryIds
	// .get("selectedQueryIds");
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// String deleteStatus = adminDAO.deleteQueryList(selectedList);
	// return deleteStatus;
	// }
	//
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/queries")
	//
	// /*
	// * This method will RUN & STOP the DS
	// */
	//
	// public String runQueries(
	// @JsonProperty("selectedDSRunIds") HashMap selectedDSRunIds) {
	// //ui has to pass whether it is run or stop and datasource id
	// List<String> selectedDSList = (List<String>) selectedDSRunIds
	// .get("selectedDSRunIds");
	// AdminManagementDAO adminDAO = new AdminManagementDAO();
	// int lastIndex = (selectedDSList.size()) - 1;
	// String playStatus = selectedDSList.get(lastIndex);
	// selectedDSList.remove(lastIndex); // removing the last index
	// DataSourceHelper dataPrsHelper = null;
	// List<DataSource> datasrcList = adminDAO.getDatasource(selectedDSList);
	// dataPrsHelper = new DataSourceHelper();
	// dataPrsHelper.runDatasources(datasrcList,playStatus);
	// return "Success";
	//
	// }

}