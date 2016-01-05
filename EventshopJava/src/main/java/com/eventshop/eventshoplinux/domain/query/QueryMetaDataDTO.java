package com.eventshop.eventshoplinux.domain.query;

public class QueryMetaDataDTO {

	int qID;
	String type;
	QueryDTO query;

	public int getqID() {
		return qID;
	}

	public void setqID(int qID) {
		this.qID = qID;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public QueryDTO getQuery() {
		return query;
	}

	public void setQuery(QueryDTO query) {
		this.query = query;
	}

}
