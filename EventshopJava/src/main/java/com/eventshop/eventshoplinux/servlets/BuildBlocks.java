package com.eventshop.eventshoplinux.servlets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.prefs.Preferences;

/**
 * Created by aravindh on 5/8/15.
 */
public class BuildBlocks {
    private final static Logger LOGGER = LoggerFactory.getLogger(BuildBlocks.class);
    public static void build(){

        float nelat=50;
        float nelong=-66;
        float swlat=25;
        float swlong=-126;
        float latunit=0.2f;
        float  longunit=0.2f;
        int length=0,breath=0;
        ArrayList<String> blocks = new ArrayList<String>();
        for(float i=nelat;i>=swlat;i=i-latunit){
            for(float j=swlong; j<=nelong-longunit;j= j+longunit) {
                LOGGER.debug(i + ":" + j + "\t");
                blocks.add(i + ":" + j);
                breath++;
            }

            LOGGER.debug(""+breath);
            breath=0;
            length++;
        }
        LOGGER.debug("Length="+length);
        int cnt=0;

      /* for(String block: blocks){
            String[] latLong = block.split(":");
            String a=block;
            String b= latLong[0] + ":" + (Float.parseFloat(latLong[1])+longunit);
            String c = (Float.parseFloat(latLong[0])-latunit) + ":" + (Float.parseFloat(latLong[1])+longunit);
            String d = (Float.parseFloat(latLong[0])-latunit) + ":" + latLong[1];
            System.out.println("Coordinates: [" + a +" " + b + " "+ c+ " "+d +"]");
            cnt++;
        }*/

        LOGGER.debug(""+cnt);
    }

    public static void main(String args[]){
        build();
    }
}
