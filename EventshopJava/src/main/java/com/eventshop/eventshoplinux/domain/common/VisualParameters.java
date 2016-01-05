package com.eventshop.eventshoplinux.domain.common;

public class VisualParameters {

	// Need this at the time of storing datasource in db
	public String tranMatPath;
	public String colorMatPath;

	public ConversionMatrix translationMatrix;
	public ConversionMatrix colorMatrix;

	public String maskPath;
	public int ignoreSinceNumber;

	public ConversionMatrix getTranslationMatrix() {
		return translationMatrix;
	}

	public void setTranslationMatrix(ConversionMatrix translationMatrix) {
		this.translationMatrix = translationMatrix;
	}

	public ConversionMatrix getColorMatrix() {
		return colorMatrix;
	}

	public void setColorMatrix(ConversionMatrix colorMatrix) {
		this.colorMatrix = colorMatrix;
	}

	public String getMaskPath() {
		return maskPath;
	}

	public void setMaskPath(String maskPath) {
		this.maskPath = maskPath;
	}

	public int getIgnoreSinceNumber() {
		return ignoreSinceNumber;
	}

	public void setIgnoreSinceNumber(int ignoreSinceNumber) {
		this.ignoreSinceNumber = ignoreSinceNumber;
	}

	public String getTranMatPath() {
		return tranMatPath;
	}

	public void setTranMatPath(String tranMatPath) {
		this.tranMatPath = tranMatPath;
	}

	public String getColorMatPath() {
		return colorMatPath;
	}

	public void setColorMatPath(String colorMatPath) {
		this.colorMatPath = colorMatPath;
	}

}
