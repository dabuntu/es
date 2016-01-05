package com.eventshop.eventshoplinux.servlets;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import akka.camel.CamelExtension;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.akka.dataSource.DataSourceSchedular;
import com.eventshop.eventshoplinux.akka.dataSource.query.QueryActor;
import com.eventshop.eventshoplinux.akka.query.MainQueryActor;
import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.CommonUtil;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.UNDERSCORE;
import static com.eventshop.eventshoplinux.constant.Constant.json;


public class RegisterServlet extends HttpServlet {
	/**
	 * Automatically generated serialVersionUID
	 */
	private static final long serialVersionUID = 1L;
	private final static Logger log = LoggerFactory.getLogger(RegisterServlet.class);
	String context = "";
	String tempDir = "";
	CamelContext camelContext;
	ActorSystem actorSystem;
	// All the registered data sources in EventShop
	HashMap<String, DataSource> sources;
	// All the data processes
	HashMap<String, QueryProcess> queryProcesses;
	HashMap<String, CamelQueryProcess> camelQueryProcesses;
	QueryJSONParser parser;
	// Query query;
	ActorSelection dataSourceSchedularActor;
	ActorSelection queryActor;
	ActorSelection mainQueryActor;



	public RegisterServlet() {
		log.info("Inside RegisterServlet()");
		sources = new HashMap<String, DataSource>();
		camelQueryProcesses = new HashMap<String, CamelQueryProcess>();
		preRegisterDataSourcesQueries();
		// no json parser required so removed the line
		this.parser = new QueryJSONParser(this); // initialized the parser --
		// sanjukta 06-08-2014
	}

	@Override
	public void init() {
		log.info("registerservlet init is called");
		context = Config.getProperty("context");
		tempDir = Config.getProperty("tempDir");
		preRegisterDataSourcesQueries();
		DataCache.updateRegisteredSources();
	}

	@Override
	public void init(final ServletConfig config) {
		log.info("registerservlet final init is called");
		context = Config.getProperty("context");
		log.info(context);
		tempDir = Config.getProperty("tempDir");
//		AkkaActorSystem
		actorSystem = (ActorSystem) config.getServletContext().getAttribute("AkkaActorSystem");
		dataSourceSchedularActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/dataSourceSchedularActor");
		queryActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/queryActor");
		mainQueryActor = actorSystem.actorSelection("akka://eventshop-actorSystem/user/mainQueryActor");

		camelContext = CamelExtension.get(actorSystem).context();
		preRegisterDataSourcesQueries();
		DataCache.updateRegisteredSources();

	}

