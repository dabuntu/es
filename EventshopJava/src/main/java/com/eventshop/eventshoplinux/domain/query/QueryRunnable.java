package com.eventshop.eventshoplinux.domain.query;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;
import com.eventshop.eventshoplinux.util.commonUtil.Config;

import static com.eventshop.eventshoplinux.constant.Constant.*;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class QueryRunnable implements Runnable {
	protected Log log = LogFactory.getLog(this.getClass().getName());
	// An array list of data source id which is used in this query
	ArrayList<DataSource> queriedSources;
	// An array list of query strings of this query
	ArrayList<String> queryCC;

	// generated data processes and threads
	ArrayList<DataProcess> dataProcesses;
	// Operator process
	Process proc;

	// The final resolution of this query
	FrameParameters finalResolution = null;

	// The query ID variable of last query
	// over which to loop and get results
	String queryID;
	// The query type, e.g. filter, agg, spchar
	String queryType;
	// The operation name
	String opName;

	// Folder path to store emages

	String dataFolderPath;
	// C++ query file path
	String ccPath;
	// result output path
	String outputName = null;
	// context Path of the servlet
	String contextPath;

	boolean isRunning;

	int OS; // -1=not support, 0=unix or linux, 1=windows,

	public QueryRunnable(String context) {
		contextPath = context;
		if (contextPath == "")
			contextPath = Config.getProperty(CONTEXT);
		dataFolderPath = Config.getProperty(TEMPDIR);

		queriedSources = new ArrayList<DataSource>();
		queryCC = new ArrayList<String>();

		dataProcesses = new ArrayList<DataProcess>();
		OS = checkOS();
	}

	public int checkOS() {
		String currentOs = System.getProperty("os.name").toLowerCase();
		int os;
		if (currentOs.contains("nix") || currentOs.contains("nux")
				|| currentOs.contains("aix")) {
			os = 0;
		} else if (currentOs.contains("win")) {
			os = 1;
		} else {
			os = -1;
		}
		return os;
	}

	public void setFinalResolution(FrameParameters res) {
		finalResolution = res;
	}

	public void setQueryID(String qid) {
		queryID = qid;
	}

	public void setQueryType(String qtype) {
		queryType = qtype;
	}

	public void setOpName(String name) {
		opName = name;
	}

	public boolean isRunning() {
		return isRunning;
	}

	private void createCCProcessWindows() {
		// Create C++ code and process to start
		String cc = "#include \"spatial_analysis.h\"\n"
				+ "#include \"temporal_analysis.h\"\n\n"
				+ "#include <ctime>\n\n" + "using namespace cv;\n\n"
				+ "int main()\n" + "{\n";
		for (int i = 0; i < queriedSources.size(); ++i) {
			DataSource src = queriedSources.get(i);
			if (src != null) {
				String filename = "\"" + dataFolderPath + "ds" + src.srcID
						+ "_" + src.srcName + "\"";

				if (src.srcVarName == null)
					cc += ("\tEmageIngestor ds" + src.srcID + "(" + filename + ");\n");
				else
					cc += ("\tEmageIngestor " + src.srcVarName + "(" + filename + ");\n");
			}
		}

		for (int i = 0; i < queryCC.size(); ++i) {
			cc += queryCC.get(i);
		}

		cc += ("\twhile(true)\n" + "\t{\n" + "\t\twhile(" + queryID
				+ ".has_next())\n" + "\t\t{\n" + "\t\t\tEmage e = " + queryID + ".next();\n");

		// Decide how to show outputs
		outputName = queryID;// + "_" + queryType;
		cc += ("\t\t\tcreate_output(e, \"" + contextPath.replace("\\", "\\\\")
				+ Constant.RESULT_Q.replace("\\", "\\\\") + outputName + "\");\n");

		cc += ("\t\t}\n" + "\t\tSleep(100);\n" + "\t}\n" + "\treturn 0;\n"
				+ "}\n");
		ccPath = contextPath + queryID + ".cc"; // changed due to new proj
												// structure
		try {
			FileOutputStream ous = new FileOutputStream(ccPath);
			ous.write(cc.getBytes());
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}

	private void createCCProcessLinux() {
		// Create C++ code and process to start
		String cc = "#include \"spatial_analysis.h\"\n"
				+ "#include \"temporal_analysis.h\"\n\n"
				+ "#include <ctime>\n\n" + "using namespace cv;\n\n"
				+ "int main()\n" + "{\n";
		for (int i = 0; i < queriedSources.size(); ++i) {
			DataSource src = queriedSources.get(i);

			String filename = "\"" + dataFolderPath + "ds" + src.srcID + "_"
					+ src.srcName + "\"";

			if (src.srcVarName == null)
				cc += ("\tEmageIngestor ds" + src.srcID + "(" + filename + ");\n");
			else
				cc += ("\tEmageIngestor " + src.srcVarName + "(" + filename + ");\n");
		}

		for (int i = 0; i < queryCC.size(); ++i) {
			cc += queryCC.get(i);
		}

		cc += ("\twhile(true)\n" + "\t{\n" + "\t\twhile(" + queryID
				+ ".has_next())\n" + "\t\t{\n" + "\t\t\tEmage e = " + queryID + ".next();\n");

		// Decide how to show outputs
		outputName = queryID; // + "_" + queryType;
		cc += ("\t\t\tcreate_output(e, \"" + contextPath.replace("\\", "/")
				+ Constant.RESULT_Q + outputName + "\");\n");
		cc += ("\t\t}\n" + "\t\tsleep(100);\n" + // (CAP "S") Sleep(100) for
													// windows, but sleep(100)
													// for linux
				"\t}\n" + "\treturn 0;\n" + "}\n");
		ccPath = contextPath + "proc/src/" + queryID + ".cc";
		try {
			FileOutputStream ous = new FileOutputStream(ccPath);
			ous.write(cc.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void build() {
		// Move starting data source logic to servlet
		if (OS == 0)
			createCCProcessLinux();
		else if (OS == 1)
			createCCProcessWindows();
	}

	@Override
	public void run() {
		build();

		isRunning = true;

		// Start the Data Process
		for (int i = 0; i < dataProcesses.size(); i++) {
			new Thread(dataProcesses.get(i)).start();
		}

		// Start the C++ Operation
		String[] compile = null;
		String run = "";
		if (OS == 0) {
			String[] tcompile = { "sh", contextPath + "proc/Debug/compile.sh",
					queryID, "EmageOperators_" + queryID };
			compile = tcompile;
			run = contextPath + "proc/Debug/EmageOperators_" + queryID;
		} else if (OS == 1) {
			String[] tcompile = {
					"\"" + contextPath + "proc\\Debug\\compile.bat\"", queryID,
					"EmageOperators_" + queryID }; // changed due to new proj
													// structure
			compile = tcompile;
			run = "\"" + contextPath + "proc\\Debug\\EmageOperators_" + queryID
					+ ".exe\""; // changed due to new proj structure
		}

		try {
			if (compile != null && run != "") {
				// Compile the query
				proc = Runtime.getRuntime().exec(compile);
				InputStream inputStream = proc.getInputStream();
				InputStreamReader inputStreamReader = new InputStreamReader(
						inputStream);
				BufferedReader bufferedReader = new BufferedReader(
						inputStreamReader);

				String line;
				while ((line = bufferedReader.readLine()) != null) {
					log.info(line);
				}
				proc.destroy();
				proc.waitFor();
				log.info("Compiled!");

				// Run the query
				proc = Runtime.getRuntime().exec(run);
				inputStream = proc.getInputStream();
				inputStreamReader = new InputStreamReader(inputStream);
				bufferedReader = new BufferedReader(inputStreamReader);
				while (isRunning && (line = bufferedReader.readLine()) != null) {
					log.info(line);
				}
				log.info("Run!");
			}

		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
			stop();
		}
	}

	public void stop() {
		if (isRunning) {
			proc.destroy();
			try {
				proc.waitFor();
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			isRunning = false;
			Thread.currentThread().interrupt();
		}
	}
}
