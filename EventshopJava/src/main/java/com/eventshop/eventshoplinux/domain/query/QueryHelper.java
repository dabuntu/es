package com.eventshop.eventshoplinux.domain.query;

import com.eventshop.eventshoplinux.DAO.admin.AdminManagementDAO;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

import static com.eventshop.eventshoplinux.constant.Constant.*;

//import net.sf.json.JSONArray;

public class QueryHelper {

	public ArrayList<QueryRunnable> queries = new ArrayList<QueryRunnable>();
	protected Log log = LogFactory.getLog(this.getClass().getName());
	// ArrayList<DataSource> queriedSources = new ArrayList<DataSource>();
	// ArrayList<String> queryCC = new ArrayList<String>();
	ArrayList<DataSource> sources = new ArrayList<DataSource>();
	QueryRunnable newQuery;
	int srcSubscript = 0;
	int maskID = 0;
	int fcondID = 0;
	int grpCritID = 0;
	int spPatternID = 0;
	int tpPatternID = 0;
	int aggID = 0;
	String context = "";
	String queryJSONText = "";
	String tempDir = Config.getProperty(TEMPDIR);

	// FrameParameters finalResolution = null;
	// String opName;
	// String queryID;
	// String queryType;

	public String queryProcess(List<QueryDTO> queryDTOList, String statFLag) {

		String output = "";

		// Registering Query-Needs to implement

		/*
		 * QueryListDAO queryListDAO = null; for (int i = 0; i <
		 * queryDTOList.size(); ++i) { queryListDAO = new QueryListDAO();
		 * boolean idExist =
		 * queryListDAO.chkQryID(Integer.parseInt(queryDTOList.
		 * get(i).getqID())); if (!idExist) { // new query gets added to DB
		 * queryListDAO.registerQuery(queryDTOList.get(i)); } }
		 */

		// Start the selected queries
		if (statFLag.equals(RUN)) {
			log.info("in qidstart");
			// String qids = request.getParameter("json");
			QueryRunnable query = null;
			for (int i = 0; i < queryDTOList.size(); ++i) {
				query = parseQuery(queryDTOList.get(i));
				queries.add(query);

			}
			// Start every selected process
			for (int i = 0; i < queryDTOList.size(); ++i) {
				// int index = Integer.parseInt(queryDTOList.get(i).getqID());
				new Thread(queries.get(i)).start();
				output = (query.contextPath + query.outputName + ".file");

			}

			for (int i = 0; i < queryDTOList.size(); ++i) {
				// int index = Integer.parseInt(queryDTOList.get(i).getqID());
				while (!queries.get(i).isRunning)
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						log.error(e.getMessage());
					}

			}

			output = (query.contextPath + query.outputName + ".file"); // throws
																		// null
																		// pointer
																		// for
																		// now
																		// --
																		// sanjukta

			/*
			 * byte[] output = "Selected Queries Started!".getBytes();
			 * OutputStream out = response.getOutputStream(); out.write(output);
			 * out.close();
			 */
		}

		// Stop the selected queries
		if (statFLag.equals(STOP)) {
			log.info("in qidstop");
			// String qids = request.getParameter("json");
			for (int i = 0; i < queryDTOList.size(); ++i) {
				QueryRunnable query = parseQuery(queryDTOList.get(i));
				queries.add(query);
			}
			/*
			 * String qids = queryJSONText; JSONObject qidObject =
			 * JSONObject.fromObject(qids); JSONArray qidArray =
			 * qidObject.getJSONArray("registeredIDs");
			 */

			// Stop every selected process
			for (int i = 0; i < queryDTOList.size(); ++i) {
				log.info("stop query " + i);
				// int index = Integer.parseInt(queryDTOList.get(i).getqID());
				// log.info(index);
				queries.get(i).stop();
			}

			for (int i = 0; i < queryDTOList.size(); ++i) {
				// int index = Integer.parseInt(queryDTOList.get(i).getqID());
				while (queries.get(i).isRunning)
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
			}

			output = "Selected Queries Stopped!";

		}

