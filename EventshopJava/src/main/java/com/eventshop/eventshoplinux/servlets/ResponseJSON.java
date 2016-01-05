package com.eventshop.eventshoplinux.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.gson.JsonObject;

public class ResponseJSON {

	public enum ResponseStatus {
		SUCCESS, INFO, ERROR
	};

	public HttpServletResponse servlet;

	public ResponseJSON(HttpServletResponse http, ResponseStatus status,
			String msg) {
		JsonObject response = new JsonObject();
		response.addProperty(status.name(), msg);
		servlet = http;
		servlet.setContentType("application/json");
		try {
			servlet.getWriter().write(response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ResponseJSON(HttpServletResponse response, Result query) {
		try {
			response.setContentType("application/json");
			PrintWriter out = response.getWriter();
			ObjectMapper mapper = new ObjectMapper();
			mapper.writeValue(out, query);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ResponseJSON(HttpServletResponse http) {
		servlet = http;
		servlet.setContentType("application/json");

	}

	public ResponseJSON(HttpServletResponse http, String msg) {
		JsonObject response = new JsonObject();
		response.addProperty("result", msg);
		servlet = http;
		servlet.setContentType("application/json");
		try {
			servlet.getWriter().write(response.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ResponseJSON(HttpServletResponse http, JsonObject res) {
		servlet = http;
		servlet.setContentType("application/json");
		try {
			servlet.getWriter().write(res.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void writeResponse(JsonObject res) {
		try {
			servlet.getWriter().write(res.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

	}

}
