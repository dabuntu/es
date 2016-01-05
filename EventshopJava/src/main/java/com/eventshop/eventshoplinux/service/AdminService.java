package com.eventshop.eventshoplinux.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.annotate.JsonProperty;

import com.eventshop.eventshoplinux.DAO.admin.AdminManagementDAO;
import com.eventshop.eventshoplinux.DAO.user.UserManagementDAO;
//import com.eventshop.eventshoplinux.domain.datasource.DataSourceHelper;
import com.eventshop.eventshoplinux.domain.datasource.DataSourceListElement;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.query.Query;
import static com.eventshop.eventshoplinux.constant.Constant.*;

@Path("/adminservice")
public class AdminService {

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users")
	/*
	 * This method returns all users
	 */
	public User[] getUsers() {
		User[] users = new User[] {};
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		List<User> list = adminDAO.getUserList();
		// User[] userList; //why do we need an additional list?
		users = list.toArray(users); // ui finds it difficult to read unless the
										// list is converted to array
		return users;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users")
	/*
	 * This method creates a user
	 */
	public String createUsers(User[] users) {
		UserManagementDAO eventShopDAO = new UserManagementDAO();
		String result = SUCCESS;
		for (User user : users) {
			result = (eventShopDAO.saveUser(user).equals(SUCCESS) ? result
					: FAILURE); // want to indicate overall failures if any
								// going forward
		}
		return result;
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users/{id}")
	/*
	 * This method updates details of a single user
	 */
	public String updateUser(User user) {

		AdminManagementDAO adminDAO = new AdminManagementDAO();
		return adminDAO.updateUserdetails(user);

	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/userDatasources")
	/*
	 * This method gets userDetails,Datasource and Querylist by Admin for
	 * selected users @QueryParam("selectedUserIds")
	 */
	public HashMap viewUserDatasources(@QueryParam("selectedUserIds") String ids) {
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		return adminDAO.getDataSrcQryLst(ids);

	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users/activate")
	/*
	 * This method activate or Deactivate Users
	 */
	public String activateDeactiveUsers(
			@JsonProperty("selectedUserIds") HashMap selectedUserIds) {
		List<String> selectedList = (List<String>) selectedUserIds
				.get("selectedUserIds");
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		String updateStatus = adminDAO.activateDeactivateUserList(selectedList);
		return updateStatus;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/datasources")
	/*
	 * This method returns all datasources
	 */
	public DataSourceListElement[] getDatasources() {
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		DataSourceListElement[] list = new DataSourceListElement[] {};
		List<DataSourceListElement> dsList = adminDAO.getDataSrcList();
		DataSourceListElement[] viewDSList;
		viewDSList = dsList.toArray(list);
		return viewDSList;

	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/datasources")
	/*
	 * This method used for deleting Datasource Details
	 */
	public String deleteDS(@JsonProperty("selectedDSIds") HashMap selectedDSIds) {
		List<String> selectedList = (List<String>) selectedDSIds
				.get("selectedDSIds");
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		String status = adminDAO.deleteDS(selectedList);
		return status;
	}

	// // move to RegisterServlet
	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/datasources")
	//
	// /*
	// * This method will RUN & STOP the DS
	// */
	//
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

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/datasources/users/{dsMasterId}")
	/*
	 * This method will give the user list associated to the DS
	 */
	public List getDSUsers(@PathParam(value = "dsMasterId") String dsMasterId) {
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		List<User> dsUsers = adminDAO.getDSUsers(dsMasterId);
		String userIds = "";
		for (int i = 0; i < dsUsers.size(); i++) {
			userIds = new StringBuffer().append(userIds)
					.append(dsUsers.get(i).getId()).append(',').toString();
		}

		userIds = userIds.substring(0, userIds.length() - 1); // need to check
																// length
		List<User> dsOtherUsers = adminDAO.getUsersNotInDS(userIds);
		List list = new ArrayList();
		list.add(dsUsers);
		list.add(dsOtherUsers);
		return list;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queries")
	/*
	 * This method returns all queries for admin
	 */
	public Query[] getQueries() {
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		Query[] qryListArray = new Query[] {};
		List<Query> queryList = adminDAO.getQueryList();
		Query[] qryList;
		qryList = queryList.toArray(qryListArray);
		return qryList;
	}

	@DELETE
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queries")
	/*
	 * This method used for deleting queries
	 */
	public String deleteQueries(
			@JsonProperty("selectedQueryIds") HashMap selectedQueryIds) {
		List<String> selectedList = (List<String>) selectedQueryIds
				.get("selectedQueryIds");
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		String deleteStatus = adminDAO.deleteQueryList(selectedList);
		return deleteStatus;
	}

	// move to registerServlet
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