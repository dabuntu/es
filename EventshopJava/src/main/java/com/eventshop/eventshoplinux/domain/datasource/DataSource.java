package com.eventshop.eventshoplinux.domain.datasource;

import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.domain.common.VisualParameters;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;


@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSource {

	public String srcID = null;
	public String srcTheme = null;
	public String srcName = null;
	public String url = null;
	public String syntax = null;
	public DataFormat srcFormat;
	public String supportedWrapper = null;
	public ArrayList<String> bagOfWords = null;
	public VisualParameters visualParam = null;
	public FrameParameters initParam = null;
	public FrameParameters finalParam = null;
	public Wrapper wrapper = null;
	// Added to be used in variable generation
	public String srcVarName = null;
	public String userId = null;
	// newly added by Sanjukta while comparing with DataSourceListElement
	public String type;
	public String emailOfCreator;
	public int archive;
	public String status;
	public int control; // 0: stop, 1: running
	public int unit;
	public String boundingbox;
	public String access;

	public DataSource() {
		bagOfWords = new ArrayList<String>();
		visualParam = new VisualParameters();
		initParam = new FrameParameters();
		finalParam = new FrameParameters();
	}

	public DataSource(String ID, String theme, String name, String url,
					  DataFormat format, String supported, ArrayList<String> bagOfWords,
					  VisualParameters vParam, FrameParameters initParam,
					  FrameParameters finalParam) {
		this.srcID = ID;
		this.srcTheme = theme;
		this.srcName = name;
		this.url = url;
		this.srcFormat = format;
		this.supportedWrapper = supported;
		this.bagOfWords = bagOfWords;
		this.visualParam = vParam;
		this.initParam = initParam;
		this.finalParam = finalParam;
	}

	public DataSource(DataSource in) {
		this.srcID = in.srcID;
		this.srcTheme = in.srcTheme;
		this.srcName = in.srcName;
		this.url = in.url;
		this.srcFormat = in.srcFormat;
		this.supportedWrapper = in.supportedWrapper;
		this.bagOfWords = in.bagOfWords;
		this.visualParam = in.visualParam;
		this.initParam = in.initParam;
		this.finalParam = in.finalParam;
		this.srcVarName = in.srcVarName;
		this.syntax = in.syntax;
	}

	public String getAccess() {
		return access;
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public String getBoundingbox() {
		return boundingbox;
	}

	public void setBoundingbox(String boundingbox) {
		this.boundingbox = boundingbox;
	}

	public int getControl() {
		return control;
	}

	public void setControl(int control) {
		this.control = control;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getEmailOfCreator() {
		return emailOfCreator;
	}

	public void setEmailOfCreator(String emailOfCreator) {
		this.emailOfCreator = emailOfCreator;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	};

	/*
	 * public class VisualParameters { public String tranMatPath; public String
	 * colorMatPath; public String maskPath; public int ignoreSinceNumber; }
	 */

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public void createVisualObject() {
		visualParam = new VisualParameters();
	}

	public void createInitParam() {
		initParam = new FrameParameters();
	}

	public void createFinalParam() {
		finalParam = new FrameParameters();
	}

	@Override
	public String toString() {
		String output;
		output = "Data Source ID: " + this.srcID + "\n";
		output += ("Theme: " + this.srcTheme + "\n");
		output += ("Name: " + this.srcName + "\n");
		output += ("URL: " + this.url + "\n");
		output += ("Format: " + this.srcFormat + "\n");
		output += ("Supported Wrapper: " + this.supportedWrapper + "\n");
		output += ("Bag of Words: ");

		for (String word : this.bagOfWords) {
			output += (word + ", ");
		}
		output += "\n";

		if (this.visualParam == null) {
			output += ("Visual Parameter: null");
		} else {
			output += ("Visual Parameters\n");
			output += ("Transformation Matrix: " + this.visualParam.translationMatrix);
			output += ("Color Matrix: " + this.visualParam.colorMatrix);
			output += ("Mask Matrix: " + this.visualParam.maskPath);
			output += ("Ignore Since Number: ")
					+ this.visualParam.ignoreSinceNumber;
		}
		output += "\n";

		if (this.initParam == null) {
			output += ("Initial Parameter: null\n");
		} else {
			output += ("Initial Parameters\n");
			output += ("Time Window: " + this.initParam.timeWindow);
			output += ("Sync At: " + this.initParam.syncAtMilSec);
			output += ("Time Type: " + this.initParam.timeType);
			output += ("Lat Unit: " + this.initParam.latUnit);
			output += ("Long Unit: " + this.initParam.longUnit);
			output += ("SW Lat: " + this.initParam.swLat);
			output += ("SW Long: " + this.initParam.swLong);
			output += ("NE Lat: " + this.initParam.neLat);
			output += ("NE Long: " + this.initParam.neLong);
		}
		output += "\n";

		if (this.finalParam == null) {
			output += ("Final Parameter: null\n");
		} else {
			output += ("Final Parameters\n");
			output += ("Time Window: " + this.finalParam.timeWindow);
			output += ("Sync At: " + this.finalParam.syncAtMilSec);
			output += ("Time type: " + this.finalParam.timeType);
			output += ("Lat Unit: " + this.finalParam.latUnit);
			output += ("Long Unit: " + this.finalParam.longUnit);
			output += ("SW Lat: " + this.finalParam.swLat);
			output += ("SW Long: " + this.finalParam.swLong);
			output += ("NE Lat: " + this.finalParam.neLat);
			output += ("NE Long: " + this.finalParam.neLong);
		}

		return output;
	}

	public String getSrcID() {
		return srcID;
	}

	public void setSrcID(String srcID) {
		this.srcID = srcID;
	}

	public String getSrcTheme() {
		return srcTheme;
	}

	public void setSrcTheme(String srcTheme) {
		this.srcTheme = srcTheme;
	}

	public String getSrcName() {
		return srcName;
	}

	public void setSrcName(String srcName) {
		this.srcName = srcName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public DataFormat getSrcFormat() {
		return srcFormat;
	}

	public void setSrcFormat(DataFormat srcFormat) {
		this.srcFormat = srcFormat;
	}

	public String getSupportedWrapper() {
		return supportedWrapper;
	}

	public void setSupportedWrapper(String supportedWrapper) {
		this.supportedWrapper = supportedWrapper;
	}

	public ArrayList<String> getBagOfWords() {
		return bagOfWords;
	}

	public void setBagOfWords(ArrayList<String> bagOfWords) {
		this.bagOfWords = bagOfWords;
	}

	public VisualParameters getVisualParam() {
		return visualParam;
	}

	public void setVisualParam(VisualParameters visualParam) {
		this.visualParam = visualParam;
	}

	public FrameParameters getInitParam() {
		return initParam;
	}

	public void setInitParam(FrameParameters initParam) {
		this.initParam = initParam;
	}

	public FrameParameters getFinalParam() {
		return finalParam;
	}

	public void setFinalParam(FrameParameters finalParam) {
		this.finalParam = finalParam;
	}

	public Wrapper getWrapper() {
		return wrapper;
	}

	public void setWrapper(Wrapper wrapper) {
		this.wrapper = wrapper;
	}

	public String getSyntax() {
		return syntax;
	}

	public void setSyntax(String syntax) {
		this.syntax = syntax;
	}

	public enum DataFormat {
		stream, visual, rest, file
	}
}
