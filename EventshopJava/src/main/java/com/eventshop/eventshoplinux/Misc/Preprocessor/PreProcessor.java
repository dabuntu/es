package com.eventshop.eventshoplinux.Misc.Preprocessor;

import com.eventshop.eventshoplinux.GeoCode.AddressConverter;
import com.eventshop.eventshoplinux.GeoCode.GoogleResponse;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.GeocodingApiRequest;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.apache.xpath.SourceTree;

import java.io.*;

/**
 * Created by aravindh on 10/15/15.
 */
public class PreProcessor {
    public static String getSeperateLatLong(String line){


        String returnLine =line.replace("\"","");
        returnLine=returnLine.replace("(","");
        returnLine=returnLine.replace(")","");
        return returnLine;
    }

    public static String populateValueField(String line, double value){
        String returnLine= line+","+value;
        return returnLine;
    }

    public static String getAddress(String line, String latLong) throws IOException {
        AddressConverter convert = new AddressConverter();
        GoogleResponse response = convert.convertFromLatLong(latLong);
        String address = response.getResults()[0].getFormatted_address().replace(",", "");
//        address = address.substring(address.length() - 9, address.length() - 3);
        return line+","+address;
    }
    public static String getZipCode(String line, String latLong) throws IOException{
        AddressConverter convert = new AddressConverter();
        GoogleResponse response = convert.convertFromLatLong(latLong);
        String address = response.getResults()[0].getFormatted_address().replace(",", "");
        address = address.substring(address.length() - 9, address.length() - 3);
        return line+","+address;
    }

    public static void main(String args[]) throws IOException {
        PrintWriter f0 = new PrintWriter(new FileWriter("/home/aravindh/Documents/ds_crime_data/vandalism1.csv"));
        try (BufferedReader br = new BufferedReader(new FileReader("/home/aravindh/Documents/ds_crime_data/vandalism.csv"))) {
            String line;
            int cnt=0;
            while ((line = br.readLine()) != null) {
                String[] lineSplit = line.split(",");
              //  lineSplit[0]
                GeoApiContext context = new GeoApiContext().setApiKey(Config.getProperty("googleAPIKey"));
//                if(cnt!=0){
//                    String returnedline=getSeperateLatLong(line);
//
//                    String[] returnedLineSplit = returnedline.split(",");
////
//                    try{
//                        if (returnedLineSplit.length==19) {
//                            //returnedline=populateValueField(returnedline,1);
//                            // if (returnedLineSplit[8].equals("BURGLARY FROM VEHICLE")) {
//                            //String latLng = returnedLineSplit[13] + "," + returnedLineSplit[14];
//                            // System.out.println(response.getResults()[0].getAddress_components().toString());
////                            returnedline = returnedline + "," + address;
////                            returnedline = returnedline + "," + "1";
//                            f0.println(returnedline);
//                            System.out.println(returnedline);
//                        } else {
//                            // System.out.println(returnedLineSplit[8]);
//                        }
//                        // }
//                    }catch(Exception ex){
//                        ex.printStackTrace();
//                    }

//                        GeoApiContext context = new GeoApiContext().setApiKey(Config.getProperty("googleAPIKey"));
                    // LatLng latLng = new LatLng(Double.parseDouble(returnedLineSplit[13]),Double.parseDouble(returnedLineSplit[14]));
//                        GeocodingApiRequest req = GeocodingApi.newRequest(context).latlng(latLng);
//                        req.await();

  //              }
                cnt++;
            }
            //         bw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
