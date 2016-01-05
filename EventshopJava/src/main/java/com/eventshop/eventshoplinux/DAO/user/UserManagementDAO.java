package com.eventshop.eventshoplinux.DAO.user;

import com.eventshop.eventshoplinux.DAO.BaseDAO;
import com.eventshop.eventshoplinux.domain.login.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static com.eventshop.eventshoplinux.constant.Constant.*;

public class UserManagementDAO extends BaseDAO {


	private final static Logger LOGGER = LoggerFactory.getLogger(UserManagementDAO.class);
	public User getLoginDetails(User user) {
		PreparedStatement ps = null;
		ResultSet rs = null;

		try {
			ps = con.prepareStatement(SELECT_USRMSTR_QRY);
			rs = ps.executeQuery();

			while (rs.next()) {

				user.getUserName();
				user.getPassword();
			}

		} catch (Exception e) {
		}

		return user;
	}

	public String saveUser(User user) {

		PreparedStatement ps = null;
		String userKey = user.getAuthentication();
		try {
			ps = con.prepareStatement(INSERT_USERMSTR_QRY);
			ps.setString(1, user.getEmailId());
			ps.setString(2, user.getPassword());
			ps.setString(3, userKey);
			ps.setString(4, user.getUserName());
			ps.setString(5, user.getGender());
			ps.setString(6, userKey);
			ps.setString(7, Integer.toString(user.getRoleId())); // got to check
			// this

			ps.executeUpdate();
			return SUCCESS;

		} catch (Exception e) {
			LOGGER.info("exception is here" + e.getMessage());
			return FAILURE;
		}

	}

	public User logIn(User loginUser) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		String adminRole = ADMIN;
		int id = -1;
		String authQuery = SELECT_USRMSTR_AUTH_QRY;
		try {
			ps = con.prepareStatement(authQuery);
			ps.setString(1, loginUser.getUserName());
			rs = ps.executeQuery();
			while (rs.next()) {
				loginUser.setAuthentication(rs.getString(1));
			}

			String userQuery = SELECT_USRMSTR_ARG_QRY;
			userQuery = (loginUser.isCheckAdmin() ? SELECT_USRMSTR_ADMIN_QRY
					+ adminRole + SELECT_USRMSTR_ADMIN_ARG_QRY : userQuery);

			try {
				ps = con.prepareStatement(userQuery);
				ps.setString(1, loginUser.getUserName());
				ps.setString(2, loginUser.getPassword());
				ps.setString(3, loginUser.getAuthentication());

				rs = ps.executeQuery();

				while (rs.next()) {
					loginUser.setId(rs.getInt(USER_ID));
					// String user_name=rs.getString("user_fullname");
					// String user_password=rs.getString("user_password");

				}
				// System.out.println("useer IDDD "+loginUser.getId());

			} catch (Exception e) {
				// TODO Auto-generated catch block
				// log.error(e.getMessage());
				LOGGER.info(DB_EXPT + e.getMessage());
				loginUser.setId(id);
			}
		} catch (Exception ex) {
			LOGGER.info(DB_EXPT + ex.getMessage());
			loginUser.setId(id);
		}
		return loginUser;
	}

	public int getUserID(String email) {

		PreparedStatement ps = null;
		ResultSet rs = null;
		String adminRole = ADMIN;
		int id = -1;
		String authQuery = SELECT_USR_FROM_EMAIL;
		try {
			ps = con.prepareStatement(authQuery);
			ps.setString(1, email);
			rs = ps.executeQuery();
			while (rs.next()) {
				return rs.getInt(USER_ID);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;

	}
}