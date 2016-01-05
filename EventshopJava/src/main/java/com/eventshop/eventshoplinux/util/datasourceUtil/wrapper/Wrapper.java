package com.eventshop.eventshoplinux.util.datasourceUtil.wrapper;

import com.eventshop.eventshoplinux.domain.datasource.emageiterator.STTPointIterator;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;

abstract public class Wrapper extends STTPointIterator implements Runnable {
	String url;

	public Wrapper(String url, String theme, FrameParameters params) {
		super(theme, params);
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	abstract public boolean stop();
}
