package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.model.STT;
import com.eventshop.eventshoplinux.ruleEngine.ApplyRule;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import com.google.gson.JsonArray;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravindh on 8/28/15.
 */
public class RuleRoute extends RouteBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(RuleRoute.class);
    private final RuleDao ruleDAO = new RuleDao();
    @Override
    public void configure() throws Exception {
        from("direct:buildRules")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        int ruleID= exchange.getIn().getHeader("ruleId",Integer.class);


                        Rules rules =ruleDAO.getRules(ruleID);

                    }
                }).to("direct:runAlert")
        ;


        from("direct:applyandExecuteRule")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
//                        Rules rules = exchange.getIn().getBody(Rules.class);
//                        System.out.println("rules1:"+rules.toString());
                        exchange.getOut().setBody(exchange.getIn().getBody());
//                        exchange.getOut().setHeader("customEndpoint", rules.getCustomEndpoint());
//                        ApplyRule applyRule = new ApplyRule();
//
//                        StringBuffer result = applyRule.getAppliedRules(rules);
//                        exchange.getOut().setBody(result.toString());
//                        exchange.getOut().setHeader("datasource", new DataSourceManagementDAO()
//                                .getDataSource(Integer.parseInt(rules.getSource().replace("ds", ""))));

                    }
                })
//                .choice().when(header("customEndpoint").isNotNull())
//                .recipientList(header("customEndpoint"))
                    .to("direct:customEmage")
        ;

        from("direct:customEmage")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        String body = exchange.getIn().getBody(String.class);
                        JSONObject jsonObject = new JSONObject("{ \"list\" : " + body + "}");
                        JSONArray jsonArray = jsonObject.getJSONArray("list");
                        LOGGER.info("******************");
                        System.out.println(jsonArray.toString());
                        List<String> sttList = new ArrayList<String>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObj = jsonArray.getJSONObject(i);
//                            double value = 0;
//                            if (jsonObj.has("emotion")) {
//                                String emotion = jsonObj.getString("emotion");
//
//                                switch (emotion) {
//                                    case "Clean":
//                                        value = 100;
//                                        break;
//                                    case "Dirty":
//                                        value = 50;
//                                        break;
//                                    default:
//                                        value = 0;
//
//                                }
//                            }
                            ELocation eLocation = null;
                            JSONArray jsonArray1 = jsonObj.getJSONArray("data");
                            JSONObject jsonObj1 = jsonArray1.getJSONObject(0);

                            System.out.println(jsonObj1.has("media"));

                            JSONArray jsonArray2 = jsonObj1.getJSONArray("media");
                            JSONObject jsonObj2 = jsonArray2.getJSONObject(0);

                            System.out.println(jsonObj2.has("where"));



                            if (jsonObj.has("loc")) {
                                Double lat = jsonObj.getJSONObject("loc").getDouble("lat");
                                Double lon = jsonObj.getJSONObject("loc").getDouble("lon");
                                eLocation = new ELocation(lon, lat);


                            }else if(jsonObj2.has("where")){
                                Double lat = jsonObj2.getJSONObject("where").getJSONObject("geo_location").getDouble("latitude");
                                Double lon = jsonObj2.getJSONObject("where").getJSONObject("geo_location").getDouble("longitude");
                                eLocation = new ELocation(lon,lat);
                                System.out.println("Where.GeoLoc: "+eLocation.toString());
                            }
                            STT stt = new STT();
//                            stt.set_id(jsonObj.getLong("_id"));
                            stt.setValue(1);
                            stt.setLoc(eLocation);
                            sttList.add(stt.toString());
                        }
                        System.out.println("STT List:" + sttList);
                        exchange.getOut().setBody(sttList);

