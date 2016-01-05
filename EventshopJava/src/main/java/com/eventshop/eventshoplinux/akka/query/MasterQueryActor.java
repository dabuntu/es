package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.constant.Constant;
import com.eventshop.eventshoplinux.domain.common.FrameParameters;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by abhisekmohanty on 29/6/15.
 */
public class MasterQueryActor extends UntypedConsumerActor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MasterQueryActor.class);

    private final ActorRef filterQueryActor;
    private final ActorRef groupingQueryActor;
    private final ActorRef spatialCharQueryActor;
    private final ActorRef temporalCharQueryActor;
    private final ActorRef spatialPatternQyertActor;
    private final ActorRef aggregationQueryActor;
    private final ActorRef mongoQueryRouteProducerActor;
    private final ActorRef ruleRouteProducerActor;
    private final ActorRef temporalPatternQueryActor;
    private final ActorRef alertRouteProducer;

    private final FiniteDuration duration = Duration.create(100, TimeUnit.SECONDS);
    private final Timeout timeout = Timeout.durationToTimeout(duration);

    MasterQueryActor(ActorRef filterQueryActor, ActorRef groupingQueryActor, ActorRef spatialCharQueryActor
            , ActorRef spatialPatternQyertActor, ActorRef aggregationQueryActor, ActorRef temporalCharQueryActor
            , ActorRef mongoQueryRouteProducerActor, ActorRef temporalPatternQueryActor, ActorRef ruleRouteProducerActor
            , ActorRef alertRouteProducer) {
        this.filterQueryActor = filterQueryActor;
        this.groupingQueryActor = groupingQueryActor;
        this.spatialCharQueryActor = spatialCharQueryActor;
        this.spatialPatternQyertActor = spatialPatternQyertActor;
        this.aggregationQueryActor = aggregationQueryActor;
        this.temporalCharQueryActor = temporalCharQueryActor;
        this.mongoQueryRouteProducerActor = mongoQueryRouteProducerActor;
        this.temporalPatternQueryActor = mongoQueryRouteProducerActor;
        this.ruleRouteProducerActor = ruleRouteProducerActor;
        this.alertRouteProducer = alertRouteProducer;

    }

    public static Props props(final ActorRef filterQueryActor, final ActorRef groupingQueryActor
            , final ActorRef spatialCharQueryActor, final ActorRef spatialPatternQueryActor
            , final ActorRef aggregationQueryActor, final ActorRef temporalCharQueryActor
            , ActorRef mongoQueryRouteProducerActor, final ActorRef temporalPatternQueryActor
            , ActorRef ruleRouteProducerActor, ActorRef alertRouteProducer) {
        return Props.create(new Creator<MasterQueryActor>() {
            @Override
            public MasterQueryActor create() throws Exception {
                return new MasterQueryActor(filterQueryActor
                        , groupingQueryActor, spatialCharQueryActor, spatialPatternQueryActor, aggregationQueryActor
                        , temporalCharQueryActor, mongoQueryRouteProducerActor, temporalPatternQueryActor, ruleRouteProducerActor, alertRouteProducer);
            }
        });
    }

    @Override
    public String getEndpointUri() {
        return "direct:masterQueryActor2";
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ExecuteQuery) {
            final String masterQueryID = String.valueOf(((ExecuteQuery) message).getId());
            runQuery(masterQueryID);

            // execute Alerts
            alertRouteProducer.tell(masterQueryID, getSelf());

        }
    }

    public void runQuery(String masterQueryID) throws Exception {
        LOGGER.info("Start run Query");
        boolean sync = false;
        List<Emage> emageList = new ArrayList<Emage>();
        QueryListDAO queryDAO = new QueryListDAO();
        JsonParser parser = new JsonParser();
        JsonArray queryArr = (JsonArray) parser.parse(queryDAO.getQueryTree(Integer.parseInt(masterQueryID)).get(0));

        List<JsonObject> jsonObjectList = new ArrayList<JsonObject>();
        int size = queryArr.size();
        for (int i = 0; i < size; i++) {
            JsonObject query = queryArr.get(i).getAsJsonObject();
            LOGGER.info("QID : " + i+1 + " :::::::: " + query.toString());
            jsonObjectList.add(query);
        }
        Map<String, Emage> emageMap = new HashMap<String, Emage>();
        for (int i = 0; i < size; i++) {

            JsonObject query = queryArr.get(i).getAsJsonObject();
            if (query.get("dataSources") != null) {
                JsonArray sources = query.get("dataSources").getAsJsonArray();
                if (sources != null) {
                    Emage resultEmage = new Emage();
                    for (int j = 0; j < sources.size(); j++) {
                        String source = sources.get(j).getAsString();
                        LOGGER.info("qid is : " + source);
                        if (source.toLowerCase().startsWith("ds")) {
//                            sync = true;
                            String id = source.toLowerCase().replace("ds", "");
                            Emage emage = getEmage(id, query, masterQueryID);
                            emageList.add(emage);
                        } else if (source.toLowerCase().startsWith("q")) {
                            Emage emage = emageMap.get(source);
                            emageList.add(emage);
                        } else if (source.toLowerCase().startsWith("rule")) {
                            //
                            String id = source.toLowerCase().replace("rule", "");
                            Emage emage = executeRuleAndGetEmage(id, query, masterQueryID);
                            emageList.add(emage);
                        }

                    }
                    String patternType = query.get("patternType").getAsString().toUpperCase();
                    LOGGER.info("Pattern type is : " + patternType);


                    switch (patternType) {
                        case "FILTER": {
                            Future<Object> future = Patterns.ask(filterQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from filter: " + resultEmage.getTheme());
                            break;
                        }
                        case "GROUPING": {
                            Future<Object> future = Patterns.ask(groupingQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from grouping: " + resultEmage.getTheme());
                            break;
                        }
                        case "SPCHAR": {
                            Future<Object> future = Patterns.ask(spatialCharQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from spbo: " + resultEmage.getTheme());
                            break;
                        }
                        case "SPMATCHING" : {
                            Future<Object> future = Patterns.ask(spatialPatternQyertActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from spatial pattern: " + resultEmage.getTheme());
                            break;
                        }
                        case "AGGREGATION" : {

                            Future<Object> future = Patterns.ask(aggregationQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from Aggregation: " + resultEmage.getTheme());
                            break;

                        }
                        case "TPMATCHING" : {
                            Future<Object> future = Patterns.ask(filterQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from Temporal Pattern : " + resultEmage.getImage());
                            break;
                        }
                        case "TPCHAR": {
                            Future<Object> future = Patterns.ask(temporalCharQueryActor, new QueryActorMessage(masterQueryID, query, emageList), timeout);
                            resultEmage = (Emage) Await.result(future, duration);
                            LOGGER.info("Result emage theme from tempchar: " + resultEmage.getTheme());
                            break;
                        }
                    }

                    emageMap.put("q" + (i + 1), resultEmage);
                }
            }
        }
        // Once emage is created, create the file in the list. It will be the last emage created
        Emage finalEmage = emageMap.get("q" + size);
        if (finalEmage.getColors() == null) {
            finalEmage.setColors(new ArrayList<>());
        }
        if (finalEmage != null) {
            File file = new File(Config.getProperty("tempDir") + "/queries/" + "Q" + masterQueryID + ".json");
            if (!file.exists()) {
                file.createNewFile();
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(new File(Config.getProperty("tempDir") + "/queries/" + "Q" + masterQueryID + ".json"), finalEmage);

        }
    }

    private Emage executeRuleAndGetEmage(String id, JsonObject query, String masterQueryID) throws Exception {

        QueryListDAO queryListDAO = new QueryListDAO();
        FrameParameters frameParams = queryListDAO.getFrameParameterQry(Integer.parseInt(masterQueryID));

        if (query.get("boundingBox") != null) {
            String boundingBox = query.get("boundingBox").getAsString();
            StringTokenizer stringTokenizer = new StringTokenizer(boundingBox, ",");

            double swLat = Double.valueOf(stringTokenizer.nextToken());
            double swLon = Double.valueOf(stringTokenizer.nextToken());
            double neLat = Double.valueOf(stringTokenizer.nextToken());
            double neLon = Double.valueOf(stringTokenizer.nextToken());

            double latUnit = query.get("latitudeUnit").getAsDouble();
            double lonUnit = query.get("longitudeUnit").getAsDouble();
            Long timeWindow = (query.get("timeWindow").getAsLong() * 1000);

            long endTimeToCheck = 0;
            long timeToCheck = 0;

            if (frameParams.getTimeType().equalsIgnoreCase("0")) {
                endTimeToCheck = System.currentTimeMillis();
                timeToCheck = endTimeToCheck - timeWindow;
            } else if (frameParams.getTimeType().equalsIgnoreCase("1")) {
                timeToCheck = System.currentTimeMillis();
                endTimeToCheck = timeToCheck + timeWindow;
            }


            String spatial_wrapper = query.get("spatial_wrapper").getAsString();
            MongoQueryMessage mongoQueryMessage = new MongoQueryMessage(Integer.parseInt(id), timeToCheck, endTimeToCheck
                    , neLat, neLon, swLat, swLon, latUnit, lonUnit, spatial_wrapper);

            Future<Object> future = Patterns.ask(ruleRouteProducerActor, mongoQueryMessage, timeout);

            CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
            Emage emage = camelMessage.getBodyAs(Emage.class, getCamelContext());

            return emage;


        }
        return null;
    }


    public Emage getEmage(String id, JsonObject query, String masterQueryID) throws Exception {

        Emage emage = new Emage();
        String fileName = Config.getProperty("context") + "temp/ds/" + id + Constant.json;
        QueryListDAO queryListDAO = new QueryListDAO();
        FrameParameters frameParams = queryListDAO.getFrameParameterQry(Integer.parseInt(masterQueryID));


        if (query.get("boundingBox") != null) {
            String boundingBox = query.get("boundingBox").getAsString();
            StringTokenizer stringTokenizer = new StringTokenizer(boundingBox, ",");

            double swLat = Double.valueOf(stringTokenizer.nextToken());
            double swLon = Double.valueOf(stringTokenizer.nextToken());
            double neLat = Double.valueOf(stringTokenizer.nextToken());
            double neLon = Double.valueOf(stringTokenizer.nextToken());

            double latUnit = query.get("latitudeUnit").getAsDouble();
            double lonUnit = query.get("longitudeUnit").getAsDouble();
            Long timeWindow = (query.get("timeWindow").getAsLong() * 1000);

            long endTimeToCheck = 0;
            long timeToCheck = 0;

            if (frameParams.getTimeType().equalsIgnoreCase("0")) {
                endTimeToCheck = System.currentTimeMillis();
                timeToCheck = endTimeToCheck - timeWindow;
            } else if (frameParams.getTimeType().equalsIgnoreCase("1")) {
                timeToCheck = System.currentTimeMillis();
                endTimeToCheck = timeToCheck + timeWindow;
            }

            //Spatial Wrapper
            String spatial_wrapper = query.get("spatial_wrapper").getAsString();
            MongoQueryMessage mongoQueryMessage = new MongoQueryMessage(Integer.parseInt(id), timeToCheck, endTimeToCheck
                    , neLat, neLon, swLat, swLon, latUnit, lonUnit, spatial_wrapper);
            Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, mongoQueryMessage, timeout);

            CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
            emage = camelMessage.getBodyAs(Emage.class, getCamelContext());

            LOGGER.info(boundingBox);


        }

        return emage;
    }


    public static class ExecuteQuery {

        private int id;

        public ExecuteQuery(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

}