		// Add a new query
		if (statFLag.equals(ADD)) {
			log.info("in query");
			// String queryJSONText = request.getParameter("json");
			for (int i = 0; i < queryDTOList.size(); ++i) {
				QueryRunnable query = parseQuery(queryDTOList.get(i));
				new Thread(query).start();
				queries.add(query);
				// TODO: provide output for numeric output
				while (query.outputName == null)
					try {
						Thread.sleep(500);
					} catch (Exception e) {
						log.error(e.getMessage());
					}

				output = (query.outputName + ".json"); // path to be returned
				// back for .file

			}
		}
		return output;
	}

	/*
	 * public DataSource parseDataSource(String srcText) { srcText =
	 * srcText.replace("\\", "\\\\"); JSONObject outerObject =
	 * JSONObject.fromObject(srcText); JSONObject srcObject =
	 * outerObject.getJSONObject("source"); JSONObject visualObject =
	 * srcObject.getJSONObject("visualParam"); JSONObject initParamObject =
	 * srcObject.getJSONObject("initRes"); JSONObject finalParamObject =
	 * srcObject.getJSONObject("finalRes");
	 * 
	 * DataSource dataSrc = new DataSource(); dataSrc.srcID =
	 * outerObject.getLong("srcID"); dataSrc.srcTheme =
	 * srcObject.getString("theme"); dataSrc.srcName =
	 * srcObject.getString("name"); dataSrc.url = srcObject.getString("url");
	 * String format = srcObject.getString("format");
	 * if(format.equals("stream")) dataSrc.srcFormat = DataFormat.stream; else
	 * if(format.equals("visual")) dataSrc.srcFormat = DataFormat.visual; else
	 * if(format.equals("file")) dataSrc.srcFormat = DataFormat.file;
	 * 
	 * if(dataSrc.srcFormat == DataFormat.stream) { dataSrc.supportedWrapper =
	 * srcObject.getString("supportedWrapper"); try { dataSrc.bagOfWords =
	 * (ArrayList<String>)
	 * JSONArray.toCollection(srcObject.getJSONArray("bagOfWords"),
	 * Class.forName("java.util.ArrayList")); } catch (ClassNotFoundException e)
	 * { log.error(e.getMessage()); } }
	 * 
	 * if(!visualObject.isNullObject()) { dataSrc.createVisualObject(); String
	 * tranPath = visualObject.getString("tranMatPath");
	 * dataSrc.visualParam.tranMatPath = (tranPath == null? tempDir +
	 * "tranMat.txt" : tranPath);
	 * 
	 * String colorPath = visualObject.getString("colorMatPath");
	 * dataSrc.visualParam.colorMatPath = (colorPath == null? tempDir +
	 * "colorMat.txt" : colorPath);
	 * 
	 * String maskPath = visualObject.getString("maskPath"); // note: maskpath
	 * is optional, so it can be null if(maskPath == null)
	 * dataSrc.visualParam.maskPath = tempDir + "mask.png"; else
	 * if(maskPath.equalsIgnoreCase("")) dataSrc.visualParam.maskPath = null;
	 * else dataSrc.visualParam.maskPath = maskPath;
	 * 
	 * 
	 * String ignoreNumber = visualObject.getString("ignoreSinceNumber");
	 * 
	 * dataSrc.visualParam.ignoreSinceNumber = ((ignoreNumber == null ||
	 * ignoreNumber.equalsIgnoreCase(""))? -1:
	 * visualObject.getInt("ignoreSinceNumber"));
	 * 
	 * log.info("maskPath |" + maskPath + "|" + ignoreNumber +
	 * dataSrc.visualParam.maskPath);
	 * 
	 * }
	 * 
	 * if(!initParamObject.isNullObject()) { dataSrc.createInitParam();
	 * dataSrc.initParam.timeWindow =
	 * initParamObject.getLong("timeWindow")*1000;
	 * dataSrc.initParam.syncAtMilSec =
	 * initParamObject.getLong("syncAtMilSec")*1000; dataSrc.initParam.latUnit =
	 * initParamObject.getDouble("latUnit"); dataSrc.initParam.longUnit =
	 * initParamObject.getDouble("longUnit"); dataSrc.initParam.swLat =
	 * initParamObject.getDouble("swLat"); dataSrc.initParam.swLong =
	 * initParamObject.getDouble("swLong"); dataSrc.initParam.neLat =
	 * initParamObject.getDouble("neLat"); dataSrc.initParam.neLong =
	 * initParamObject.getDouble("neLong"); dataSrc.initParam.numOfRows = (int)
	 * Math.abs(Math.ceil((dataSrc.initParam.neLat - dataSrc.initParam.swLat) /
	 * dataSrc.initParam.latUnit)); dataSrc.initParam.numOfColumns = (int)
	 * Math.abs(Math.ceil((dataSrc.initParam.neLong - dataSrc.initParam.swLong)
	 * / dataSrc.initParam.longUnit)); }
	 * 
	 * if(!finalParamObject.isNullObject()) { dataSrc.createFinalParam();
	 * dataSrc.finalParam.timeWindow =
	 * finalParamObject.getLong("timeWindow")*1000;
	 * dataSrc.finalParam.syncAtMilSec =
	 * finalParamObject.getLong("syncAtMilSec")*1000; dataSrc.finalParam.latUnit
	 * = finalParamObject.getDouble("latUnit"); dataSrc.finalParam.longUnit =
	 * finalParamObject.getDouble("longUnit"); dataSrc.finalParam.swLat =
	 * finalParamObject.getDouble("swLat"); dataSrc.finalParam.swLong =
	 * finalParamObject.getDouble("swLong"); dataSrc.finalParam.neLat =
	 * finalParamObject.getDouble("neLat"); dataSrc.finalParam.neLong =
	 * finalParamObject.getDouble("neLong"); dataSrc.finalParam.numOfRows =
	 * (int) Math.abs(Math.ceil((dataSrc.finalParam.neLat -
	 * dataSrc.finalParam.swLat) / dataSrc.finalParam.latUnit));
	 * dataSrc.finalParam.numOfColumns = (int)
	 * Math.abs(Math.ceil((dataSrc.finalParam.neLong -
	 * dataSrc.finalParam.swLong) / dataSrc.finalParam.longUnit)); }
	 * 
	 * return dataSrc; }
	 */

	public FrameParameters getFinalParamFromSrc(int qryId) {

		/*
		 * if(index < sources.size()) return sources.get(index).finalParam;
		 */
		QueryListDAO qryDAO = new QueryListDAO();
		FrameParameters fpDB = qryDAO.getFrameParameterQry(qryId);
		if (fpDB != null) {
			return fpDB;
		} else {
			// Create FrameParameters
			long timeWindow = 1000 * 10;
			long mSecsOffset = 1000;

			// Group 1
			double latUnit1 = 0.5;
			double longUnit1 = 0.5;
			double swLat1 = 24;
			double swLong1 = -125;
			double neLat1 = 50;
			double neLong1 = -66;

			FrameParameters fp = new FrameParameters(timeWindow, mSecsOffset,
					latUnit1, longUnit1, swLat1, swLong1, neLat1, neLong1);
			return fp;
		}
	}

	public QueryRunnable parseQuery(QueryDTO qryDTO) {
		context = Config.getProperty(CONTEXT);
		newQuery = new QueryRunnable(context);
		// newQuery = new Query(servlet.context);
		// JSONArray queryArray = JSONArray.fromObject(queryJSONText);

		// JSONObject query = queryArray.getJSONObject(i);
		String queryType = qryDTO.getPatternType();

		String cc = null;
		if (queryType.indexOf(FILTER) != -1)
			cc = parseFilter(qryDTO);
		else if (queryType.indexOf(GROUPING) != -1)
			cc = parseGrouping(qryDTO);
		else if (queryType.indexOf(AGGREGATION) != -1)
			cc = parseAggregation(qryDTO);
		else if (queryType.indexOf(SPCHAR) != -1)
			cc = parseSpatialChar(qryDTO);
		else if (queryType.indexOf(SPMATCHING) != -1)
			cc = parseSpatialMatching(qryDTO);
		else if (queryType.indexOf(TPCHAR) != -1)
			cc = parseTemporalChar(qryDTO);
		else if (queryType.indexOf(TPMATCHING) != -1)
			cc = parseTemporalMatching(qryDTO);
		else
			System.err.println("No such operators!");

		// push the cc string into query list of query object
		newQuery.queryCC.add(cc);

		return newQuery;
	}

	private String parseFilter(QueryDTO qryDTO) {

		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		// String opVar = "q" + qryDTO.getqID();
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(FILTER);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();

		// if 1 datasource add that otherwise look for the array of datasources
		// and add them all in a loop
		// newQuery.queriedSources.add(sources.get(0));
		// newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
		/*
		 * if(dataSrc.indexOf(DSR_DS) != -1) { // add the used srcID into
		 * newQuery.srcIDs // add final resolution int srcID =
		 * Integer.parseInt(dataSrc.substring(2)); boolean srcFound = false;
		 * for(int i = 0; i < newQuery.queriedSources.size(); ++i) { // If the
		 * data source has been added once, then ignore it if(srcID ==
		 * newQuery.queriedSources.get(i).srcID) { srcFound = true; // Change
		 * the data Src to be the dsID_subscript dataSrc = dataSrc + "_" +
		 * srcSubscript; srcSubscript++;
		 * 
		 * // update the srcVarName DataSource src = new
		 * DataSource(newQuery.queriedSources.get(i)); src.srcVarName = dataSrc;
		 * newQuery.queriedSources.add(src); break; } } if(!srcFound) {
		 */
		// newQuery.queriedSources.add(sources.get(srcID));
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}

		// get the mask method
		int lowX = 0, lowY = 0, upX = 0, upY = 0;
		String maskMethod = qryDTO.getMaskMethod();
		if (maskMethod.equals(MAP) || maskMethod.equals(TEXTUAL)) {
			// double[] coords = (double[])
			// JSONArray.toArray(content.getJSONArray("coords"), double.class);
			String temp[] = qryDTO.getCoordrs();
			double[] coords = new double[qryDTO.getCoordrs().length];
			for (int i = 0; i < coords.length; i++) {
				coords[i] = Double.parseDouble(temp[i]);
			}

			lowX = (int) Math.abs(Math
					.ceil((coords[1] - newQuery.finalResolution.swLong)
							/ newQuery.finalResolution.longUnit));

			upX = (int) Math.abs(Math
					.ceil((coords[3] - newQuery.finalResolution.swLong)
							/ newQuery.finalResolution.longUnit));

			upY = newQuery.finalResolution.numOfRows
					- (int) Math.abs(Math
							.ceil((coords[0] - newQuery.finalResolution.swLat)
									/ newQuery.finalResolution.latUnit));

			lowY = newQuery.finalResolution.numOfRows
					- (int) Math.abs(Math
							.ceil((coords[2] - newQuery.finalResolution.swLat)
									/ newQuery.finalResolution.latUnit));

			if (lowX == upX)
				lowX--;
			if (lowY == upY)
				lowY--;
		}

		// get the value range
		String[] valRange = qryDTO.getValRange();
		String valMin = valRange[0];
		String valMax = valRange[1];

		// get the time range
		String[] timeRange = qryDTO.getTimeRange();
		String tMin = timeRange[0];
		String tMax = timeRange[1];

		if (tMax.equals("-999")) {
			long min = System.currentTimeMillis() - Long.parseLong(tMin) * 1000;
			tMin = String.valueOf(min);
			tMax = String.valueOf(Long.MAX_VALUE);
		}

		// get the norm values range
		String[] normRange = qryDTO.getNormVals();
		String nMin = normRange[0];
		String nMax = normRange[1];
		String nMode = qryDTO.getNormMode();

		// generate the cc string
		String filterCC = "";
		// generate the mask
		String maskVar = "";
		if (qryDTO.getMaskMethod().equals(MAP)
				|| qryDTO.getMaskMethod().equals(TEXTUAL)) {
			maskVar = "mask" + maskID;
			filterCC = "\tint rows" + maskID + " = "
					+ newQuery.finalResolution.numOfRows + ";\n";
			filterCC += "\tint cols" + maskID + " = "
					+ newQuery.finalResolution.numOfColumns + ";\n";
			filterCC += ("\tMat " + maskVar + "(rows" + maskID + " , cols"
					+ maskID + " , CV_8U);\n");
			filterCC += "\tfor(int i = 0; i < rows" + maskID + "; ++i)\n";
			filterCC += "\t{\n";
			filterCC += "\t\tfor(int j = 0; j < cols" + maskID + "; ++j)\n";
			filterCC += "\t\t{\n";
			filterCC += ("\t\t\tif(j <= " + upX + " && j> " + lowX + " && i<="
					+ upY + " && i> " + lowY + ")\n");
			filterCC += ("\t\t\t\t" + maskVar + ".at<unsigned char>(i, j) = 255;\n");
			filterCC += "\t\t\telse\n";
			filterCC += ("\t\t\t\t" + maskVar + ".at<unsigned char>(i, j) = 0;\n");
			filterCC += "\t\t}\n";
			filterCC += "\t}\n";
			maskID++;
		} else if (maskMethod.equals(MATRIX)) {
			maskVar = "\"" + qryDTO.getFilePath().replace("\\", "\\\\") + "\"";
		}

		// generate the filter condition
		String fcondVar = "fcond" + fcondID;
		fcondID++;
		filterCC += ("\tFilterCondition " + fcondVar + "(" + maskVar + ", LT, "
				+ valMin + ", " + valMax + ", " + tMin + ", " + tMax + ", "
				+ nMode + ", " + nMin + ", " + nMax + ");\n");
		// generate the filter operator
		filterCC += ("\tFilter " + opVar + "(" + dataSrc + ", " + fcondVar + ");\n\n");

		return filterCC;
	}

	private String parseGrouping(QueryDTO qryDTO) {
		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(GROUPING);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();
		/*
		 * if(dataSrc.indexOf(DSR_DS) != -1) { // add the used srcID into
		 * newQuery.srcIDs // add final resolution int srcID =
		 * Integer.parseInt(dataSrc.substring(2)); boolean srcFound = false;
		 * for(int i = 0; i < newQuery.queriedSources.size(); ++i) { // If the
		 * data source has been added once, then ignore it if(srcID ==
		 * newQuery.queriedSources.get(i).srcID) { srcFound = true; // Change
		 * the data Src to be the dsID_subscript dataSrc = dataSrc + "_" +
		 * srcSubscript; srcSubscript++;
		 * 
		 * // update the srcVarName DataSource src = new
		 * DataSource(newQuery.queriedSources.get(i)); src.srcVarName = dataSrc;
		 * newQuery.queriedSources.add(src); break; } } if(!srcFound) {
		 */
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}

		String split = qryDTO.getSplit().toLowerCase();
		String doColoring = qryDTO.getDoColoring().toLowerCase();
		String colorVector = null;
		if (doColoring.equals("true")) {
			String[] colors = qryDTO.getColorCodes();

			colorVector = "\tvector<GroupingColors> grpColors;\n";
			for (int i = 0; i < colors.length; i++) {
				colorVector += ("\tgrpColors.push_back("
						+ colors[i].trim().toLowerCase() + ");\n");
			}
		}

		String groupCC = null;
		String method = qryDTO.getMethod();
		if (method.equals("KMeans")) {
			String numGroups = qryDTO.getNumGroup();

			groupCC = ("\n\tGrouping " + opVar + "(" + dataSrc);
			groupCC += (", " + numGroups + ", KMEANS, ");
			groupCC += (split + ", " + doColoring);
		} else if (method.equals("Thresholds")) {
			groupCC = ("\n\tGroupingCriteria gcrit" + grpCritID + " = GroupingCriteria();\n");
			groupCC += ("\tgcrit" + grpCritID + ".critGrpType = gt_absolute;\n");
			groupCC += ("\tvector<double> thresh" + grpCritID + ";\n");

			String[] thresholds = qryDTO.getThresholds();
			for (int i = 0; i < thresholds.length; i++) {
				groupCC += ("\tthresh" + grpCritID + ".push_back("
						+ thresholds[i].trim() + ");\n");
			}

			groupCC += ("\tgcrit" + grpCritID + ".setThresholds(thresh"
					+ grpCritID + ");\n");
			groupCC += ("\tGrouping " + opVar + "(" + dataSrc);
			groupCC += (", gcrit" + grpCritID + ", ");
			groupCC += (split + ", " + doColoring);
			grpCritID++;
		}

		if (colorVector != null)
			groupCC = colorVector + groupCC + (", grpColors);\n");
		else
			groupCC += (");\n");

		return groupCC;
	}

	private String parseAggregation(QueryDTO qryDTO) {
		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(AGGREGATION);

		// Get the source IDs
		String aggCC;

		aggCC = "\n\tvector<ProcEmageIterator*> eits" + aggID + ";\n";
		String[] srcArray = qryDTO.getDataSources();
		for (int i = 0; i < srcArray.length; i++) {
			String dataSrc = srcArray[i];
			if (dataSrc != null) {
				String dataSources[] = qryDTO.getDataSources();
				for (int j = 0; j < dataSources.length; j++) {
					dataSrc = dataSources[j];
					if (dataSrc.indexOf(DSR_DS) != -1) {
						int srcID = Integer.parseInt(dataSrc.substring(2));
						sources = getRegisteredDatasources(srcID);
						newQuery.queriedSources.add(sources.get(0));
						newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
					}
				}

			}
			/*
			 * { int srcID = Integer.parseInt(dataSrc.substring(2));
			 * 
			 * boolean srcFound = false; for(int j = 0; j <
			 * newQuery.queriedSources.size(); ++j) { // If the data source has
			 * been added once, then ignore it if(srcID ==
			 * newQuery.queriedSources.get(j).srcID) { srcFound = true; //
			 * Change the data Src to be the dsID_subscript dataSrc = dataSrc +
			 * "_" + srcSubscript; srcSubscript++;
			 * 
			 * // update the srcVarName DataSource src = new
			 * DataSource(newQuery.queriedSources.get(j)); src.srcVarName =
			 * dataSrc; newQuery.queriedSources.add(src); break; } }
			 * if(!srcFound) { sources = getRegisteredDatasources(srcID);
			 * newQuery.queriedSources.add(sources.get(0));
			 * //newQuery.queriedSources.add(sources.get(srcID));
			 * newQuery.setFinalResolution(getFinalParamFromSrc(srcID)); } }
			 */
			aggCC += ("\teits" + aggID + ".push_back(&" + dataSrc + ");\n");
		}

		String[] valueArray = qryDTO.getValRange();
		if (valueArray != null && valueArray.length > 0) {
			aggCC += ("\n\tvector<double> scalars" + aggID + ";\n");
			for (int i = 0; i < valueArray.length; ++i) {
				aggCC += ("\tscalars" + aggID + ".push_back(" + valueArray[i] + ");\n");
			}
			// get scalarFirst
			String scalarFirst = qryDTO.getScalarFirst();
			// get aggOperator
			String operation = qryDTO.getAggOperator().substring(3)
					.toUpperCase();
			// get normalization
			if (!qryDTO.getValueNorm().equals("valueNorm")
					|| qryDTO.getValueNorm().equals("false")) {
				aggCC += "\n\tAggregate " + opVar + "(eits" + aggID
						+ ", scalars" + aggID + ", Agg" + operation + ", "
						+ scalarFirst + ");\n\n";
			} else {
				String[] normArray = qryDTO.getNormedRange();
				String minValue = normArray[0];
				String maxValue = normArray[1];
				aggCC += "\n\tAggregate " + opVar + "(eits" + aggID
						+ ", scalars" + aggID + ", Agg" + operation + ", "
						+ scalarFirst + ", true, " + minValue + ", " + maxValue
						+ ");\n\n";
			}
		} else {
			// get aggOperator
			String operation = qryDTO.getAggOperator().substring(3)
					.toUpperCase();
			// get normalization
			if (!qryDTO.getValueNorm().equals("valueNorm")
					|| qryDTO.getValueNorm().equals("false")) {
				aggCC += "\n\tAggregate " + opVar + "(eits" + aggID + ", Agg"
						+ operation + ");\n\n";
			} else {
				String[] normArray = qryDTO.getNormedRange();
				String minValue = normArray[0];
				String maxValue = normArray[1];
				aggCC += "\n\tAggregate " + opVar + "(eits" + aggID + ", Agg"
						+ operation + ", true, " + minValue + ", " + maxValue
						+ ");\n\n";
			}
		}

		aggID++;

		log.info(aggCC);
		return aggCC;
	}

	private String parseSpatialChar(QueryDTO qryDTO) {
		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(SPCHAR);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}

		String spcharOp = qryDTO.getSpCharoperator();
		newQuery.setOpName(spcharOp);
		spcharOp = spcharOp.toUpperCase();

		String spcharCC;
		spcharCC = "\tSpatialChar " + opVar + "(" + dataSrc + ", " + spcharOp
				+ ");\n\n";
		return spcharCC;
	}

	private String parseSpatialMatching(QueryDTO qryDTO) {
		// JSONObject content = qryDTO.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(SPMATCHING);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}
		/*
		 * if(dataSrc.indexOf(DSR_DS) != -1) { // add the used srcID into
		 * newQuery.srcIDs // add final resolution int srcID =
		 * Integer.parseInt(dataSrc.substring(2)); boolean srcFound = false;
		 * for(int i = 0; i < newQuery.queriedSources.size(); ++i) { // If the
		 * data source has been added once, then ignore it if(srcID ==
		 * newQuery.queriedSources.get(i).srcID) { srcFound = true; // Change
		 * the data Src to be the dsID_subscript dataSrc = dataSrc + "_" +
		 * srcSubscript; srcSubscript++;
		 * 
		 * // update the srcVarName DataSource src = new
		 * DataSource(newQuery.queriedSources.get(i)); src.srcVarName = dataSrc;
		 * newQuery.queriedSources.add(src); break; } } if(!srcFound) { sources
		 * = getRegisteredDatasources(srcID);
		 * newQuery.queriedSources.add(sources.get(0));
		 * //newQuery.queriedSources.add(sources.get(srcID));
		 * newQuery.setFinalResolution(getFinalParamFromSrc(srcID)); } }
		 */

		String sizeNorm = qryDTO.getSizeNorm().toLowerCase();
		String valueNorm = qryDTO.getValueNorm().toLowerCase();
		String src = qryDTO.getPatternSrc(); // patternsrc int to do

		String spMatchingCC = null;
		// Get input from file
		if (src.equalsIgnoreCase("file")) {
			String filePath = qryDTO.getFilePath().replace("\\", "\\\\");
			spMatchingCC = "\tSpatialPatternMatching " + opVar + "(" + dataSrc
					+ ", Input" + ", " + sizeNorm + ", " + valueNorm + ", \""
					+ filePath + "\");\n\n";
		}
		// Get input from generated patterns
		else if (src.equalsIgnoreCase("create")) {
			String pattern = null;
			String patternType;

			String numRows = qryDTO.getNumRows().trim();
			String numCols = qryDTO.getNumCols().trim();
			patternType = qryDTO.getParmType();// parmType getPatternType()

			// Generate the gaussian pattern code
			if (patternType.equals(GAUSSIAN)) {
				patternType = GAUSSIAN;
				SpatialMatchGaussianParam gaussParam = qryDTO.getGaussParam();
				String centerX = gaussParam.getCenterX();
				String centerY = gaussParam.getCenterY();
				String varX = gaussParam.getVarX();
				String varY = gaussParam.getVarY();
				String amplitude = gaussParam.getAmplitude();
				pattern = "\tGaussianPattern spPattern" + spPatternID + "("
						+ numRows + ", " + numCols + ", " + centerX + ", "
						+ centerY + ", " + varX + ", " + varY + ", "
						+ amplitude + ");\n";
			}
			// Generate the linear pattern code
			else if (patternType.equals("linear")) {
				patternType = "Linear2D";
				SpatialMatchLinearParam linearParam = qryDTO.getLinearParam();
				String startX = linearParam.getStartX();
				String startY = linearParam.getStartY();
				String startValue = linearParam.getStartValue();
				String dirGradient = linearParam.getDirGradient();
				String valGradient = linearParam.getValGradient();
				pattern = "\tLinearPattern spPattern" + spPatternID + "("
						+ numRows + ", " + numCols + ", " + startX + ", "
						+ startY + ", " + startValue + ", " + dirGradient
						+ ", " + valGradient + ");\n";

			}

			spMatchingCC = pattern;
			spMatchingCC += "\tSpatialPatternMatching " + opVar + "(" + dataSrc
					+ ", " + patternType + ", " + sizeNorm + ", " + valueNorm
					+ ", &spPattern" + spPatternID + ");\n\n";

			spPatternID++;
		}

		return spMatchingCC;
	}

	private String parseTemporalChar(QueryDTO qryDTO) {
		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(TPCHAR);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}
		/*
		 * if(dataSrc.indexOf(DSR_DS) != -1) { // add the used srcID into
		 * newQuery.srcIDs // add final resolution int srcID =
		 * Integer.parseInt(dataSrc.substring(2)); boolean srcFound = false;
		 * for(int i = 0; i < newQuery.queriedSources.size(); ++i) { // If the
		 * data source has been added once, then ignore it if(srcID ==
		 * newQuery.queriedSources.get(i).srcID) { srcFound = true; // Change
		 * the data Src to be the dsID_subscript dataSrc = dataSrc + "_" +
		 * srcSubscript; srcSubscript++;
		 * 
		 * // update the srcVarName DataSource src = new
		 * DataSource(newQuery.queriedSources.get(i)); src.srcVarName = dataSrc;
		 * newQuery.queriedSources.add(src); break; } } if(!srcFound) { sources
		 * = getRegisteredDatasources(srcID);
		 * newQuery.queriedSources.add(sources.get(0));
		 * //newQuery.queriedSources.add(sources.get(srcID));
		 * newQuery.setFinalResolution(getFinalParamFromSrc(srcID)); } }
		 */

		String tpcharOp = qryDTO.getTmplCharOperator();
		newQuery.setOpName(tpcharOp);
		tpcharOp = tpcharOp.toUpperCase();
		String window = qryDTO.getTimeWindow().trim();

		String tpcharCC;
		tpcharCC = "\tTemporalChar " + opVar + "(" + dataSrc + ", " + tpcharOp
				+ ", " + window + "*1000);\n\n";
		return tpcharCC;
	}

	private String parseTemporalMatching(QueryDTO qryDTO) {
		// JSONObject content = query.getJSONObject("query");
		// generate the operator variable
		String opVar = qryDTO.getqID();
		// Set the opVar to be query ID of this query
		newQuery.setQueryID(opVar);
		// Set the operator type
		newQuery.setQueryType(TPMATCHING);

		// get the source ID
		String dataSrc = qryDTO.getDataSrcID();
		if (dataSrc != null) {
			if (dataSrc.indexOf(DSR_DS) != -1) {
				int srcID = Integer.parseInt(dataSrc.substring(2));
				sources = getRegisteredDatasources(srcID);
				newQuery.queriedSources.add(sources.get(0));
				newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
			}
		} else {
			String dataSources[] = qryDTO.getDataSources();
			for (int i = 0; i < dataSources.length; i++) {
				dataSrc = dataSources[i];
				if (dataSrc.indexOf(DSR_DS) != -1) {
					int srcID = Integer.parseInt(dataSrc.substring(2));
					sources = getRegisteredDatasources(srcID);
					newQuery.queriedSources.add(sources.get(0));
					newQuery.setFinalResolution(getFinalParamFromSrc(srcID));
				}
			}
		}
		/*
		 * if(dataSrc.indexOf(DSR_DS) != -1) { // add the used srcID into
		 * newQuery.srcIDs // add final resolution int srcID =
		 * Integer.parseInt(dataSrc.substring(2)); boolean srcFound = false;
		 * for(int i = 0; i < newQuery.queriedSources.size(); ++i) { // If the
		 * data source has been added once, then ignore it if(srcID ==
		 * newQuery.queriedSources.get(i).srcID) { srcFound = true; // Change
		 * the data Src to be the dsID_subscript dataSrc = dataSrc + "_" +
		 * srcSubscript; srcSubscript++;
		 * 
		 * // update the srcVarName DataSource src = new
		 * DataSource(newQuery.queriedSources.get(i)); src.srcVarName = dataSrc;
		 * newQuery.queriedSources.add(src); break; } } if(!srcFound) { sources
		 * = getRegisteredDatasources(srcID);
		 * newQuery.queriedSources.add(sources.get(0));
		 * //newQuery.queriedSources.add(sources.get(srcID));
		 * newQuery.setFinalResolution(getFinalParamFromSrc(srcID)); } }
		 */

		String dataDuration = qryDTO.getDataDuration().trim();
		String durationNorm = qryDTO.getDurationNorm().toLowerCase();
		String valueNorm = qryDTO.getValueNorm().toLowerCase();
		String patternSrc = qryDTO.getPatternSrc();// patternSrc
																		// should
																		// be
																		// string
																		// to do

		String tpMatchingCC = null;
		String pattern = null;
		String samplingRate = qryDTO.getPatternSamplingRate().trim();
		String patternDuration = qryDTO.getPatternDuration().trim();
		String patternType = qryDTO.getParmType().trim().toLowerCase();// getPatternType()

		if (patternSrc.equals(FILE)) {
			String filePath = "\"" + qryDTO.getFilePath().replace("\\", "\\\\")
					+ "\"";
			pattern = "\tInputTemporalPatternTemplate tpPattern" + tpPatternID
					+ "(" + filePath + ");\n";
		} else if (patternSrc.equals("create")) {
			if (patternType.equals("linear")) {
				TemporalMatchLinearParam linear = qryDTO.getTemplinearParam();
				String slope = linear.getSlope();
				String intercept = linear.getyIntercept();

				pattern = "\tLinearTemporalPatternTemplate tpPattern"
						+ tpPatternID + "(" + slope + ", " + intercept + ", "
						+ samplingRate + "*1000, " + patternDuration
						+ "*1000);\n";
			} else if (patternType.equals("exponential")) {
				ExponentialParameter exp = qryDTO.getExpParam();
				String base = exp.getBase();
				String scale = exp.getScale();

				pattern = "\tExponentialTemporalPatternTemplate tpPattern"
						+ tpPatternID + "(" + base + ", " + scale + ", "
						+ samplingRate + "*1000, " + patternDuration
						+ "*1000);\n";
			} else if (patternType.equals("periodic")) {
				PeriodicParameter periodic = qryDTO.getPeriodicParam();
				String frequency = periodic.getFrequency();
				String amplitude = periodic.getAmplitude();
				String phaseDelay = periodic.getPhaseDelay();

				pattern = "\tPeriodicTemporalPatternTemplate tpPattern"
						+ tpPatternID + "(" + frequency + ", " + amplitude
						+ ", " + phaseDelay + ", " + samplingRate + "*1000, "
						+ patternDuration + "*1000);\n";
			}
		}

		tpMatchingCC = pattern;
		tpMatchingCC += "\tTemporalPatternMatching " + opVar + "(" + dataSrc
				+ ", tpo_" + patternType.toUpperCase() + ", " + valueNorm
				+ ", " + durationNorm + ", " + dataDuration
				+ "*1000, &tpPattern" + tpPatternID + ");\n\n";
		tpPatternID++;

		return tpMatchingCC;
	}

	ArrayList<DataSource> getRegisteredDatasources(int srcID) {
		AdminManagementDAO adminDAO = new AdminManagementDAO();
		List<String> dsIDList = new ArrayList<String>();
		dsIDList.add(Integer.toString(srcID));
		sources = (ArrayList<DataSource>) adminDAO.getDatasource(dsIDList);
		return sources;
	}

}
