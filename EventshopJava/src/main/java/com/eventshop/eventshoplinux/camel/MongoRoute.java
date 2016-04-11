package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.DAO.rule.RuleDao;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.model.MongoResponse;
import com.eventshop.eventshoplinux.ruleEngine.ApplyRule;
import com.eventshop.eventshoplinux.ruleEngine.Rule;
import com.eventshop.eventshoplinux.ruleEngine.Rules;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by aravindh on 5/8/15.
 */
public class MongoRoute extends RouteBuilder {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoRoute.class);

    @Override
    public void configure() throws Exception {

        from("direct:commonQueryMongo")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
                        LOGGER.info(dataSource.toString());


                        String timeType = dataSource.getInitParam().getTimeType();

                        double nelat = dataSource.getInitParam().getNeLat();
                        double nelong = dataSource.getInitParam().getNeLong();
                        double swlat = dataSource.getInitParam().getSwLat();
                        double swlong = dataSource.getInitParam().getSwLong();
                        double latUnit = dataSource.getInitParam().getLatUnit();
                        double longUnit = dataSource.getInitParam().getLongUnit();


                        long timeWindow = dataSource.getInitParam().getTimeWindow();
                        long endTimeToCheck = 0;
                        long timeToCheck = 0;

                        if (timeType.equalsIgnoreCase("0")) {
                            endTimeToCheck = System.currentTimeMillis();
                            timeToCheck = endTimeToCheck - timeWindow;
                        } else if (timeType.equalsIgnoreCase("1")) {
                            timeToCheck = System.currentTimeMillis();
                            endTimeToCheck = timeToCheck + timeWindow;
                        }

                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        String sptlWrpr = jObj.get("spatial_wrapper").getAsString();

                        exchange.getOut().setHeader("endTimeToCheck", endTimeToCheck);
                        exchange.getOut().setHeader("dataSource", dataSource);
                        exchange.getOut().setHeader("datasource", dataSource);
                        exchange.getOut().setHeader("timeToCheck", timeToCheck);
                        exchange.getOut().setHeader("nelat", nelat);
                        exchange.getOut().setHeader("nelong", nelong);
                        exchange.getOut().setHeader("swlat", swlat);
                        exchange.getOut().setHeader("swlong", swlong);
                        exchange.getOut().setHeader("latUnit", latUnit);
                        exchange.getOut().setHeader("longUnit", longUnit);
                        exchange.getOut().setHeader("spatial_wrapper", sptlWrpr);
                    }
                })
                .to("direct:commonQuery")
        ;

        from("direct:commonQuery")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        exchange.getOut().setHeader("c_id", exchange.getProperty(Exchange.CORRELATION_ID));

                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        long endTimeToCheck = exchange.getIn().getHeader("endTimeToCheck", Long.class);
                        long startTimeToCheck = exchange.getIn().getHeader("timeToCheck", Long.class);
                        double nelat = exchange.getIn().getHeader("nelat", Double.class);
                        double nelong = exchange.getIn().getHeader("nelong", Double.class);
                        double swlat = exchange.getIn().getHeader("swlat", Double.class);
                        double swlong = exchange.getIn().getHeader("swlong", Double.class);

                        LOGGER.info("Time intervals : {} , {} ", startTimeToCheck, endTimeToCheck);
//                        JsonParser jsonParser = new JsonParser();
//                        JsonObject dsQuery = (JsonObject) jsonParser.parse(ds.getInitParam().getDsQuery());
//                        String query = dsQuery.get("query").getAsString();
//                        JsonObject queryParams = dsQuery.get("queryParams").getAsJsonObject();

//                        String query = ds.getInitParam().getDsQuery();
//
//                        query = query.replace("$swlat", "" + ds.getInitParam().getSwLat());
//                        query = query.replace("$swlong", "" + ds.getInitParam().getSwLong());
//                        query = query.replace("$nelat", "" + ds.getInitParam().getNeLat());
//                        query = query.replace("$nelong", "" + ds.getInitParam().getNeLong());
//                        query = query.replace("$swlat", "" + startTimeToCheck);
//                        query = query.replace("$swlat", "" + endTimeToCheck);

                        String query = "{ $and: [ {loc: { $geoWithin: { $box:  [ [ " + swlong + ", " +
                                +swlat + "], [ " + nelong + ", "
                                + nelat + " ] ]}}}, {timestamp : { $gt : " + (startTimeToCheck) + ", $lt : " + (endTimeToCheck) + " }} ] }";


