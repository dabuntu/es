package com.eventshop.eventshoplinux.test;

import java.io.*;

class StreamGobbler extends Thread {
	InputStream is;
	String type;
	OutputStream os;

	StreamGobbler(InputStream is, String type) {
		this(is, type, null);
	}

	StreamGobbler(InputStream is, String type, OutputStream redirect) {
		this.is = is;
		this.type = type;
		this.os = redirect;
	}

	@Override
	public void run() {
		try {
			InputStreamReader inpStrd = new InputStreamReader(is);
			BufferedReader buffRd = new BufferedReader(inpStrd);
			String line = null;
			while ((line = buffRd.readLine()) != null) {
				System.out.println(type + " —> " + line);
			}
			buffRd.close();

		} catch (Exception e) {
			System.out.println(e);
		}

	}
	/*
	 * public void run() { try { PrintWriter pw = null; if (os != null) pw = new
	 * PrintWriter(os);
	 * 
	 * InputStreamReader isr = new InputStreamReader(is); BufferedReader br =
	 * new BufferedReader(isr); String line=null; while ( (line = br.readLine())
	 * != null) { if (pw != null) pw.println(line); System.out.println(type +
	 * ">" + line); } if (pw != null) pw.flush(); } catch (IOException ioe) {
	 * ioe.printStackTrace(); } }
	 */

}

public class TestExec {
	public static void main(String args[]) {
		if (args.length < 1) {
			System.out.println("USAGE: java TestExec \"cmd\"");
			System.exit(1);
		}

		String exe = "/usr/share/tomcat7/webapps/eventshoplinux/proc/Debug/EmageOperators_Q7";
		Process proc = null;
		try {
			String cmd = args[0];
			Runtime rt = Runtime.getRuntime();
			proc = rt.exec(cmd);

			// any error message?
			StreamGobbler errorGobbler = new StreamGobbler(
					proc.getErrorStream(), "ERR");

			// any output?
			StreamGobbler outputGobbler = new StreamGobbler(
					proc.getInputStream(), "OUT");

			// kick them off
			errorGobbler.start();
			outputGobbler.start();

			// any error???
			int exitVal = proc.waitFor();
			System.out.println("ExitValue: " + exitVal);
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if (proc != null)
				proc.destroy();
		}
	}
}