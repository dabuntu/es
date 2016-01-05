package com.eventshop.eventshoplinux.domain.query;

public class QueryDTO {

	String qID; // Added by Bibhuti
	String queryName;
	String queryEsql;
	String timeType;
	double latitudeUnit;
	double longitudeUnit;
	String timeWindow;
	String queryStatus;
	String parmType;
	int qryCreatorId;
	String boundingBox;// Added by Bibhuti

	// for filter
	String dataSrcID;
	String maskMethod; // (values are map, textual, matrix);
	String coordrs[]; //
	String placeName;
	String filePath;
	String valRange[];
	String timeRange[];
	String normMode;
	String normVals[];

	// for aggregation
	String dataSources[];
	String values[];
	String scalarFirst;
	String aggOperator;
	String valueNorm;
	String normedRange[];

	// for grouping

	String split;
	String doColoring;
	String colorCodes[];
	String method; // (values are KMeans, Thresholds)
	String numGroup;
	String thresholds[];

	// spatial characterization
	String spCharoperator;

	// Spatial Matching

	String sizeNorm;
	String patternSrc;
	String numRows;
	String numCols;
	String patternType; // (values - gaussian, linear)
	SpatialMatchGaussianParam gaussParam;
	SpatialMatchLinearParam linearParam;

	// Temporal Characterization

	String tmplCharOperator;
	String tcTimeWindow;

	// Temporal Matching

	String dataDuration;
	String durationNorm;
	String patternSamplingRate;
	String patternDuration;
	TemporalMatchLinearParam templinearParam;
	ExponentialParameter expParam;
	PeriodicParameter periodicParam;

	// String valueNorm;
	// String patternSrc; (values - file, create)
	// String patternType; (values - linear, exponential, periodic)
	// String filePath; (duplicate)
	// added by sanjukta for registerservlet
	String status;
	int control;

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

	public String getQueryName() {
		return queryName;
	}

	public void setQueryName(String queryName) {
		this.queryName = queryName;
	}

	public String getQueryEsql() {
		return queryEsql;
	}

	public void setQueryEsql(String queryEsql) {
		this.queryEsql = queryEsql;
	}

	public double getLatitudeUnit() {
		return latitudeUnit;
	}

	public void setLatitudeUnit(double latitudeUnit) {
		this.latitudeUnit = latitudeUnit;
	}

	public double getLongitudeUnit() {
		return longitudeUnit;
	}

	public void setLongitudeUnit(double longitudeUnit) {
		this.longitudeUnit = longitudeUnit;
	}

	public String getQueryStatus() {
		return queryStatus;
	}

	public void setQueryStatus(String queryStatus) {
		this.queryStatus = queryStatus;
	}

	public String getDataSrcID() {
		return dataSrcID;
	}

	public void setDataSrcID(String dataSrcID) {
		this.dataSrcID = dataSrcID;
	}

	public String getMaskMethod() {
		return maskMethod;
	}

	public void setMaskMethod(String maskMethod) {
		this.maskMethod = maskMethod;
	}

	public String[] getCoordrs() {
		return coordrs;
	}

	public void setCoordrs(String[] coordrs) {
		this.coordrs = coordrs;
	}

	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String[] getValRange() {
		return valRange;
	}

	public void setValRange(String[] valRange) {
		this.valRange = valRange;
	}

	public String[] getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(String[] timeRange) {
		this.timeRange = timeRange;
	}

	public String getNormMode() {
		return normMode;
	}

	public void setNormMode(String normMode) {
		this.normMode = normMode;
	}

	public String[] getNormVals() {
		return normVals;
	}

	public void setNormVals(String[] normVals) {
		this.normVals = normVals;
	}

	public String[] getDataSources() {
		return dataSources;
	}

	public void setDataSources(String[] dataSources) {
		this.dataSources = dataSources;
	}

	public String[] getValues() {
		return values;
	}

	public void setValues(String[] values) {
		this.values = values;
	}

	public String getScalarFirst() {
		return scalarFirst;
	}

	public void setScalarFirst(String scalarFirst) {
		this.scalarFirst = scalarFirst;
	}

	public String getAggOperator() {
		return aggOperator;
	}

	public void setAggOperator(String aggOperator) {
		this.aggOperator = aggOperator;
	}

	public String getValueNorm() {
		return valueNorm;
	}

	public void setValueNorm(String valueNorm) {
		this.valueNorm = valueNorm;
	}

	public String[] getNormedRange() {
		return normedRange;
	}

