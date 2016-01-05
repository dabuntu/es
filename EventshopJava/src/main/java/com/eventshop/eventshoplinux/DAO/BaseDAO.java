package com.eventshop.eventshoplinux.DAO;

import java.sql.Connection;
import java.sql.DriverManager;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.eventshop.eventshoplinux.constant.Constant.*;

public class BaseDAO {

	private final static Logger LOGGER = LoggerFactory.getLogger(BaseDAO.class);
	public Connection con = connection();

	public Connection connection() {
		try {
			Class.forName(DRIVER_NAME);
			String url = Config.getProperty(DB_URL);
			String userName = Config.getProperty(USR_NAME);
			String pwd = Config.getProperty(PASSWORD);
			con = DriverManager.getConnection(url, userName, pwd);

		} catch (Exception e) {
			LOGGER.error(" driver issues " + e);

		}
		return con;
	}

}
