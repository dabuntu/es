package com.eventshop.eventshoplinux.servlets;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;

import com.eventshop.eventshoplinux.DAO.alert.AlertDAO;


@SuppressWarnings("serial")
public class StartupHandler extends HttpServlet {

	@Override
	public void init() throws ServletException
    {
          AlertDAO aDAO = new AlertDAO();
          boolean status = false;
          status = aDAO.disableAllAlerts();
          if(status) {
        	  System.out.println("Disable all alerts works");
          } else {
			System.out.println("disable all alerts doesn't work");
		}
    }
	
}