	public void setNormedRange(String[] normedRange) {
		this.normedRange = normedRange;
	}

	public String getSplit() {
		return split;
	}

	public void setSplit(String split) {
		this.split = split;
	}

	public String getDoColoring() {
		return doColoring;
	}

	public void setDoColoring(String doColoring) {
		this.doColoring = doColoring;
	}

	public String[] getColorCodes() {
		return colorCodes;
	}

	public void setColorCodes(String[] colorCodes) {
		this.colorCodes = colorCodes;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getNumGroup() {
		return numGroup;
	}

	public void setNumGroup(String numGroup) {
		this.numGroup = numGroup;
	}

	public String[] getThresholds() {
		return thresholds;
	}

	public void setThresholds(String[] thresholds) {
		this.thresholds = thresholds;
	}

	public String getSpCharoperator() {
		return spCharoperator;
	}

	public void setSpCharoperator(String spCharoperator) {
		this.spCharoperator = spCharoperator;
	}

	public String getSizeNorm() {
		return sizeNorm;
	}

	public void setSizeNorm(String sizeNorm) {
		this.sizeNorm = sizeNorm;
	}

	public String getPatternSrc() {
		return patternSrc;
	}

	public void setPatternSrc(String patternSrc) {
		this.patternSrc = patternSrc;
	}

	public String getNumRows() {
		return numRows;
	}

	public void setNumRows(String numRows) {
		this.numRows = numRows;
	}

	public String getNumCols() {
		return numCols;
	}

	public void setNumCols(String numCols) {
		this.numCols = numCols;
	}

	public String getPatternType() {
		return patternType;
	}

	public void setPatternType(String patternType) {
		this.patternType = patternType;
	}

	public SpatialMatchGaussianParam getGaussParam() {
		return gaussParam;
	}

	public void setGaussParam(SpatialMatchGaussianParam gaussParam) {
		this.gaussParam = gaussParam;
	}

	public SpatialMatchLinearParam getLinearParam() {
		return linearParam;
	}

	public void setLinearParam(SpatialMatchLinearParam linearParam) {
		this.linearParam = linearParam;
	}

	public String getTmplCharOperator() {
		return tmplCharOperator;
	}

	public void setTmplCharOperator(String tmplCharOperator) {
		this.tmplCharOperator = tmplCharOperator;
	}

	public String getTcTimeWindow() {
		return tcTimeWindow;
	}

	public void setTcTimeWindow(String tcTimeWindow) {
		this.tcTimeWindow = tcTimeWindow;
	}

	public String getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(String timeWindow) {
		this.timeWindow = timeWindow;
	}

	public String getDataDuration() {
		return dataDuration;
	}

	public void setDataDuration(String dataDuration) {
		this.dataDuration = dataDuration;
	}

	public String getDurationNorm() {
		return durationNorm;
	}

	public void setDurationNorm(String durationNorm) {
		this.durationNorm = durationNorm;
	}

	public String getPatternSamplingRate() {
		return patternSamplingRate;
	}

	public void setPatternSamplingRate(String patternSamplingRate) {
		this.patternSamplingRate = patternSamplingRate;
	}

	public String getPatternDuration() {
		return patternDuration;
	}

	public void setPatternDuration(String patternDuration) {
		this.patternDuration = patternDuration;
	}

	public TemporalMatchLinearParam getTemplinearParam() {
		return templinearParam;
	}

	public void setTemplinearParam(TemporalMatchLinearParam templinearParam) {
		this.templinearParam = templinearParam;
	}

	public ExponentialParameter getExpParam() {
		return expParam;
	}

	public void setExpParam(ExponentialParameter expParam) {
		this.expParam = expParam;
	}

	public PeriodicParameter getPeriodicParam() {
		return periodicParam;
	}

	public void setPeriodicParam(PeriodicParameter periodicParam) {
		this.periodicParam = periodicParam;
	}

	public String getqID() {
		return qID;
	}

	public void setqID(String qID) {
		this.qID = qID;
	}

	public String getParmType() {
		return parmType;
	}

	public void setParmType(String parmType) {
		this.parmType = parmType;
	}

	public int getQryCreatorId() {
		return qryCreatorId;
	}

	public void setQryCreatorId(int qryCreatorId) {
		this.qryCreatorId = qryCreatorId;
	}

	public String getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(String boundingBox) {
		this.boundingBox = boundingBox;
	}

	public String getTimeType() {
		return timeType;
	}

	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}
}
