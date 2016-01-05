package com.eventshop.eventshoplinux.model;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

/**
 * Created by abhisekmohanty on 4/8/15.
 */
@JsonIgnoreProperties
public class DataSource {

    //Columns for Datasource_Master
    public int ID;
    public String Name = null;
    public String Theme = null;
    public String Url = null;
    public String Format = null;
    public int User_Id;
    public Date Created_Date;
    public Date Updated_Date;
    public int Status;
    public String Syntax;

    //Columns for Datasource_Resolution
    public int Resolution_Id;
    public long Time_Window;
    public double Latitude_Unit;
    public double Longitude_Unit;
    public String boundingbox = null;
    public long Sync_Time;

    //Columns for Wrapper
    public int Wrapper_Id;
    public String Wrapper_Name = null;
    public String Wrapper_Key_Value = null;
    public String Bag_Of_Words = null;

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTheme() {
        return Theme;
    }

    public void setTheme(String theme) {
        Theme = theme;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getFormat() {
        return Format;
    }

    public void setFormat(String format) {
        Format = format;
    }

    public int getUser_Id() {
        return User_Id;
    }

    public void setUser_Id(int user_Id) {
        User_Id = user_Id;
    }

    public Date getCreated_Date() {
        return Created_Date;
    }

    public void setCreated_Date(Date created_Date) {
        Created_Date = created_Date;
    }

    public Date getUpdated_Date() {
        return Updated_Date;
    }

    public void setUpdated_Date(Date updated_Date) {
        Updated_Date = updated_Date;
    }

    public int getStatus() {
        return Status;
    }

    public void setStatus(int status) {
        Status = status;
    }

    public int getResolution_Id() {
        return Resolution_Id;
    }

    public void setResolution_Id(int resolution_Id) {
        Resolution_Id = resolution_Id;
    }

    public long getTime_Window() {
        return Time_Window;
    }

    public void setTime_Window(long time_Window) {
        Time_Window = time_Window;
    }

    public double getLatitude_Unit() {
        return Latitude_Unit;
    }

    public void setLatitude_Unit(double latitude_Unit) {
        Latitude_Unit = latitude_Unit;
    }

    public double getLongitude_Unit() {
        return Longitude_Unit;
    }

    public void setLongitude_Unit(double longitude_Unit) {
        Longitude_Unit = longitude_Unit;
    }

    public String getBoundingbox() {
        return boundingbox;
    }

    public void setBoundingbox(String boundingbox) {
        this.boundingbox = boundingbox;
    }

    public long getSync_Time() {
        return Sync_Time;
    }

    public void setSync_Time(long sync_Time) {
        Sync_Time = sync_Time;
    }

    public int getWrapper_Id() {
        return Wrapper_Id;
    }

    public void setWrapper_Id(int wrapper_Id) {
        Wrapper_Id = wrapper_Id;
    }

    public String getWrapper_Name() {
        return Wrapper_Name;
    }

    public void setWrapper_Name(String wrapper_Name) {
        Wrapper_Name = wrapper_Name;
    }

    public String getWrapper_Key_Value() {
        return Wrapper_Key_Value;
    }

    public void setWrapper_Key_Value(String wrapper_Key_Value) {
        Wrapper_Key_Value = wrapper_Key_Value;
    }

    public String getBag_Of_Words() {
        return Bag_Of_Words;
    }

    public void setBag_Of_Words(String bag_Of_Words) {
        Bag_Of_Words = bag_Of_Words;
    }


    public String getSyntax() {
        return Syntax;
    }

    public void setSyntax(String syntax) {
        Syntax = syntax;
    }
}
