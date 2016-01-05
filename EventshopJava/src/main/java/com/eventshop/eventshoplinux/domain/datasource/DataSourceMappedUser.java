package com.eventshop.eventshoplinux.domain.datasource;

import java.util.HashMap;
import java.util.List;

public class DataSourceMappedUser {

	public List<HashMap<Integer, String>> dsLinkedUserList;

	// public List<HashMap<Integer, String>> dsNotLinkedUserList;

	public List<HashMap<Integer, String>> getDsLinkedUserList() {
		return dsLinkedUserList;
	}

	public void setDsLinkedUserList(
			List<HashMap<Integer, String>> dsLinkedUserList) {
		this.dsLinkedUserList = dsLinkedUserList;
	}

}
