package com.eventshop.eventshoplinux.test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by abhisekmohanty on 23/7/15.
 */

@Path("/restTest")
public class RestTest {

    public static void main(String[] args) {
//        JsonArray queryParams = new JsonArray();
//        JsonObject queryParamObj = new JsonObject();
//        queryParamObj.addProperty("lat", "25");
//        queryParamObj.addProperty("lon", "36");
//        queryParams.add(queryParamObj);
//        JsonObject obj = new JsonObject();
//        obj.addProperty("baseUrl", "http://open.live.bbc.co.uk/weather/feeds/en/2643123/3dayforecast.rss");
//        obj.add("queryParams", queryParams);
//        RestTest restTest = new RestTest();
//        restTest.myTest(obj);

        try {
            String f = "src/main/webapp/weatherList.xml";
            String ury = "http://open.live.bbc.co.uk/weather/feeds/en/2643123/3dayforecast.rss";
            File inputFile = new File("src/main/webapp/weatherList.xml");
            DocumentBuilderFactory dbFactory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();
//            System.out.println("Root element :"
//                    + doc.getDocumentElement().getNodeName());
//            NodeList nList = doc.getElementsByTagName("item");
            XPath xPath =  XPathFactory.newInstance().newXPath();
            System.out.println("----------------------------");

            String lat_res = null;
            String expression = "/cities/list/item";
            NodeList nList = (NodeList) xPath.compile(expression).evaluate(doc, XPathConstants.NODESET);

            for (int temp = 0; temp < nList.getLength(); temp++) {
//                Node nNode = nList.item(temp);
//                System.out.println("\nCurrent Element :"
//                        + nNode.getNodeName());
//                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
//                    Element eElement = (Element) nNode;
//                    System.out.println("City Name : "
//                            + eElement
//                            .getElementsByTagName("city")
//                            .item(0).getAttributes().getNamedItem("name"));
//                    System.out.println("Student roll no : "
//                            + eElement.getAttributeNS("city", "name"));
//                    System.out.println("City Name : "
//                            + eElement
//                            .getElementsByTagName("city")
//                            .item(0)
//                            .getTextContent());
//                    System.out.println("title : "
//                            + eElement
//                            .getElementsByTagName("title")
//                            .item(0)
//                            .getTextContent());
//                    System.out.println("pubDate : "
//                            + eElement
//                            .getElementsByTagName("pubDate")
//                            .item(0)
//                            .getTextContent());
//                    System.out.println("georss:point : "
//                            + eElement
//                            .getElementsByTagName("georss:point")
//                            .item(0)
//                            .getTextContent());
//                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/myTest")
    public String myTest(JsonObject restParams) {

        String baseUrl = restParams.get("baseUrl").getAsString();
//        System.out.println(restParams.toString());
//        JsonObject queryParams = (JsonObject) restParams.getAsJsonArray("queryParams").get(0);
//        System.out.println(restParams.getAsJsonArray("queryParams").get(0));
//
//        Set<Map.Entry<String,JsonElement>> entrySet=queryParams.entrySet();
//        for(Map.Entry<String,JsonElement> entry : entrySet){
//            System.out.println(entry.getKey()+" : " + entry.getValue());
//            baseUrl = baseUrl + entry.getKey() + "=" + entry.getValue().getAsString().replaceAll("\"", "") + "&" ;
//        }

        try {
            sendGet(baseUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public void sendGet(String url) throws Exception {

//        String url = "http://api.openweathermap.org/data/2.5/weather?lat=29&lon=57";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
//        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
    }

}
