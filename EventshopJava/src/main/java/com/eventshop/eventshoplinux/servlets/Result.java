package com.eventshop.eventshoplinux.servlets;

public class Result {

	String resId;
	String status;
	String output;
	String comment;

	public Result() {

	}

	public Result(String id, String status, String output, String comment) {
		this.resId = id;
		this.status = status;
		this.output = output;
		this.comment = comment;
	}

	public String getResId() {
		return resId;
	}

	public void setResId(String resId) {
		this.resId = resId;
	}

	public String getOutput() {
		return output;
	}

	public void setOutput(String output) {
		this.output = output;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String cm) {
		this.comment = cm;
	}

	public String getStatus() {
		return status;
	}

	public void setInfo() {
		this.status = "info";
	}

	public void setSuccess() {
		this.status = "success";
	}

	public void setError() {
		this.status = "error";
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