//                        Map<String, Object> attributes = new HashMap<String, Object>();
//                        Set<Map.Entry<String, JsonElement>> entrySet = queryParams.entrySet();
//                        for(Map.Entry<String,JsonElement> entry : entrySet){
//                            System.out.println(entry.getKey());
//                            System.out.println(entry.getValue());
//                            query=query.replace(entry.getKey(),entry.getValue().toString());
//
//                        }
//
//
//                        query=query.replace("startTimeToCheck",""+startTimeToCheck);
//                        query=query.replace("endTimeToCheck",""+endTimeToCheck);


                        LOGGER.info("Query : {}", query);

                        String dsId = ds.getSrcID();
                        String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + dsId + "&operation=findAll";
                        exchange.getOut().setHeader("mPath", mongoPath);

                        exchange.getOut().setBody(query);
                        LOGGER.info(mongoPath);
                        LOGGER.info("Query Start TIme:" + System.currentTimeMillis());

                    }
                })
                .convertBodyTo(String.class)
                .recipientList(header("mPath"))
                .to("direct:applySpatialWrapper");

        from("direct:applyRule")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        //Get the rule from DB

                        MongoQueryMessage mongoQueryMessage = exchange.getIn().getBody(MongoQueryMessage.class);

                        exchange.getOut().setHeader("endTimeToCheck", mongoQueryMessage.getEndTimeToFilter());
                        exchange.getOut().setHeader("timeToCheck", mongoQueryMessage.getTimeToFilter());
                        exchange.getOut().setHeader("nelat", mongoQueryMessage.getNelat());
                        exchange.getOut().setHeader("nelong", mongoQueryMessage.getNelong());
                        exchange.getOut().setHeader("swlat", mongoQueryMessage.getSwlat());
                        exchange.getOut().setHeader("swlong", mongoQueryMessage.getSwlong());
                        exchange.getOut().setHeader("latUnit", mongoQueryMessage.getLatUnit());
                        exchange.getOut().setHeader("longUnit", mongoQueryMessage.getLonUnit());
                        exchange.getOut().setHeader("spatial_wrapper", mongoQueryMessage.getSpatial_wrapper());

                        RuleDao ruleDao = new RuleDao();
                        Rules rules = ruleDao.getRules(mongoQueryMessage.getDataSourceID());
                        System.out.println("RuleId: "+ rules.getRuleID());
                        System.out.println("Rules:-1 : " + rules.toString());


                        DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
                        String source = rules.getSource();
                        source = source.replace("ds", "");

                        exchange.getOut().setHeader("datasource", dataSourceManagementDAO.getDataSource(Integer.parseInt(source)));

                        StringTokenizer st;

                        //Process the Rule and create the Emage
                        Mongo mongoConn = new Mongo(Config.getProperty("mongoHost"), Integer.parseInt(Config.getProperty("mongoPort")));
                        DB mongoDb = mongoConn.getDB("events");
                        DBCollection collection = mongoDb.getCollection(rules.getSource());
                        // Building the query parameters from Rules
                        QueryBuilder query = new QueryBuilder();
                        List<Rule> rulesList = rules.getRules();//TODO
                        System.out.println("Rule List: "+rulesList.toString());
                        if (rulesList == null || rulesList.isEmpty()) {
                            rulesList = new ArrayList<Rule>();
                        }

                        // Adding the bounding box and timestamp Rule by default as Eventshop is always considered Geo-Spatial
                        Rule geoLocationRule = new Rule("loc", "coordinates",
                                String.valueOf(mongoQueryMessage.getSwlong()) + "," +
                                        String.valueOf(mongoQueryMessage.getSwlat()) + "," +
                                        String.valueOf(mongoQueryMessage.getNelong()) + "," +
                                        String.valueOf(mongoQueryMessage.getNelat()));

                        Rule startTimeStampRule = new Rule("timestamp", ">", String.valueOf(mongoQueryMessage.getTimeToFilter()));
                        Rule endTimeStampRule = new Rule("timestamp", "<", String.valueOf(mongoQueryMessage.getEndTimeToFilter()));

//                        rulesList.add(geoLocationRule);
//                        rulesList.add(startTimeStampRule);
//                        rulesList.add(endTimeStampRule);

                        ApplyRule ar = new ApplyRule();
                        StringBuffer result = ar.getAppliedRules(rules);

//                        Added to test writing the result to a file
                        File file = new File("result.json");

                        // if file doesnt exists, then create it
                        if (!file.exists()) {
                            file.createNewFile();
                        }

                        FileWriter fw = new FileWriter(file.getAbsoluteFile());
                        BufferedWriter bw = new BufferedWriter(fw);
                        bw.write(String.valueOf(String.valueOf(result)));
                        bw.close();


                        System.out.println("rule0:"+result);
                        exchange.getOut().setBody(String.valueOf(result));
                        exchange.getOut().setHeader("createEmageFile", false);
                    }
                })
