package com.eventshop.eventshoplinux.demo;


import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by abhisekmohanty on 9/11/15.
 */
@Path("krumbsService")
public class KrumbsDemoService {

//    public static void main(String[] args) {
//        KrumbsDemoService krumbsDemoService = new KrumbsDemoService();
//        krumbsDemoService.postData();
//    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/postData")
    public String postData () {

        String data = "[{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T02:53:17.181Z\",\"end_time\":\"2015-10-17T02:53:22.181Z\"},\"where\":{\"geo_location\":{\"latitude\":28.613152,\"longitude\":77.272167},\"revgeo_places\":[{\"latitude\":28.613152,\"longitude\":77.272167,\"name\":\"Commonwealth Games Village\",\"category\":\"residential\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":5,\"intent_expression_name\":\"Dirty Toilet\",\"intent_expression_display_name\":\"Clean This Now\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"restroom\",\"confidence\":0.85},{\"concept_name\":\"box\",\"confidence\":0.65}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/dirty-toilet.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Nightmare Toilets at Sports Complex\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:27:36.766Z\",\"end_time\":\"2015-10-17T03:27:41.766Z\"},\"where\":{\"geo_location\":{\"latitude\":28.677475,\"longitude\":77.229109},\"revgeo_places\":[{\"latitude\":28.677475,\"longitude\":77.229109,\"name\":\"Railway Colony\",\"category\":\"shops, business entities\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Water Pollution \",\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/water-pollution.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Trash and Plastic in Yamuna\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:09:17.427Z\",\"end_time\":\"2015-10-17T03:09:22.427Z\"},\"where\":{\"geo_location\":{\"latitude\":28.540984,\"longitude\":77.247923},\"revgeo_places\":[{\"latitude\":28.540984,\"longitude\":77.247923,\"name\":\"Chittaranjan Park\",\"category\":\"school,education institution\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Bad Roads\",\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/pothole-road.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"My Car is going to Burp Here\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:30:37.758Z\",\"end_time\":\"2015-10-17T03:30:42.758Z\"},\"where\":{\"geo_location\":{\"latitude\":28.677475,\"longitude\":77.229109},\"revgeo_places\":[{\"latitude\":28.677475,\"longitude\":77.229109,\"name\":\"Railway Colony\",\"category\":\"shops, business entities\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Water Pollution \",\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/water-pollution.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Polluted Yamuna in Delhi\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T00:45:21.784Z\",\"end_time\":\"2015-10-17T00:45:26.784Z\"},\"where\":{\"geo_location\":{\"latitude\":28.6335765,\"longitude\":77.308765},\"revgeo_places\":[{\"latitude\":28.633046,\"longitude\":77.304535,\"name\":\"Manglam Chowk, Pratapganj\",\"category\":\"residential\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":2,\"intent_expression_name\":\"Garbage\",\"intent_expression_display_name\":\"Yuck\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"bus\",\"confidence\":0.9},{\"concept_name\":\"street\",\"confidence\":0.88},{\"concept_name\":\"city\",\"confidence\":0.79}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/trash-bus.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Trash Littered in Delhi Streets\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:27:15.890Z\",\"end_time\":\"2015-10-17T03:27:20.890Z\"},\"where\":{\"geo_location\":{\"latitude\":28.540984,\"longitude\":77.247923},\"revgeo_places\":[{\"latitude\":28.540984,\"longitude\":77.247923,\"name\":\"Chittaranjan Park\",\"category\":\"school,education institution\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Bad Roads\",\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/pothole-road.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Dangerous Potholes in Main Thoroughfares\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:07:29.709Z\",\"end_time\":\"2015-10-17T03:07:34.709Z\"},\"where\":{\"geo_location\":{\"latitude\":28.540984,\"longitude\":77.247923},\"revgeo_places\":[{\"latitude\":28.540984,\"longitude\":77.247923,\"name\":\"Chittaranjan Park\",\"category\":\"school,education institution\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Bad Roads\",\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/pothole-road.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Daily commuters nightmare in Monsoon\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:23:40.184Z\",\"end_time\":\"2015-10-17T03:23:45.184Z\"},\"where\":{\"geo_location\":{\"latitude\":28.540984,\"longitude\":77.247923},\"revgeo_places\":[{\"latitude\":28.540984,\"longitude\":77.247923,\"name\":\"Chittaranjan Park\",\"category\":\"school,education institution\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Bad Roads\",\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/pothole-road.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Super unsafe roads in Delhi\"},{\"media\":[{\"when\":{\"start_time\":\"2015-10-17T03:11:09.964Z\",\"end_time\":\"2015-10-17T03:11:14.964Z\"},\"where\":{\"geo_location\":{\"latitude\":28.540984,\"longitude\":77.247923},\"revgeo_places\":[{\"latitude\":28.540984,\"longitude\":77.247923,\"name\":\"Chittaranjan Park\",\"category\":\"school,education institution\",\"city\":\"New Delhi\",\"state\":\"Delhi\",\"country\":\"India\"}]},\"why\":[{\"intent_expression_id\":3,\"intent_expression_name\":\"Bad Roads\",\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http://data.krumbs.io/pothole-road.jpg\"}},{\"media_source\":{\"default_src\":\"http://data.krumbs.io/1441956664773.3gp\"}}],\"theme\":\"CLEAN_INDIA\",\"title\":\"Please fix the Potholes\"}]";
        double lat = 12.971599;
        double lng = 77.594563;
        int radius = 15000;
        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(data);
            JSONArray array=(JSONArray)obj;
            JSONArray jsonData = new JSONArray();
            JSONObject result = new JSONObject();
            for (int i=0; i<array.size(); i++) {
                JSONObject time = getTime();
                JSONObject latLng = getLocation(lat, lng, radius);
                JSONObject jsonObject = (JSONObject) array.get(i);
                JSONArray media = (JSONArray) jsonObject.get("media");
                JSONObject media0 = (JSONObject) media.get(0);
                JSONObject when = (JSONObject) media0.get("when");
                when.put("start_time", time.get("start_time"));
                when.put("end_time", time.get("end_time"));
                JSONObject where = (JSONObject) media0.get("where");
                JSONObject geoLocation = (JSONObject) where.get("geo_location");
                geoLocation.put("latitude", latLng.get("lat"));
                geoLocation.put("longitude", latLng.get("lng"));

                jsonData.add(jsonObject);

            }
//            result.put("dsID", 12);
//            result.put("data", jsonData);
//            String escapedResult = JSONObject.escape(jsonData.toString());
            sendPost(jsonData.toString());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public JSONObject getTime() {
        JSONObject time = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        long t = cal.getTimeInMillis();
        long st = (long) (Math.random()*t);
        String start_time = dateFormat.format(st);

        Date date = new Date(st + (long)(Math.random()*1000000));
        String end_time = dateFormat.format(date);
        time.put("start_time", start_time);
        time.put("end_time", end_time);
        return time;
    }

    public JSONObject getLocation(double x0, double y0, int radius) {

        Random random = new Random();
        JSONObject jObj = new JSONObject();

        // Convert radius from meters to degrees
        double radiusInDegrees = radius / 111000f;

        double u = random.nextDouble();
        double v = random.nextDouble();
        double w = radiusInDegrees * Math.sqrt(u);
        double t = 2 * Math.PI * v;
        double x = w * Math.cos(t);
        double y = w * Math.sin(t);

        // Adjust the x-coordinate for the shrinking of the east-west distances
        double new_x = x / Math.cos(y0);

        double foundLatitude = new_x + x0;
        double foundLongitude = y + y0;
        jObj.put("lat", foundLatitude);
        jObj.put("lng", foundLongitude);
        return jObj;
    }


    // HTTP POST request
    private void sendPost(String data) {
//        System.out.println(data);
        JSONObject result = new JSONObject();
        result.put("dsID", 54);
        result.put("data", data);
        System.out.println(result.toJSONString());


        try {
            HttpClient httpclient = HttpClients.createDefault();
            HttpPost httppost = new HttpPost("http://54.254.138.95/eventshoplinux/webresources/populateDataService/populateListData");
            httppost.setEntity(new StringEntity(result.toString(), ContentType.create("application/json", Consts.UTF_8)));

            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                InputStream instream = entity.getContent();
                try {
                    System.out.println(instream.toString());
                } finally {
                    instream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
