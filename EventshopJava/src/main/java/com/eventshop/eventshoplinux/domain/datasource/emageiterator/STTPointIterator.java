package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import java.util.Iterator;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.datasource.emage.STTPoint;

abstract public class STTPointIterator implements Iterator<STTPoint> {
	public String theme;
	public FrameParameters params;

	public STTPointIterator(String theme, FrameParameters params) {
		this.theme = theme;
		this.params = params;
	}

	public FrameParameters getParams() {
		return params;
	}

	public String getTheme() {
		return theme;
	}

	public void setParams(FrameParameters params) {
		this.params = params;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}
}
