package com.eventshop.eventshoplinux.service;

import static com.eventshop.eventshoplinux.constant.Constant.CONTEXT;
import static com.eventshop.eventshoplinux.constant.Constant.SUCCESS;
import static com.eventshop.eventshoplinux.constant.Constant.json;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.lang.ArrayUtils;
import org.codehaus.jackson.annotate.JsonProperty;

import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.domain.common.EmageElement;
import com.eventshop.eventshoplinux.domain.login.User;
import com.eventshop.eventshoplinux.domain.query.Query;
import com.eventshop.eventshoplinux.domain.query.QueryDTO;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonObject;

@Path("/queryservice")
public class QueryService {

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queriesByUser")
	/*
	 * This method used for displaying queryList accessed by User Home page
	 */
	public Query[] getQueryList(User user) {

		QueryListDAO queryDAO = new QueryListDAO();
		Query[] qryListArray = new Query[] {};
		List<Query> queryList = queryDAO.getUserQuery(user);
		Query[] qryList;
		qryList = queryList.toArray(qryListArray);
		return qryList;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queries")
	/*
	 * This method used for displaying queryList accessed by User Home page
	 */
	public Query[] getQueryList(@QueryParam("userId") int userId) {

		User user = new User();
		user.setId(userId);
		QueryListDAO queryDAO = new QueryListDAO();
		Query[] qryListArray = new Query[] {};
		List<Query> queryList = queryDAO.getUserQuery(user);
		Query[] qryList = queryList.toArray(qryListArray);
		return qryList;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/queries")
	/*
	 * This method will execute the Query/QueryList with Operator
	 */
	public String[] runQuery(
			@JsonProperty("selectedQueryRun") String selectedQueryRun) {

		System.out.println("selectedQueryRun is ::: " + selectedQueryRun);
		JsonParser parser = new JsonParser();
		JsonObject rootObj = parser.parse(selectedQueryRun).getAsJsonObject();
//		System.out.println("parser.parse(selectedQueryRun).getAsJsonObject();::::::::  "  + parser.parse(selectedQueryRun).getAsJsonObject());
		JsonElement queryElement = rootObj.get("query");
		Gson gson = new Gson();
		List<QueryDTO> queryDTOList = null;

		// Check if "project" element is an array or an object and parse
		// accordingly...
		if (queryElement.isJsonObject()) {
			// The returned list has only 1 element
			QueryDTO queryDTO = gson.fromJson(queryElement, QueryDTO.class);
			queryDTOList.add(queryDTO);
		} else if (queryElement.isJsonArray()) {
			Type queryListType = new TypeToken<List<QueryDTO>>() {
			}.getType();
			queryDTOList = gson.fromJson(queryElement, queryListType);
		}

		// System.out.println("Exception");
		QueryListDAO queryListDAO = new QueryListDAO();

		queryDTOList = replaceIntmQuery(queryDTOList);
		try {
			queryListDAO.registerQuery(queryDTOList.get(0),
					queryElement.toString(), queryDTOList.size());
		} catch (Exception e) {
			System.out.println("exc" + e);
		}

		String[] strArr = new String[1];
		strArr[0] = SUCCESS;
		return strArr;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/qemage/{id}")
	public EmageElement getQueryEmageElement(
			@PathParam(value = "id") final String qid) {
		System.out.println("Inside qemage id is " + qid);
		// DataSourceManagementDAO dao=new DataSourceManagementDAO();
		String vizFilePath = Config.getProperty(CONTEXT) + "/temp/queries/Q"
				+ qid + json;
		File viz = new File(vizFilePath);
		BufferedReader br;
		try {
			// while(!viz.exists()){
			// new Thread().sleep(1000);
			// }{
			if (viz.exists()) {
				br = new BufferedReader(new FileReader(vizFilePath));
				EmageElement eme = new Gson().fromJson(br, EmageElement.class);
				return eme;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} // catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		return null;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/qemage/{id}/{level}/{state}")
	public EmageElement getQueryEmageElementLevel(
			@PathParam(value = "id") final String qid,
			@PathParam(value = "level") final int level,
			@PathParam(value = "state") final String state) {
		System.out.println("Inside qemage level, id is " + qid + "level is "
				+ level);
		// DataSourceManagementDAO dao=new DataSourceManagementDAO();
		String vizFilePath = Config.getProperty(CONTEXT) + "/temp/queries/Q"
				+ qid + json;
		File viz = new File(vizFilePath);
		BufferedReader br;
		try {
			// while(!viz.exists()){
			// new Thread().sleep(1000);
			// }{
			if (viz.exists()) {
				br = new BufferedReader(new FileReader(vizFilePath));
				EmageElement eme = new Gson().fromJson(br, EmageElement.class);
				eme.selectState(state);
				eme.reduceSize(level);
				// System.out.println(eme.toString());
				return eme;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} // catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		return null;

	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/qemage/{id}/{level}")
	public EmageElement getQueryEmageElementLevel(
			@PathParam(value = "id") final String qid,
			@PathParam(value = "level") final int level) {
		// DataSourceManagementDAO dao=new DataSourceManagementDAO();
		String vizFilePath = Config.getProperty(CONTEXT) + "/temp/queries/Q"
				+ qid + json;
		File viz = new File(vizFilePath);
		BufferedReader br;
		try {
			// while(!viz.exists()){
			// new Thread().sleep(1000);
			// }{
			if (viz.exists()) {
				br = new BufferedReader(new FileReader(vizFilePath));
				EmageElement eme = new Gson().fromJson(br, EmageElement.class);
				eme.reduceSize(level);
				// System.out.println(eme.toString());
				return eme;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} // catch (InterruptedException e) {
			// e.printStackTrace();
			// }
		return null;

	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/qcode/{id}")
	public String getCode(@PathParam(value = "id") final String qid) {
		// DataSourceManagementDAO dao=new DataSourceManagementDAO();
		String codePath = Config.getProperty(CONTEXT) + "/proc/src/Q" + qid
				+ ".cc";
		File codeFile = new File(codePath);
		BufferedReader br;
		try {
			// while(!viz.exists()){
			// new Thread().sleep(1000);
			// }
			if (codeFile.exists()) {
				br = new BufferedReader(new FileReader(codePath));
				String line;
				StringBuilder code = new StringBuilder();
				while ((line = br.readLine()) != null) {
					code.append(line + '\n');
				}
				return code.toString();
			} else {
				return "not available";
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			// } catch (InterruptedException e) {
			// e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	// @POST
	// @Consumes(MediaType.APPLICATION_JSON)
	// @Produces(MediaType.APPLICATION_JSON)
	// @Path("/runsinglequery")

	/*
	 * This method execute the query from left nav
	 */
	/*
	 * public String runQuery(@JsonProperty("selectedQryId") HashMap
	 * selectedQryId) { List<String> qryIdList = (List<String>)
	 * selectedQryId.get("selectedQryId"); int qryId =
	 * Integer.parseInt(qryIdList.get(0)); //First element is qryId String
	 * runOption = qryIdList.get(1); //Second element is run option QueryListDAO
	 * qryDAO = new QueryListDAO(); String qryEsql = qryDAO.getQryEsql(qryId);
	 * Gson gson = new Gson(); TypeToken<List<QueryDTO>> token = new
	 * TypeToken<List<QueryDTO>>() {}; List<QueryDTO> queryDTOList =
	 * gson.fromJson(qryEsql, token.getType()); QueryHelper qryHelper = new
	 * QueryHelper(); String outPut = qryHelper.queryProcess(queryDTOList,
	 * runOption); return outPut; }
	 */
	/*
	 * This method will replace the qo,q1 to real datasource ids
	 */

	public List<QueryDTO> replaceIntmQuery(List<QueryDTO> queryDTOListInit) {
		for (int i = 0; i < queryDTOListInit.size(); ++i) {
			// checking for q0,q1..and replacing with datasources
			String[] datasrc = queryDTOListInit.get(i).getDataSources();
			String[] dataSourcesFinal = null;
			int intmQryIsExist = 0;
			if (queryDTOListInit.get(i).getDataSources() != null
					&& queryDTOListInit.get(i).getDataSources().length > 0) {
				for (int j = 0; j < datasrc.length; j++) {
					if (datasrc[j].indexOf("q") != -1) {// checking for q0,q1
						String qid = datasrc[j].substring(1);// finding for
																// q0,q1..
						for (int k = 0; k < queryDTOListInit.size(); ++k) {// comparing
																			// to
																			// get
																			// the
																			// queryDTO
																			// for
																			// q0,q1..
							if (queryDTOListInit.get(k).getqID().equals(qid)) {
								if (queryDTOListInit.get(k).getDataSources() != null
										&& queryDTOListInit.get(i)
												.getDataSources().length > 0) {
									// dataSourcesFinal.queryDTOList.get(k).getDataSources();
									dataSourcesFinal = (String[]) ArrayUtils
											.addAll(dataSourcesFinal,
													queryDTOListInit.get(k)
															.getDataSources());
									intmQryIsExist = 1;
								}
							}
						}
					}
				}

			}
			if (intmQryIsExist == 1) {
				queryDTOListInit.get(i).setDataSources(dataSourcesFinal); // replacing
																			// q0,q1
																			// with
																			// datasources
			}

		}
		return queryDTOListInit;
	}

}
