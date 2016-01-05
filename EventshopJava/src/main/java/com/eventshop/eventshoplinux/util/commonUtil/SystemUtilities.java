package com.eventshop.eventshoplinux.util.commonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

//import servlets.RegisterServlet;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.eventshop.eventshoplinux.constant.Constant;

public class SystemUtilities {
	protected static Log log = LogFactory.getLog(SystemUtilities.class);
	private static final String TASKLIST = "tasklist";
	private static final String KILL = "taskkill /IM ";
	private static final String PROCESS = "EmageOperators_q";
	private static final String QUERYSTORE = Config.getProperty("tempDir")
			+ "/query/";
	private static final String DATASTORE = Config.getProperty("tempDir")
			+ "/datasource/";
	private static final String CONTEXTPATH = Config.getProperty("context");

	public SystemUtilities() {

	}

	public static boolean reset() {
		// To reset the system
		try {
			int os = checkOS();
			// 1. kill running EmageOperators_qXX.exe
			killEmageOperatorsProcess(os);

			// 2. Remove temporary files
			removeTempFile(os);

			return true;
		} catch (Exception e) {
			log.error(e.getMessage());
			return false;
		}

	}

	private static boolean isFixDS(String ds) {
		String[] fixDS = { "ds0.", "ds1.", "ds2.", "ds3.", "ds4.", "ds5.",
				"ds6.", "ds7.", "ds8.", "ds9.", "ds10.", "ds11.", "ds12.",
				"ds13.", "ds14." };
		for (int i = 0; i < fixDS.length; i++) {
			if (ds.contains(fixDS[i]))
				return true;
		}
		return false;
	}

	public static int checkOS() {
		String currentOs = System.getProperty("os.name").toLowerCase();
		int os;
		if (currentOs.indexOf("nix") >= 0 || currentOs.indexOf("nux") >= 0
				|| currentOs.indexOf("aix") > 0) {
			os = 0;
		} else if (currentOs.indexOf("win") >= 0) {
			os = 1;
		} else {
			os = -1;
		}
		return os;
	}

	private static void removeTempFile(int os) throws IOException {
		// remove temp query results
		File resultsDir = new File(CONTEXTPATH + Constant.RESULT_Q);
		File[] resultsEmage = resultsDir.listFiles();
		if (resultsEmage != null) {
			for (int i = 0; i < resultsEmage.length; i++) {
				if (resultsEmage[i].isFile()) {
					FileUtils.forceDelete(resultsEmage[i]);
				}
			}
		}

		// move temporary added datasources
		File dsDir = new File(CONTEXTPATH + Constant.RESULT_DS);
		File[] ds = dsDir.listFiles();
		if (ds != null) {
			for (int i = 0; i < ds.length; i++) {
				if (ds[i].isFile() && !isFixDS(ds[i].getName())
						&& !ds[i].getName().contains("before")) {
					FileUtils.forceDelete(ds[i]);

				}
			}
		}

		// move *.o file to temp folder
		File objDir = new File(CONTEXTPATH + "proc/Debug/src/");
		File[] objQuery = objDir.listFiles();
		String pattern = "\\Aq\\d*\\.o";
		Pattern r = Pattern.compile(pattern);

		if (objQuery != null) {
			for (int i = 0; i < objQuery.length; i++) {
				Matcher m = r.matcher(objQuery[i].getName());
				if (m.find() && !objQuery[i].getName().contains("q13")
						&& !objQuery[i].getName().contains("q4")) {
					FileUtils.forceDelete(objQuery[i]);
				}
			}
		}

		// move *.exe file to temp folder
		File exeDir = new File(CONTEXTPATH + "proc/Debug/");
		File[] exeQuery = exeDir.listFiles();
		if (os == 0)
			pattern = "EmageOperators_q\\d*"; // \\.exe";
		else if (os == 1)
			pattern = "EmageOperators_q\\d*\\.exe";
		else
			pattern = null;

		if (pattern != null) {
			r = Pattern.compile(pattern);

			if (objQuery != null) {
				for (int i = 0; i < exeQuery.length; i++) {
					Matcher m = r.matcher(exeQuery[i].getName());
					if (m.find() && !exeQuery[i].getName().contains("q13")
							&& !exeQuery[i].getName().contains("q4")) {
						FileUtils.forceDelete(exeQuery[i]);
					}
				}
			}
		}

		// move *.cc source code to temp folder
		File srcDir = new File(CONTEXTPATH + "proc/src/");
		File[] srcQuery = srcDir.listFiles();
		pattern = "\\Aq\\d*\\.cc";
		r = Pattern.compile(pattern);

		if (srcQuery != null) {
			for (int i = 0; i < srcQuery.length; i++) {
				Matcher m = r.matcher(srcQuery[i].getName());
				if (m.find() && !srcQuery[i].getName().contains("q13")
						&& !srcQuery[i].getName().contains("q4")) {
					FileUtils.forceDelete(srcQuery[i]);
				}
			}
		}
	}

