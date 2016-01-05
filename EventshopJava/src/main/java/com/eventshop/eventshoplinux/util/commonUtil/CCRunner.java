package com.eventshop.eventshoplinux.util.commonUtil;

import static com.eventshop.eventshoplinux.constant.Constant.WINOS;

import java.io.IOException;

public class CCRunner {

	String osName = "";
	String commandTobeRun = "";
	String cygwinPath = "";
	String cygwinBash = "";

	public void CCRunner() {
		osName = Config.getProperty("currentOS");
		if (osName.equals(WINOS)) {
			commandTobeRun = Config.getProperty("ccComilerBAT");
			cygwinPath = Config.getProperty("cygwinPath");
		} else {
			commandTobeRun = Config.getProperty("ccCompilerSH");
		}
	}

	public void CCRunner(String command) {
		osName = Config.getProperty("currentOS");
		commandTobeRun = command;
	}

	public void runSHOnEnv() {
		try {
			Process p = Runtime.getRuntime().exec(commandTobeRun);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void runSHOnWindows() {
		try {
			String path = "PATH=" + cygwinPath;
			Process p = Runtime.getRuntime().exec(
					new String[] { cygwinBash, "-c", commandTobeRun },
					new String[] {});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
