package com.eventshop.eventshoplinux.domain.alert;

public class Alert {
	private int aID;
	private String alertName;
	private int alertType;
	private String alertTheme;
	private String alertMessage;
	private String alertSrcId;
	private String safeSrcId;
	private long alertMinVal;
	private long alertMaxVal;
	private long safeMinVal;
	private long safeMaxVal;
	private int alertStatus;
	private int user_id;
	private String resultEndpoint;

	/**
	 * @return the aID
	 */
	public int getaID() {
		return aID;
	}
	/**
	 * @param aID the aID to set
	 */
	public void setaID(int aID) {
		this.aID = aID;
	}
	/**
	 * @return the alertName
	 */
	public String getAlertName() {
		return alertName;
	}
	/**
	 * @param alertName the alertName to set
	 */
	public void setAlertName(String alertName) {
		this.alertName = alertName;
	}
	/**
	 * @return the alertType
	 */
	public int getAlertType() {
		return alertType;
	}
	/**
	 * @param alertType the alertType to set
	 */
	public void setAlertType(int alertType) {
		this.alertType = alertType;
	}
	/**
	 * @return the alertStatus
	 */
	public int getAlertStatus() {
		return alertStatus;
	}
	/**
	 * @param alertStatus the alertStatus to set
	 */
	public void setAlertStatus(int alertStatus) {
		this.alertStatus = alertStatus;
	}
	/**
	 * @return the alertTheme
	 */
	public String getAlertTheme() {
		return alertTheme;
	}
	/**
	 * @param alertTheme the alertTheme to set
	 */
	public void setAlertTheme(String alertTheme) {
		this.alertTheme = alertTheme;
	}
	/**
	 * @return the alertSrcId
	 */
	public String getAlertSrcId() {
		return alertSrcId;
	}
	/**
	 * @param alertSrcId the alertSrcId to set
	 */
	public void setAlertSrcId(String alertSrcId) {
		this.alertSrcId = alertSrcId;
	}
	/**
	 * @return the safeSrcId
	 */
	public String getSafeSrcId() {
		return safeSrcId;
	}
	/**
	 * @param safeSrcId the safeSrcId to set
	 */
	public void setSafeSrcId(String safeSrcId) {
		this.safeSrcId = safeSrcId;
	}
	/**
	 * @return the alertMinVal
	 */
	public long getAlertMinVal() {
		return alertMinVal;
	}
	/**
	 * @param alertMinVal the alertMinVal to set
	 */
	public void setAlertMinVal(long alertMinVal) {
		this.alertMinVal = alertMinVal;
	}
	/**
	 * @return the alertMaxVal
	 */
	public long getAlertMaxVal() {
		return alertMaxVal;
	}
	/**
	 * @param alertMaxVal the alertMaxVal to set
	 */
	public void setAlertMaxVal(long alertMaxVal) {
		this.alertMaxVal = alertMaxVal;
	}
	/**
	 * @return the safeMinVal
	 */
	public long getSafeMinVal() {
		return safeMinVal;
	}
	/**
	 * @param safeMinVal the safeMinVal to set
	 */
	public void setSafeMinVal(long safeMinVal) {
		this.safeMinVal = safeMinVal;
	}
	/**
	 * @return the safeMaxVal
	 */
	public long getSafeMaxVal() {
		return safeMaxVal;
	}
	/**
	 * @param safeMaxVal the safeMaxVal to set
	 */
	public void setSafeMaxVal(long safeMaxVal) {
		this.safeMaxVal = safeMaxVal;
	}


	public int getUser_id() {
		return user_id;
	}

	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}

	public String getResultEndpoint() {
		return resultEndpoint;
	}

	public void setResultEndpoint(String resultEndpoint) {
		this.resultEndpoint = resultEndpoint;
	}

	public String getAlertMessage() {
		return alertMessage;
	}

	public void setAlertMessage(String alertMessage) {
		this.alertMessage = alertMessage;
	}
}