	public static void killEmageOperatorsProcess(int os) throws Exception {

		boolean found = false;
		if (os == 0) {
			Runtime.getRuntime().exec("pkill EmageOperators_");
		} else if (os == 1) {
			Process p = Runtime.getRuntime().exec(TASKLIST);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.contains(PROCESS)) {
					log.info("kill " + line.split(" ")[0]);
					Runtime.getRuntime().exec(
							KILL + " " + line.split(" ")[0] + " /f");
					found = true;
				}
			}
			// no more EmageOperators process is running
		}
	}

	private static void moveTempFile(int os) throws IOException {
		// create query store directory
		Date today = new Date();
		String todayTxt = "" + today.getYear() + (today.getMonth() + 1)
				+ today.getDate();
		File queryStoreDir = new File(QUERYSTORE + todayTxt);

		if (!queryStoreDir.exists())
			queryStoreDir.mkdir();

		// move result emage to temp folder
		File resultsDir = new File(CONTEXTPATH + Constant.RESULT_Q);
		File[] resultsEmage = resultsDir.listFiles();
		if (resultsEmage != null) {
			for (int i = 0; i < resultsEmage.length; i++) {
				if (resultsEmage[i].isFile()) {
					File destFile = new File(queryStoreDir.getAbsolutePath()
							+ "/" + resultsEmage[i].getName() + "_"
							+ resultsEmage[i].lastModified());
					if (destFile.exists())
						destFile.delete();
					FileUtils.moveFile(resultsEmage[i], destFile);
					log.info(resultsEmage[i] + ", " + destFile);
				}
			}
		}

		// move temporary added datasources
		File dsDir = new File(CONTEXTPATH + Constant.RESULT_DS);
		File[] ds = dsDir.listFiles();
		File DataStoreDir = new File(DATASTORE + todayTxt);
		if (ds != null) {
			for (int i = 0; i < ds.length; i++) {
				if (ds[i].isFile() && !isFixDS(ds[i].getName())
						&& !ds[i].getName().contains("before")) {
					File destFile = new File(DataStoreDir.getAbsolutePath()
							+ "/" + ds[i].getName() + "_"
							+ ds[i].lastModified());
					if (destFile.exists())
						destFile.delete();
					FileUtils.moveFile(ds[i], destFile);
					log.info(ds[i] + ", " + destFile);
				}
			}
		}

		// move *.o file to temp folder
		File objDir = new File(CONTEXTPATH + "proc/Debug/src/");
		File[] objQuery = objDir.listFiles();
		String pattern = "\\Aq\\d*\\.o";
		Pattern r = Pattern.compile(pattern);

		if (objQuery != null) {
			for (int i = 0; i < objQuery.length; i++) {
				Matcher m = r.matcher(objQuery[i].getName());
				if (m.find() && !objQuery[i].getName().contains("q13")
						&& !objQuery[i].getName().contains("q4")) {
					File destFile = new File(queryStoreDir.getAbsolutePath()
							+ "/" + objQuery[i].getName() + "_"
							+ objQuery[i].lastModified());
					if (destFile.exists())
						destFile.delete();
					FileUtils.moveFile(objQuery[i], destFile);
					log.info(objQuery[i] + ", " + destFile);
				}
			}
		}

		// move *.exe file to temp folder
		File exeDir = new File(CONTEXTPATH + "proc/Debug/");
		File[] exeQuery = exeDir.listFiles();
		if (os == 0)
			pattern = "EmageOperators_q\\d*";
		else if (os == 1)
			pattern = "EmageOperators_q\\d*\\.exe";
		else
			pattern = null;

		if (pattern != null) {
			r = Pattern.compile(pattern);

			if (objQuery != null) {
				for (int i = 0; i < exeQuery.length; i++) {
					Matcher m = r.matcher(exeQuery[i].getName());
					if (m.find() && !exeQuery[i].getName().contains("q13")
							&& !exeQuery[i].getName().contains("q4")) {
						File destFile = new File(
								queryStoreDir.getAbsolutePath() + "/"
										+ exeQuery[i].getName() + "_"
										+ exeQuery[i].lastModified());
						if (destFile.exists())
							destFile.delete();
						FileUtils.moveFile(exeQuery[i], destFile);
						log.info(exeQuery[i] + ", " + destFile);
					}
				}
			}
		}
		// move *.cc source code to temp folder
		File srcDir = new File(CONTEXTPATH + "proc/src/");
		File[] srcQuery = srcDir.listFiles();
		pattern = "\\Aq\\d*\\.cc";
		r = Pattern.compile(pattern);

		if (srcQuery != null) {
			for (int i = 0; i < srcQuery.length; i++) {
				Matcher m = r.matcher(srcQuery[i].getName());
				if (m.find() && !srcQuery[i].getName().contains("q13")
						&& !srcQuery[i].getName().contains("q4")) {
					File destFile = new File(queryStoreDir.getAbsolutePath()
							+ "/" + srcQuery[i].getName() + "_"
							+ srcQuery[i].lastModified());
					if (destFile.exists())
						destFile.delete();
					log.info(srcQuery[i] + ", " + destFile);
					FileUtils.moveFile(srcQuery[i], destFile);

				}
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		SystemUtilities.reset();

	}

}