//                .to("direct:applyandExecuteRule")
                .to("direct:applySpatialWrapper");

        from("direct:applySpatialWrapper")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        LOGGER.info("Query End Time:");
                        LOGGER.info("{}", System.currentTimeMillis());
                        LOGGER.info("{}", new Date());
                        final String body = exchange.getIn().getBody(String.class);
                        System.out.println("STT:"+body);
                        JSONObject obj = new JSONObject("{ \"list\" : " + body + "}");
//                        LOGGER.info("{ \"list \" :" + exchange.getIn().getBody(String.class) + "}");
                        List<MongoResponse> list = new ArrayList<MongoResponse>();
                        JSONArray array = obj.getJSONArray("list");
                        try {
                            for (int i = 0; i < array.length(); i++) {
                                MongoResponse mongoResponse = new MongoResponse();
                                mongoResponse.setValue(array.getJSONObject(i).getDouble("value"));
                                ELocation loc = new ELocation();
                                loc.setLon(array.getJSONObject(i).getJSONObject("loc").getDouble("lon"));
                                loc.setLat(array.getJSONObject(i).getJSONObject("loc").getDouble("lat"));
                                mongoResponse.setLoc(loc);
                                list.add(mongoResponse);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
                        double latUnit = exchange.getIn().getHeader("latUnit", Double.class);
                        double longUnit = exchange.getIn().getHeader("longUnit", Double.class);
                        double nelat = exchange.getIn().getHeader("nelat", Double.class);
                        double nelong = exchange.getIn().getHeader("nelong", Double.class);
                        double swlat = exchange.getIn().getHeader("swlat", Double.class);
                        double swlong = exchange.getIn().getHeader("swlong", Double.class);


                        long rows = (long) ((Math.ceil((nelat - swlat) / latUnit)));
                        long cols = (long) ((Math.ceil((nelong - swlong) / longUnit) * longUnit) / longUnit);
                        LOGGER.info("Row:" + rows);
                        LOGGER.info("Col:" + cols);
                        LOGGER.info("nelat:" + nelat);
                        LOGGER.info("nelong:" + nelong);
                        LOGGER.info("swlat:" + swlat);
                        LOGGER.info("swlong:" + swlong);
                        ArrayList<ArrayList<Double>> grid = new ArrayList<ArrayList<Double>>();

                        double lng;
                        double lat;
                        LOGGER.info("Size of data: " + list.size());
                        for (int i = 0; i < (rows * cols); i++) {
                            grid.add(new ArrayList<Double>());
                        }

                        for (int i = 0; i < list.size(); i++) {

                            lng = list.get(i).getLoc().getLon();
                            lat = list.get(i).getLoc().getLat();

                            //                            Please do not remove the below commented lines until the results are proved.
//
//                            LOGGER.debug("Lat:Long ["+lat+":"+lng +" ]");;
//                            LOGGER.debug("Calculated Lat : ["+((Math.ceil(lat/latUnit))*latUnit) +"]");
//                            LOGGER.debug("Calculated Long : ["+ ((Math.floor(lng/longUnit))*longUnit)+"]");
//                            LOGGER.debug("RowNumber Identified: "+(((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1));
//                            LOGGER.debug("ColNumber Identified: "+ (((Math.floor(lng/longUnit))*longUnit)-swlong)/longUnit);
//                            LOGGER.debug("Array Element to insert : "+((int)((((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1)*cols)+(int)((((Math.floor(lng/longUnit))*longUnit)-nelong)/longUnit)));
//                            LOGGER.debug("Value Inserted:" + list.get(i).getValue());
//
//                            LOGGER.debug("1. "+Math.ceil(lat/latUnit));
//                            LOGGER.debug("2. "+((Math.ceil(lat/latUnit))*latUnit));
//                            LOGGER.debug("3. "+(nelat-((Math.ceil(lat/latUnit))*latUnit)));
//                            LOGGER.debug("4. "+((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit));
//                            LOGGER.debug("5. "+(((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1));
//                            LOGGER.debug("6. "+((((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1)*cols));
//                            LOGGER.debug("7. "+(int)((((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1)*cols));
//                            LOGGER.debug("8. "+ (Math.floor(lng/longUnit)) );
//                            LOGGER.debug("9. "+ ((Math.floor(lng/longUnit))*longUnit));
//                            LOGGER.debug("10. "+ (((Math.floor(lng/longUnit))*longUnit)-swlong));
//                            LOGGER.debug("11. "+((((Math.floor(lng/longUnit))*longUnit)-swlong)/longUnit));
//                            LOGGER.debug("12. "+(int)((((Math.floor(lng/longUnit))*longUnit)-swlong)/longUnit) );
//                            LOGGER.debug("13. "+((int)((((nelat-(((Math.ceil(lat/latUnit))*latUnit)+(nelat%latUnit)))/latUnit)-1)*cols)+(int)(((((Math.floor(lng/longUnit))*longUnit)-swlong)-(swlong%longUnit))/longUnit)));
//                            ((((nelat-((Math.ceil(lat/latUnit))*latUnit))/latUnit)-1)*cols);


//                            //RowNumber * Cols + colNumber
                            int point = ((int) ((((nelat - (((Math.ceil(lat / latUnit)) * latUnit) + (nelat % latUnit))) / latUnit)) * cols) + (int) (((((Math.floor(lng / longUnit)) * longUnit) - swlong) - (swlong % longUnit)) / longUnit));
                            if (point > 0 && point < (rows * cols)) {
                                grid.get(point).add(list.get(i).getValue());
                            }


                        }
                        String operation = exchange.getIn().getHeader("spatial_wrapper", String.class);

                        LOGGER.info("Operator: " + operation);
                        List<Double> outputList = new ArrayList<Double>();

                        if (list.size() > 0) {
                            if (operation.equalsIgnoreCase("sum")) {
                                for (int i = 0; i < (rows * cols); i++) {
                                    double val = 0;
                                    for (int j = 0; j < grid.get(i).size(); j++) {
                                        val += (grid.get(i).get(j));
                                    }
                                    outputList.add(val);
                                }
                            } else if (operation.equalsIgnoreCase("min")) {


                                for (int i = 0; i < (rows * cols); i++) {
                                    double val = 0;
                                    double min = Double.MAX_VALUE;
                                    for (int j = 0; j < grid.get(i).size(); j++) {
                                        if (grid.get(i).get(j) < min) {
                                            val = grid.get(i).get(j);
                                            min = val;
                                        }
                                    }
                                    outputList.add(val);
                                }

                            } else if (operation.equalsIgnoreCase("max")) {


                                for (int i = 0; i < (rows * cols); i++) {
                                    double val = 0;
                                    double max = (-Double.MAX_VALUE) + 1;
                                    for (int j = 0; j < grid.get(i).size(); j++) {
                                        if (grid.get(i).get(j) > max) {
                                            val = grid.get(i).get(j);
                                            max = val;
                                        }
                                    }
                                    outputList.add(val);
                                }

                            } else if (operation.equalsIgnoreCase("avg")) {
                                for (int i = 0; i < (rows * cols); i++) {
                                    double val = 0;
                                    double avg = 0;
                                    for (int j = 0; j < grid.get(i).size(); j++) {
                                        val += (grid.get(i).get(j));
                                    }
                                    avg = val / grid.get(i).size();
                                    outputList.add(avg);
                                }
                            } else if (operation.equalsIgnoreCase("count")) {
                                for (int i = 0; i < (rows * cols); i++) {
                                    double count = grid.get(i).size();
                                    outputList.add(count);
                                }
                            } else if (operation.equalsIgnoreCase("majority")) {
                                for (int i = 0; i < (rows * cols); i++) {
                                    double maj = getMajority(grid.get(i));
                                    outputList.add(maj);
                                }
                            } else if (operation.equalsIgnoreCase("most_freq")) {
                                for (int i = 0; i < (rows * cols); i++) {
                                    double mostFreq = getMostFreq(grid.get(i));
                                    outputList.add(mostFreq);
                                }
                            }
                        } else {
                            LOGGER.info("List size empty...");
                            for (int i = 0; i < (rows * cols); i++) {
                                outputList.add(0.0);
                            }
                        }
                        exchange.getOut().setHeader("numOfRows", rows);
                        exchange.getOut().setHeader("numOfCols", cols);
                        exchange.getOut().setBody(outputList, List.class);
                        LOGGER.info("End Time:");
                        LOGGER.info("{}", System.currentTimeMillis());
                        LOGGER.info("{}", new Date());
                    }
                })
                .to("direct:emageBuilder");
    }


    public double getMajority(List<Double> list) {
        double pop = 0;
        if (!list.isEmpty()) {
            pop = list.get(0);
            int count = 1;
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) == pop) {
                    count++;
                    if (count > list.size() / 2)
                        break;
                } else {
                    if (i + count < list.size()
                            && list.get(i).equals(list.get(i + count))) {
                        pop = list.get(i);
                        i = i + count;
                        count++;
                    }
                }
            }
            if (count <= list.size() / 2)
                pop = 0;
        }
        return pop;
    }
    public double getMostFreq(List<Double> list) {
        double pop = 0;
        if (!list.isEmpty()) {
            pop = list.get(0);
            int count = 1;
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i) == pop) {
                    count++;
                } else {
                    if (i + count < list.size()
                            && list.get(i).equals(list.get(i + count))) {
                        pop = list.get(i);
                        i = i + count;
                        count++;
                    }
                }
            }
        }
        return pop;
    }
}