package com.eventshop.eventshoplinux.GeoCode;


import com.eventshop.eventshoplinux.util.commonUtil.Config;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by aravindh on 6/10/15.
 */

public class AddressConverter {

    private static final String URL = "https://maps.googleapis.com/maps/api/geocode/json";


    public GoogleResponse convertToLatLong(String fullAddress) throws IOException {

        URL url = new URL(URL + "?address="
                    + URLEncoder.encode(fullAddress, "UTF-8") + "&sensor=false");
            // Open the Connection
        URLConnection conn = url.openConnection();

        InputStream in = conn.getInputStream();
        ObjectMapper mapper = new ObjectMapper();
     //   System.out.println("Input Stream"+in);
        GoogleResponse response = (GoogleResponse) mapper.readValue(in, GoogleResponse.class);
        in.close();
        return response;
    }

    public GoogleResponse convertFromLatLong(String latlongString) throws IOException {


        URL url = new URL(URL + "?latlng="
                + URLEncoder.encode(latlongString, "UTF-8") + "&key=" + Config.getProperty("googleAPIKey"));
        // Open the Connection
        URLConnection conn = url.openConnection();
        InputStream in = conn.getInputStream() ;
        ObjectMapper mapper = new ObjectMapper();
        GoogleResponse response = (GoogleResponse)mapper.readValue(in,GoogleResponse.class);
        in.close();
        return response;


    }



//    public static void main(String[] args) throws IOException {
//
//        GoogleResponse res = new AddressConverter().convertToLatLong("BREAK IN ROUTE");
//        if(res.getStatus().equals("OK"))
//        {
//            for(Result result : res.getResults())
//            {
//                System.out.println("Lattitude of address is :"  +result.getGeometry().getLocation().getLat());
//                System.out.println("Longitude of address is :" + result.getGeometry().getLocation().getLng());
//                System.out.println("Location is " + result.getGeometry().getLocation_type());
//            }
//        }
//        else
//        {
//            System.out.println(res.getStatus());
//        }
//
//        System.out.println("\n");
//        GoogleResponse res1 = new AddressConverter().convertFromLatLong("18.92038860,72.83013059999999");
//        if(res1.getStatus().equals("OK"))
//        {
//            for(Result result : res1.getResults())
//            {
//                System.out.println("address is :"  +result.getFormatted_address());
//            }
//        }
//        else
//        {
//            System.out.println(res1.getStatus());
//        }
//
//
//
//
//
//    }
}





//public class AddressConverter {
// /*
//  * Geocode request URL. Here see we are passing "json" it means we will get
//  * the output in JSON format. You can also pass "xml" instead of "json" for
//  * XML output. For XML output URL will be
//  * "http://maps.googleapis.com/maps/api/geocode/xml";
//  */
//
//    private static final String URL = "http://maps.googleapis.com/maps/api/geocode/json";
//
//    /*
//     * Here the fullAddress String is in format like
//     * "address,city,state,zipcode". Here address means "street number + route"
//     * .
//     */
//    public GoogleResponse convertToLatLong(String fullAddress) throws IOException {
//
//  /*
//   * Create an java.net.URL object by passing the request URL in
//   * constructor. Here you can see I am converting the fullAddress String
//   * in UTF-8 format. You will get Exception if you don't convert your
//   * address in UTF-8 format. Perhaps google loves UTF-8 format. :) In
//   * parameter we also need to pass "sensor" parameter. sensor (required
//   * parameter) — Indicates whether or not the geocoding request comes
//   * from a device with a location sensor. This value must be either true
//   * or false.
//   */
//        URL url = new URL(URL + "?address="
//                + URLEncoder.encode(fullAddress, "UTF-8") + "&sensor=false");
//        // Open the Connection
//        URLConnection conn = url.openConnection();
//
//        InputStream in = conn.getInputStream() ;
//        ObjectMapper mapper = new ObjectMapper();
//        GoogleResponse response = (GoogleResponse)mapper.readValue(in,GoogleResponse.class);
//        in.close();
//        return response;
//
//
//    }