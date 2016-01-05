package com.eventshop.eventshoplinux.domain.login;

public class User {

	private int id;
	private String userName;
	private String password;
	private String emailId;
	private String gender;
	private String authentication;
	private int roleId;
	private String roleType;
	private String userLastAccessed;
	private String status;
	private boolean checkAdmin;

	public boolean isCheckAdmin() {
		return checkAdmin;
	}

	public void setCheckAdmin(boolean checkAdmin) {
		this.checkAdmin = checkAdmin;
	}

	// public List<DataSourceListElement> dataSrcList;
	// public List<Query> queryList;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getAuthentication() {
		return authentication;
	}

	public void setAuthentication(String authentication) {
		this.authentication = authentication;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public String getRoleType() {
		return roleType;
	}

	public void setRoleType(String roleType) {
		this.roleType = roleType;
	}

	public String getUserLastAccessed() {
		return userLastAccessed;
	}

	public void setUserLastAccessed(String userLastAccessed) {
		this.userLastAccessed = userLastAccessed;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
