
package com.eventshop.eventshoplinux.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.List;

/**
 * Created by aravindh on 5/12/15.
 */
@JsonIgnoreProperties
public class Emage {
    public String theme;
    public long startTime;
    public long endTime;
    public String startTimeStr;
    public String endTimeStr;
    public double latUnit; // Resolution of latitude
    public double longUnit; // Resolution of longitude
    public double swLat;
    public double swLong;
    public double neLat;
    public double neLong;
    public int row = 0;
    public int col = 0;
    public double min = 0;
    public double max = 0;
    public double[] image;
    public String value;
    public List<String> colors;
    String mapEnabled;

    public Emage() {

    }

    public Emage(String theme, long startTime, long endTime, String startTimeStr, String endTimeStr,
                 double latUnit, double longUnit, double swLat, double swLong, double neLat, double neLong,
                 int row, int col, double min, double max, double[] image, String value, List<String> colors, String mapEnabled) {
        this.theme = theme;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startTimeStr = startTimeStr;
        this.endTimeStr = endTimeStr;
        this.latUnit = latUnit;
        this.longUnit = longUnit;
        this.swLat = swLat;
        this.swLong = swLong;
        this.neLat = neLat;
        this.neLong = neLong;
        this.row = row;
        this.col = col;
        this.min = min;
        this.max = max;
        this.image = image;
        this.value = value;
        this.colors = colors;
        this.mapEnabled = mapEnabled;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public double getLatUnit() {
        return latUnit;
    }

    public void setLatUnit(double latUnit) {
        this.latUnit = latUnit;
    }

    public double getLongUnit() {
        return longUnit;
    }

    public void setLongUnit(double longUnit) {
        this.longUnit = longUnit;
    }

    public double getSwLat() {
        return swLat;
    }

    public void setSwLat(double swLat) {
        this.swLat = swLat;
    }

    public double getSwLong() {
        return swLong;
    }

    public void setSwLong(double swLong) {
        this.swLong = swLong;
    }

    public double getNeLat() {
        return neLat;
    }

    public void setNeLat(double neLat) {
        this.neLat = neLat;
    }

    public double getNeLong() {
        return neLong;
    }

    public void setNeLong(double neLong) {
        this.neLong = neLong;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public double[] getImage() {
        return image;
    }

    public void setImage(double[] image) {
        this.image = image;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public String getMapEnabled() {
        return mapEnabled;
    }

    public void setMapEnabled(String mapEnabled) {
        this.mapEnabled = mapEnabled;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public String getStartTimeStr() {
        return startTimeStr;
    }

    public void setStartTimeStr(String startTimeStr) {
        this.startTimeStr = startTimeStr;
    }

    public String getEndTimeStr() {
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }


}
