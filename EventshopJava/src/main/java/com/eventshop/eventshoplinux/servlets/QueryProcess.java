package com.eventshop.eventshoplinux.servlets;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
//import com.eventshop.eventshoplinux.servlets.DataSource;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.eventshop.eventshoplinux.util.datasourceUtil.DataProcess;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

public class QueryProcess implements Runnable {
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
	// is colors?

	String colors = "";

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

	public QueryProcess(String context) {
		contextPath = context;
		if (contextPath == "")
			contextPath = Config.getProperty("context");
		dataFolderPath = Config.getProperty("tempDir");

		queriedSources = new ArrayList<DataSource>();
		queryCC = new ArrayList<String>();

		dataProcesses = new ArrayList<DataProcess>();
		OS = checkOS();
	}

	public void setColors(String colors) {
		this.colors = colors;
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
		outputName = queryID;// + "_" + queryType;
		cc += ("\t\t\tcreate_output(e, \"" + contextPath.replace("\\", "\\\\")
				+ Constant.RESULT_Q.replace("\\", "\\\\") + outputName
				+ "\",\"" + colors + "\");\n");

		cc += ("\t\t}\n" + "\t\tSleep(100);\n" + "\t}\n" + "\treturn 0;\n"
				+ "}\n");
		ccPath = contextPath + "proc\\src\\" + queryID + ".cc";
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
				+ "#include <ctime>\n\n" + "#include <unistd.h>\n\n"
				+ "using namespace cv;\n\n" + "int main()\n" + "{\n";
		for (int i = 0; i < queriedSources.size(); ++i) {
			DataSource src = queriedSources.get(i);

			// String filename = "\""+ dataFolderPath + "ds" + src.srcID + "_" +
			// src.srcName + "\"";
			String filename = "\"" + dataFolderPath + "ds" + src.srcID + "\"";

			if (src.srcVarName == null)
				cc += ("\tEmageIngestor ds" + src.srcID + "(" + filename + ");\n");
			else
				cc += ("\tEmageIngestor " + src.srcVarName + "(" + filename + ");\n");
		}

		for (int i = 0; i < queryCC.size(); ++i) {
			log.info("last sub query -> queryID " + queryID + "_"
					+ (queryCC.size() - 1));
			if (i == queryCC.size() - 1) {// root op
				String qCode = queryCC.get(i);
				qCode = qCode.replace(queryID + "_" + (i + 1), queryID);
				System.out.println("qCode is " + qCode);
				cc += qCode;
				log.info("replace id of last subquery " + queryID + "_"
						+ (i + 1) + " with masterqid ");
			} else {

				System.out.println("qCode is " + queryCC.get(i));
				cc += queryCC.get(i);
			}
		}

		cc += ("\twhile(true)\n" + "\t{\n" + "\t\twhile(" + queryID
				+ ".has_next())\n" + "\t\t{\n" + "\t\t\tEmage e = " + queryID + ".next();\n");

		// Decide how to show outputs
		outputName = queryID;// + "_" + queryType;
		cc += ("\t\t\tcreate_output(e, \"" + contextPath.replace("\\", "/")
				+ Constant.RESULT_Q + outputName + "\",\"" + colors + "\");\n");

		cc += ("\t\t}\n" + "\t\tsleep(100);\n" + // (CAP "S") Sleep(100) for
													// windows, but sleep(100)
													// for linux
				"\t}\n" + "\treturn 0;\n" + "}\n");
		ccPath = contextPath + "proc/src/" + queryID + ".cc";
		log.info("ccPath " + ccPath);
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
			// String[] tcompile = {"/bin/sh", "cd",
			// "/usr/share/tomcat7/webapps/eventshoplinux/proc/Debug"};
			compile = tcompile;
			run = contextPath + "proc/Debug/EmageOperators_" + queryID;
			log.info("Execution file: " + run);
		} else if (OS == 1) {
			String[] tcompile = {
					"\"" + contextPath + "proc\\Debug\\compile.bat\"", queryID,
					"EmageOperators_" + queryID };
			compile = tcompile;
			run = "\"" + contextPath + "proc\\Debug\\EmageOperators_" + queryID
					+ ".exe\"";
		}

		try {
			if (compile != null && run != "") {
				// Compile the query

				proc = Runtime.getRuntime().exec(compile);
				BufferedReader bri = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));
				BufferedReader bre = new BufferedReader(new InputStreamReader(
						proc.getErrorStream()));

				String errLine;
				log.info("Compiling........");
				while ((errLine = bre.readLine()) != null) {
					log.info(errLine);
				}
				String line;
				while ((line = bri.readLine()) != null) {
					log.info(line);
				}
				proc.destroy();
				proc.waitFor();
				log.info("Compiled!");

				// Run the query
				log.info("Trying to RUN: " + run);
				proc = Runtime.getRuntime().exec(run);

				bri = new BufferedReader(new InputStreamReader(
						proc.getInputStream()));
				bre = new BufferedReader(new InputStreamReader(
						proc.getErrorStream()));

				while ((line = bri.readLine()) != null) {
					log.info(line);
				}
				bri.close();
				while ((line = bre.readLine()) != null) {
					log.info(line);
				}
				bre.close();
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
			log.info("proc.destroy");
			try {
				log.info("proc.waitFor");
				proc.waitFor();
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			isRunning = false;
			Thread.currentThread().interrupt();
		}
	}
	/*
	 * public void testExe(String cmd){ try { String line, er; Process p =
	 * Runtime.getRuntime().exec(cmd); // InputStream stderr =
	 * p.getErrorStream(); //InputStreamReader isr = new
	 * InputStreamReader(stderr); //BufferedReader br = new BufferedReader(isr);
	 * //System.out.println("<Execute>"); //while ( (line = br.readLine())
	 * !=null){ // System.out.println(line); //
	 * //System.out.println("</Execute>"); //int exitVal = p.waitFor();
	 * //System.out.println("Process exitValue: " + exitVal);
	 * 
	 * BufferedReader bri = new BufferedReader (new
	 * InputStreamReader(p.getInputStream())); BufferedReader bre = new
	 * BufferedReader (new InputStreamReader(p.getErrorStream()));
	 * 
	 * while ((line = bri.readLine()) != null) { System.out.println(line); }
	 * bri.close(); while ((line = bre.readLine()) != null) {
	 * System.out.println(line); } bre.close(); int exitVal = p.waitFor();
	 * System.out.println("Done. exitVal: " + exitVal);
	 * 
	 * } catch (Exception err) { err.printStackTrace(); } catch (Throwable t){
	 * t.printStackTrace(); } }
	 */
}
