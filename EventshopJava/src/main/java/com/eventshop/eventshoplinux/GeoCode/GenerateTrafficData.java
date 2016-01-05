package com.eventshop.eventshoplinux.GeoCode;

import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;

import java.io.*;

/**
 * Created by aravindh on 6/10/15.
 */
public class GenerateTrafficData {
    public static void main(String[] args) throws IOException {


        String csvFile = "/home/aravindh/Traffic/sample.file";
        BufferedReader br = new BufferedReader(new FileReader(csvFile));
        String line = "";
        String[] lineSplit = new String[3];
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
                "/home/aravindh/Traffic/sample_output2.file"), true));

        while ((line = br.readLine()) != null) {
           lineSplit=line.split(";");
           // System.out.println(line);
            if (lineSplit.length == 3) {
               try{
                   //GoogleResponse res = new AddressConverter().convertToLatLong(lineSplit[0]+", "+lineSplit[1]+", CA");
                   GeoApiContext context = new GeoApiContext().setApiKey(Config.getProperty("googleAPIKey"));
                   GeocodingResult[] results =  GeocodingApi.geocode(context,
                           lineSplit[0]+", "+lineSplit[1]+", CA").await();
             //      System.out.println("LatLong"+results[0].geometry.location.lat);
             //      System.out.println(results[0]);

                   for (int i=0;i<results.length;i++) {
                            bw.write(results[i].geometry.location.lat+","+results[i].geometry.location.lng+","+lineSplit[2]);
                            bw.newLine();
//                           System.out.print(results[i].geometry.location.lat+",");
//                           System.out.print(results[i].geometry.location.lng+",");
//                           System.out.print(lineSplit[2]);
//                           System.out.println();
                   }


               }catch(Exception ex){
                   ex.printStackTrace();
                  // System.out.println("Google didnot return lat long...");
               }
            }
        }
        bw.close();

    }

}