	// This method is most likely for testing and debugging purpose
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		if (request.getParameter("type") != null
				&& request.getParameter("type").equalsIgnoreCase("startds")) {
			String dsID = request.getParameter("dsID");
			log.info("Going to start data source actor {}", dsID);
			dataSourceSchedularActor.tell(new DataSourceSchedular.StartDataSource(Integer.parseInt(dsID)), null);
			Result dsource = new Result();
			dsource.resId = dsID;
			dsource.output = "";
			new ResponseJSON(response, ResponseJSON.ResponseStatus.SUCCESS, "");
		} else if (request.getParameter("type") != null
				&& request.getParameter("type").equalsIgnoreCase("stopds")) {
			String dsID = request.getParameter("dsID");
			String output = "";
			log.info("in GET stopds: trying to stop ds: " + dsID);
			dataSourceSchedularActor.tell(new DataSourceSchedular.StopDataSource(Integer.parseInt(dsID)), null);
			Result dsource = new Result();
			dsource.resId = dsID;
			dsource.output = output;
			new ResponseJSON(response, ResponseJSON.ResponseStatus.SUCCESS,
					output);
		} else if (request.getParameter("type") != null
				&& request.getParameter("type").equalsIgnoreCase("startq")) {//Start query doGet
			int qID = Integer.parseInt(request.getParameter("qID"));
			//queryActor.tell(new QueryActor.EnableAndRunQuery(qID), null);
			mainQueryActor.tell(new MainQueryActor.EnableAndRunQuery(qID),null);

		} else if (request.getParameter("type") != null
				&& request.getParameter("type").equalsIgnoreCase("stopq")) {
			log.info("Stopping ds in doGet");

			String qId = request.getParameter("qID");
			log.info("in stopq: " + qId);
			if (qId == null || qId.isEmpty()) {
				log.info("invalid qID " + qId);
				Result res = new Result();
				res.resId = qId;
				res.comment = "invalid qID: " + qId;
				res.setError();
				new ResponseJSON(response, res);
				return;
			} else {
				try {
//					boolean done = this.stopQueryProcess(qId);
					//Delete file
//					String dirToSearchAndDelete = Config.getProperty("context") + "temp/queries/";
//					File file = new File(dirToSearchAndDelete);
//					if (file.isDirectory()) {
//						String[] subNote = file.list();
//						for (String filename : subNote) {
//							if (filename.startsWith("Q" + qId)) {
//								log.info("Deleting file " + filename);
//								new File(dirToSearchAndDelete + filename).delete();
//							}
//						}
//					}
//					file.delete();
					Result query = new Result();
					query.resId = qId;
//					if (done) {
//						query.comment = camelQueryProcesses.get(qId).isRunning()
//								+ "";
//						query.setSuccess();
//					} else {
//						query.setError();
//					}

					queryActor.tell(new QueryActor.DisableQuery(Integer.valueOf(qId)), null);
					new ResponseJSON(response, query);
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean getqStatus(String id) {
		boolean status = false;
		if (camelQueryProcesses.get(id).isRunning()) {
			status = true;
			return status;
		}
		return status;
	}

	public boolean getdsStatus(String id) {
		boolean status = false;
		DataSource dataSrc = sources.get(id);
		if (dataSrc.getControl() == 1) {
			status = true;
			return status;
		}
		return status;
	}

	public boolean stopQueryProcess(String qId) {
		boolean done = false;
		// stop the queryprocess thread
		if (camelQueryProcesses.containsKey(qId)) {
			log.info("try to stop qId: " + qId);
			camelQueryProcesses.get(qId).stop();
			while (camelQueryProcesses.get(qId).isRunning()) {
				try {
					Thread.sleep(500);
					log.info("sleep: waiting to stop");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			log.info("done stop qId: " + qId);
			done = true;
		}
		// remove exefile
		String exePath = context + "proc/Debug/EmageOperators_Q" + qId; // query
		// execution
		// path
		File exeFile = new File(exePath);
		if (exeFile.exists()) {
			exeFile.delete();

		}
		String jsonPath = context + "temp/queries/" + "Q" + qId + ".json";
		File jsonFile = new File(jsonPath);

		if (jsonFile.exists()) {
			jsonFile.delete();

		}
		return done;
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		response.setContentType("application/json");
		try {
			log.info("in servlet");
			if (request.getParameter("type") == null) {
				log.info("invalid action each request require `type` parameter");
				new ResponseJSON(response, ResponseJSON.ResponseStatus.ERROR,
						"invalid parameters, each request required `type` parameter");
				return;
			}

			if (request.getParameter("type").equals("readquery")) {
				String qid = request.getParameter("qid");

				OutputStream out = response.getOutputStream();
				BufferedReader reader = new BufferedReader(new FileReader(
						context + "proc/src/Q" + qid + ".cc"));
				String line;
				while ((line = reader.readLine()) != null) {
					line += "\n";
					out.write(line.getBytes());
				}
				reader.close();
				out.close();
			} else if (request.getParameter("type").equals("alert")) {
				log.info("in alert part");

				String userDS = request.getParameter("userDS").split(":")[0];
				String srcQuery = request.getParameter("sourceQuery")
						.split(":")[0];
				String nearestDS = request.getParameter("nearestDs").split(":")[0];

				String[] qRanges = request.getParameter("qRange").split(":");
				double minValProb = Double.valueOf(qRanges[0]);
				double maxValProb = Double.valueOf(qRanges[1]);

				String[] aRanges = request.getParameter("aRange").split(":");
				double minValSol = Double.valueOf(aRanges[0]);
				double maxValSol = Double.valueOf(aRanges[1]);

				String msg = request.getParameter("msg");


				byte[] output = "Sent tweets".toString().getBytes();
				OutputStream out = response.getOutputStream();
				out.write(output);
				out.close();
			}// Read the query file
			else if (request.getParameter("type").equals("readquery")) {
				String qid = request.getParameter("qid");

				OutputStream out = response.getOutputStream();
				BufferedReader reader = new BufferedReader(new FileReader(
						context + "proc/src/q" + qid + ".cc"));
				String line;
				while ((line = reader.readLine()) != null) {
					line += "\n";
					out.write(line.getBytes());
				}
				reader.close();
				out.close();
			} else {
				doGet(request, response);
			}

		} catch (Exception p) {
			log.error(p.getMessage());
			new ResponseJSON(response, ResponseJSON.ResponseStatus.ERROR,
					p.getMessage());
			return;
		}
	}

	private void preRegisterDataSourcesQueries() {
		DataSourceManagementDAO dataSrcDAO = new DataSourceManagementDAO();
		List<DataSource> tempArry = dataSrcDAO.getDataSrcList();
		for (int i = 0; i < tempArry.size(); i++) {
			sources.put(tempArry.get(i).srcID,
					tempArry.get(i));
		}
	}

	@Override
	public void destroy() {
		log.info("destroy");
		// read through tempDir and move them to ds folder inside tempDir
		File tempDirectory = new File(tempDir);
		File[] files = tempDirectory.listFiles();
		long now = System.currentTimeMillis();
		for (File file : files) {
			if (!file.isDirectory()) {
				String oriName = file.getPath();
				String newName = tempDir + "/ds/" + file.getName() + UNDERSCORE
						+ now;
				if (CommonUtil.RenameFile(oriName, newName))
					log.info("success! rename " + oriName + " to " + newName);
				else
					log.info("failed! rename " + oriName + " to " + newName);
			}
		}
		// read though tomcat7 temp folder and remove query execution files
		tempDirectory = new File(context + "/proc/Debug/");
		files = tempDirectory.listFiles();
		for (File file : files) {
			if (!file.isDirectory()) {
				String oriName = file.getPath();
				if (oriName.contains("EmageOperators")) {
					boolean deleted = file.delete();
					if (deleted)
						log.info("successfully deleted exe file! " + oriName);
					else
						log.info("failed to delete exe file! " + oriName);
				}
			}
		}
		// suppose to delete all result files both dsEmage and queryResult
		// To do!
	}
	private void startCamelQuerydoGet(HttpServletResponse response, String qId) {
		boolean running = true;
		CamelQueryProcess queryProcess = new CamelQueryProcess(qId, camelContext, running);
		new Thread(queryProcess).start();
		camelQueryProcesses.put(qId, queryProcess);
	}

	private void startQdoGet(HttpServletResponse response, String qId) {
//		deleteExeFileIfExists(qId);
		log.info("in startq: " + qId);
		if (qId == null || qId.isEmpty()) {
			log.info("invalid qID " + qId);
			Result res = new Result();
			res.resId = qId;
			res.comment = "invalid qID: " + qId;
			res.setError();
			new ResponseJSON(response, res);
			return;
		} else {
			try {
				if (camelQueryProcesses.containsKey(qId)
						&& camelQueryProcesses.get(qId).isRunning()) {
					this.stopQueryProcess(qId);
				}

				startCamelQuerydoGet(response, qId);

				String filepath = context + Constant.RESULT_Q + "Q" + qId
						+ json; // query output path
				File resultFile = new File(filepath);

				Result query = new Result();
				query.resId = qId;
//                query.comment = queryProcesses.get(qId).isRunning() + "";
				query.setSuccess();
				byte[] output;
				if (resultFile.exists()) {
					output = org.apache.commons.io.FileUtils
							.readFileToByteArray(resultFile);
					query.output = new String(output);
				} else {
					query.output = "";
				}
				new ResponseJSON(response, query);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}



}
