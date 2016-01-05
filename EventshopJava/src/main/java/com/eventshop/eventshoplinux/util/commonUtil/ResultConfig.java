package com.eventshop.eventshoplinux.util.commonUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ResultConfig {
	private static Properties configProp = new Properties();

	static {
		try {
			InputStream in = Config.class
					.getResourceAsStream("/resultConfig.properties");
			configProp.load(in);
			in.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getProperty(String key) {
		return configProp.getProperty(key);
	}

}