//                         List<String> resultList = exchange.getIn().getBody(ArrayList.class);
//                        for(String result: resultList){
//                            LOGGER.info("******************");
//                            LOGGER.info(result);
//                        }
                    }
                });
    }

    public static void main(String[] args) throws Exception{
        String a="[{\"_id\":{\"$oid\":\"564999c444ae0eac166f9af3\"},\"data\":[{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"restroom\",\"confidence\":0.85},{\"concept_name\":\"box\",\"confidence\":0.65}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/dirty-toilet.jpg\"},\"why\":[{\"intent_expression_name\":\"Dirty Toilet\",\"intent_expression_id\":5,\"intent_expression_display_name\":\"Clean This Now\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.982910172594133,\"longitude\":77.47509650279422},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.613152,\"name\":\"Commonwealth Games Village\",\"state\":\"Delhi\",\"category\":\"residential\",\"longitude\":77.272167}]},\"when\":{\"start_time\":\"2011-12-24 06:47:32\",\"end_time\":\"2011-12-24 06:48:23\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Nightmare Toilets at Sports Complex\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/water-pollution.jpg\"},\"why\":[{\"intent_expression_name\":\"Water Pollution \",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.889460264236144,\"longitude\":77.5446260528979},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.677475,\"name\":\"Railway Colony\",\"state\":\"Delhi\",\"category\":\"shops, business entities\",\"longitude\":77.229109}]},\"when\":{\"start_time\":\"1998-07-10 21:48:05\",\"end_time\":\"1998-07-10 22:00:56\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Trash and Plastic in Yamuna\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.868550455627846,\"longitude\":77.57893318334973},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"1982-05-21 10:58:59\",\"end_time\":\"1982-05-21 11:13:22\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"My Car is going to Burp Here\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/water-pollution.jpg\"},\"why\":[{\"intent_expression_name\":\"Water Pollution \",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.998946160423955,\"longitude\":77.49245059341688},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.677475,\"name\":\"Railway Colony\",\"state\":\"Delhi\",\"category\":\"shops, business entities\",\"longitude\":77.229109}]},\"when\":{\"start_time\":\"1997-08-08 08:02:46\",\"end_time\":\"1997-08-08 08:10:43\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Polluted Yamuna in Delhi\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"bus\",\"confidence\":0.9},{\"concept_name\":\"street\",\"confidence\":0.88},{\"concept_name\":\"city\",\"confidence\":0.79}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/trash-bus.jpg\"},\"why\":[{\"intent_expression_name\":\"Garbage\",\"intent_expression_id\":2,\"intent_expression_display_name\":\"Yuck\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.960549162833956,\"longitude\":77.72310745238138},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.633046,\"name\":\"Manglam Chowk, Pratapganj\",\"state\":\"Delhi\",\"category\":\"residential\",\"longitude\":77.304535}]},\"when\":{\"start_time\":\"1983-12-31 15:31:35\",\"end_time\":\"1983-12-31 15:33:43\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Trash Littered in Delhi Streets\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.036204762786443,\"longitude\":77.62968936664551},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2006-08-30 10:03:41\",\"end_time\":\"2006-08-30 10:14:20\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Dangerous Potholes in Main Thoroughfares\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.077704893436875,\"longitude\":77.51915336341756},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2015-08-12 18:23:41\",\"end_time\":\"2015-08-12 18:35:16\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Daily commuters nightmare in Monsoon\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.122310210838604,\"longitude\":77.64091716019614},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2000-10-25 12:43:04\",\"end_time\":\"2000-10-25 12:47:17\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Super unsafe roads in Delhi\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.780072659646924,\"longitude\":77.56792704004293},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2012-07-26 14:23:44\",\"end_time\":\"2012-07-26 14:30:36\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Please fix the Potholes\"}]},{\"_id\":{\"$oid\":\"5649a5da44ae4a0ebc4ef000\"},\"data\":[{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"restroom\",\"confidence\":0.85},{\"concept_name\":\"box\",\"confidence\":0.65}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/dirty-toilet.jpg\"},\"why\":[{\"intent_expression_name\":\"Dirty Toilet\",\"intent_expression_id\":5,\"intent_expression_display_name\":\"Clean This Now\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.925898020283306,\"longitude\":77.66700463337378},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.613152,\"name\":\"Commonwealth Games Village\",\"state\":\"Delhi\",\"category\":\"residential\",\"longitude\":77.272167}]},\"when\":{\"start_time\":\"2014-07-21 03:44:55\",\"end_time\":\"2014-07-21 03:55:07\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Nightmare Toilets at Sports Complex\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/water-pollution.jpg\"},\"why\":[{\"intent_expression_name\":\"Water Pollution \",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.757521818271137,\"longitude\":77.56573621040638},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.677475,\"name\":\"Railway Colony\",\"state\":\"Delhi\",\"category\":\"shops, business entities\",\"longitude\":77.229109}]},\"when\":{\"start_time\":\"2013-08-12 00:03:37\",\"end_time\":\"2013-08-12 00:10:45\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Trash and Plastic in Yamuna\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.05984452094148,\"longitude\":77.7097770786022},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"1981-03-18 05:19:06\",\"end_time\":\"1981-03-18 05:25:14\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"My Car is going to Burp Here\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"river\",\"confidence\":0.7},{\"concept_name\":\"person\",\"confidence\":0.6},{\"concept_name\":\"building\",\"confidence\":0.5}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/water-pollution.jpg\"},\"why\":[{\"intent_expression_name\":\"Water Pollution \",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Drainage is bad\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.871915605447564,\"longitude\":77.562733388592},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.677475,\"name\":\"Railway Colony\",\"state\":\"Delhi\",\"category\":\"shops, business entities\",\"longitude\":77.229109}]},\"when\":{\"start_time\":\"2011-12-13 06:48:46\",\"end_time\":\"2011-12-13 06:55:49\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Polluted Yamuna in Delhi\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"bus\",\"confidence\":0.9},{\"concept_name\":\"street\",\"confidence\":0.88},{\"concept_name\":\"city\",\"confidence\":0.79}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/trash-bus.jpg\"},\"why\":[{\"intent_expression_name\":\"Garbage\",\"intent_expression_id\":2,\"intent_expression_display_name\":\"Yuck\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.78056156101855,\"longitude\":77.57916439566182},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.633046,\"name\":\"Manglam Chowk, Pratapganj\",\"state\":\"Delhi\",\"category\":\"residential\",\"longitude\":77.304535}]},\"when\":{\"start_time\":\"2013-07-29 03:20:25\",\"end_time\":\"2013-07-29 03:35:14\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Trash Littered in Delhi Streets\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.047842355747383,\"longitude\":77.69438288632062},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2006-03-12 20:12:33\",\"end_time\":\"2006-03-12 20:17:18\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Dangerous Potholes in Main Thoroughfares\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":13.168738866336653,\"longitude\":77.55636126456027},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"1983-04-13 11:19:31\",\"end_time\":\"1983-04-13 11:30:36\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Daily commuters nightmare in Monsoon\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.975640817622173,\"longitude\":77.5993552840937},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"2014-02-13 07:56:52\",\"end_time\":\"2014-02-13 08:11:15\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Super unsafe roads in Delhi\"},{\"theme\":\"CLEAN_INDIA\",\"media\":[{\"what\":[{\"concept_name\":\"car\",\"confidence\":0.97},{\"concept_name\":\"street\",\"confidence\":0.95},{\"concept_name\":\"city\",\"confidence\":0.85},{\"concept_name\":\"water\",\"confidence\":0.76}],\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/pothole-road.jpg\"},\"why\":[{\"intent_expression_name\":\"Bad Roads\",\"intent_expression_id\":3,\"intent_expression_display_name\":\"Watch out!\",\"context_name\":\"CLEAN_INDIA\"}],\"where\":{\"geo_location\":{\"latitude\":12.951102541611325,\"longitude\":77.66447401438792},\"revgeo_places\":[{\"country\":\"India\",\"city\":\"New Delhi\",\"latitude\":28.540984,\"name\":\"Chittaranjan Park\",\"state\":\"Delhi\",\"category\":\"school,education institution\",\"longitude\":77.247923}]},\"when\":{\"start_time\":\"1984-07-27 05:54:05\",\"end_time\":\"1984-07-27 06:08:42\"}},{\"media_source\":{\"default_src\":\"http:\\/\\/data.krumbs.io\\/1441956664773.3gp\"}}],\"title\":\"Please fix the Potholes\"}]}]\n";
        JSONObject jsonObject = new JSONObject("{ \"list\" : " + a + "}");
        JSONArray jsonArray = jsonObject.getJSONArray("list");
        LOGGER.info("******************");
        System.out.println(jsonArray.toString());
        List<String> sttList = new ArrayList<String>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObj = jsonArray.getJSONObject(i);
//                            double value = 0;
//                            if (jsonObj.has("emotion")) {
//                                String emotion = jsonObj.getString("emotion");
//
//                                switch (emotion) {
//                                    case "Clean":
//                                        value = 100;
//                                        break;
//                                    case "Dirty":
//                                        value = 50;
//                                        break;
//                                    default:
//                                        value = 0;
//
//                                }
//                            }
            ELocation eLocation = null;
//            System.out.println(jsonObj.has("data"));

            JSONArray jsonArray1 = jsonObj.getJSONArray("data");
            JSONObject jsonObj1 = jsonArray1.getJSONObject(0);

            System.out.println(jsonObj1.has("media"));

            JSONArray jsonArray2 = jsonObj1.getJSONArray("media");
            JSONObject jsonObj2 = jsonArray2.getJSONObject(0);

            System.out.println(jsonObj2.has("where"));



            if (jsonObj.has("loc")) {
                Double lat = jsonObj.getJSONObject("loc").getDouble("lat");
                Double lon = jsonObj.getJSONObject("loc").getDouble("lon");
                eLocation = new ELocation(lon, lat);


            }else if(jsonObj2.has("where")){
                Double lat = jsonObj2.getJSONObject("where").getJSONObject("geo_location").getDouble("latitude");
                Double lon = jsonObj2.getJSONObject("where").getJSONObject("geo_location").getDouble("longitude");
                eLocation = new ELocation(lon,lat);
                System.out.println("Where.GeoLoc: "+eLocation.toString());
            }
            STT stt = new STT();
//                            stt.set_id(jsonObj.getLong("_id"));
            stt.setValue(1);
            stt.setLoc(eLocation);
            sttList.add(stt.toString());
        }
        System.out.println("STT List:"+ sttList);
    }

}
