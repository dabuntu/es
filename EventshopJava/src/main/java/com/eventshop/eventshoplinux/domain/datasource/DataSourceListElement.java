package com.eventshop.eventshoplinux.domain.datasource;

import java.util.List;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.login.User;

public class DataSourceListElement {

	private long srcID;
	private String srcName;
	private String status;
	private String desc;
	private String format;
	private String resolution_type;
	private String access;
	private String url;
	private String type;
	private int creater;
	private int archive;
	private int unit;
	private String createdDate;
	private String emailOfCreater;
	private int control;
	private String boundingbox;
	private String dsTitle;// name for running DS
	private FrameParameters finalParam;
	public Long timeWindow; // new added 16-07-2014 -- sanjukta

	public Long getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(Long timeWindow) {
		this.timeWindow = timeWindow;
	}

	public List<User> userListAssToDSAccess;

	public List<User> getUserListAssToDSAccess() {
		return userListAssToDSAccess;
	}

	public void setUserListAssToDSAccess(List<User> userListAssToDSAccess) {
		this.userListAssToDSAccess = userListAssToDSAccess;
	}

	public long getSrcID() {
		return srcID;
	}

	public void setSrcID(long srcID) {
		this.srcID = srcID;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getCreater() {
		return creater;
	}

	public void setCreater(int creater) {
		this.creater = creater;
	}

	public int getArchive() {
		return archive;
	}

	public void setArchive(int archive) {
		this.archive = archive;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public int getControl() {
		return control;
	}

	public void setControl(int control) {
		this.control = control;
	}

	public String getEmailOfCreater() {
		return emailOfCreater;
	}

	public void setEmailOfCreater(String emailOfCreater) {
		this.emailOfCreater = emailOfCreater;
	}

	public String getResolution_type() {
		return resolution_type;
	}

	public void setResolution_type(String resolution_type) {
		this.resolution_type = resolution_type;
	}

	public String getBoundingbox() {
		return boundingbox;
	}

	public void setBoundingbox(String boundingbox) {
		this.boundingbox = boundingbox;
	}

	public String getDsTitle() {
		return dsTitle;
	}

	public void setDsTitle(String dsTitle) {
		this.dsTitle = dsTitle;
	}

	public FrameParameters getFinalParam() {
		return finalParam;
	}

	public void setFinalParam(FrameParameters finalParam) {
		this.finalParam = finalParam;
	}

}
