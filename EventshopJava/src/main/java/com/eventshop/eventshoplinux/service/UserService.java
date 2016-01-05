package com.eventshop.eventshoplinux.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.eventshop.eventshoplinux.DAO.user.UserManagementDAO;
import com.eventshop.eventshoplinux.domain.login.User;

@Path("/userservice")
public class UserService {

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users")
	public String registerUser(User user) {

		UserManagementDAO eventShopDAO = new UserManagementDAO();
		/* eventShopDAO.saveUser(user); */

		return eventShopDAO.saveUser(user);

	}

	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/users")
	public User login(User loginUser) {
		UserManagementDAO eventShopDAO = new UserManagementDAO();
		loginUser = eventShopDAO.logIn(loginUser);
		return loginUser;

	}

}
