package com.eventshop.eventshoplinux.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.eventshop.eventshoplinux.DAO.user.UserManagementDAO;
import com.eventshop.eventshoplinux.domain.login.User;

@Path("/loginservice")
public class LoginService {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/login")
	public User login(User loginUser) {
		UserManagementDAO eventShopDAO = new UserManagementDAO();
		loginUser = eventShopDAO.logIn(loginUser);
		return loginUser;

	}

}
