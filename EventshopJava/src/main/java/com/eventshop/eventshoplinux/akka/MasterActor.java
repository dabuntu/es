package com.eventshop.eventshoplinux.akka;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.eventshop.eventshoplinux.DAO.query.QueryListDAO;
import com.eventshop.eventshoplinux.model.QueryMessage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by nandhiniv on 5/28/15.
 */

/**
 * This actor is used to parse the query tree and execute the sub queries and create the final JSON outpur.
 */
public class MasterActor extends UntypedConsumerActor {

    private final static Logger LOGGER = LoggerFactory.getLogger(MasterActor.class);
    private final ActorRef aggregationActor;
    private final ActorRef filterActor;
    private final ActorRef groupingActor;
    private final ActorRef spatialCharActor;
    private final ActorRef spatialMatchingActor;
    private final ActorRef temporalCharActor;
    private final ActorRef temporalMatchingActor;
    private final ActorRef alertRouteProducer;


    private final FiniteDuration duration = Duration.create(10, TimeUnit.SECONDS);
    private final Timeout timeout = Timeout.durationToTimeout(duration);


    MasterActor(ActorRef aggregationActor
            , ActorRef filterActor
            , ActorRef groupingActor
            , ActorRef spatialCharActor
            , ActorRef spatialMatchingActor
            , ActorRef temporalCharActor
            , ActorRef temporalMatchingActor
            , ActorRef alertRouteProducer) {
        this.aggregationActor = aggregationActor;
        this.filterActor = filterActor;
        this.groupingActor = groupingActor;
        this.spatialCharActor = spatialCharActor;
        this.spatialMatchingActor = spatialMatchingActor;
        this.temporalCharActor = temporalCharActor;
        this.temporalMatchingActor = temporalMatchingActor;
        this.alertRouteProducer = alertRouteProducer;
    }

    /**
     * Creates the master actor.
     *
     * @param aggregationActor
     * @param filterActor
     * @param groupingActor
     * @param spatialCharActor
     * @param spatialMatchingActor
     * @param temporalCharActor
     * @param temporalMatchingActor
     * @param alertRouteProducer
     * @return
     */
    public static Props props(final ActorRef aggregationActor,
                              final ActorRef filterActor,
                              final ActorRef groupingActor,
                              final ActorRef spatialCharActor,
                              final ActorRef spatialMatchingActor,
                              final ActorRef temporalCharActor,
                              final ActorRef temporalMatchingActor,
                              final ActorRef alertRouteProducer) {
        return Props.create(new Creator<MasterActor>() {
            @Override
            public MasterActor create() throws Exception {
                return new MasterActor(aggregationActor
                        , filterActor
                        , groupingActor
                        , spatialCharActor
                        , spatialMatchingActor
                        , temporalCharActor
                        , temporalMatchingActor
                        , alertRouteProducer);
            }
        });

    }

    /**
     * Camel endpoint for this actor.
     *
     * @return
     */
    @Override
    public String getEndpointUri() {
        return "direct:masterActor";
    }

    /**
     * This actor parsed the query tree and sends mesaages to other actors to perform the query operation.
     * @param message
     * @throws Exception
     */
    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.info("In Master Actor");
        if (message instanceof CamelMessage) {
            LOGGER.debug("Message is Camel message");
            CamelMessage camelMessage = (CamelMessage) message;
            String masterQueryID = camelMessage.getBodyAs(String.class, getCamelContext());
            String regex = "[0-9]+";
            if (masterQueryID != null && masterQueryID.matches(regex)) {
                runQuery(masterQueryID);
                alertRouteProducer.tell(masterQueryID, getSelf());
            }
        } else if (message instanceof ExecuteQuery) {
            runQuery(String.valueOf(((ExecuteQuery) message).getId()));
        }
    }

    /**
     * This method parses the query tree and sends messages to different actors.
     * @param masterQueryID
     * @throws Exception
     */
    public void runQuery(String masterQueryID) throws Exception {
        boolean sync = false;
        QueryListDAO queryDAO = new QueryListDAO();
        JsonParser parser = new JsonParser();
        JsonArray queryArr = (JsonArray) parser.parse(queryDAO.getQueryTree(Integer.parseInt(masterQueryID)).get(0));

        List<JsonObject> jsonObjectList = new ArrayList<JsonObject>();
        for (int i = 0; i < queryArr.size(); i++) {
            JsonObject query = queryArr.get(i).getAsJsonObject();
            jsonObjectList.add(query);
        }
        for (int i = 0; i < queryArr.size(); i++) {
            if (sync) {
                break;
            }
            JsonObject query = queryArr.get(i).getAsJsonObject();
            if (query.get("dataSources") != null) {
                JsonArray sources = query.get("dataSources").getAsJsonArray();
                if (sources != null) {
                    for (int j = 0; j < sources.size(); j++) {
                        String source = sources.get(j).getAsString();
                        if (source.toLowerCase().startsWith("q")) {
                            sync = true;
                            break;
                        }
                    }
                }
            }
        }

        if (sync) {
            //ask
            LOGGER.info("Calling Akka actors synchronously ");
            for (int i = 0; i < queryArr.size(); i++) {
                JsonObject query = queryArr.get(i).getAsJsonObject();
                String patternType = query.get("patternType").getAsString();

                if (patternType.equalsIgnoreCase("filter")) {
                    Future<Object> future = Patterns.ask(filterActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("grouping")) {
                    Future<Object> future = Patterns.ask(groupingActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("aggregation")) {
                    Future<Object> future = Patterns.ask(aggregationActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("spchar")) {
                    Future<Object> future = Patterns.ask(spatialCharActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("spmatching")) {
                    Future<Object> future = Patterns.ask(spatialMatchingActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("tpchar")) {
                    Future<Object> future = Patterns.ask(temporalCharActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                } else if (patternType.equalsIgnoreCase("tpmatching")) {
                    Future<Object> future = Patterns.ask(temporalMatchingActor, new QueryMessage(masterQueryID, query), timeout);
                    String result = (String) Await.result(future, duration);
                }
            }
        } else {
            //tell
            LOGGER.info("Calling Akka actors Asynchronously ");
            for (int i = 0; i < queryArr.size(); i++) {
                JsonObject query = queryArr.get(i).getAsJsonObject();
                String patternType = query.get("patternType").getAsString();

                if (patternType.equalsIgnoreCase("filter")) {
                    filterActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("grouping")) {
                    groupingActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("aggregation")) {
                    aggregationActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("spchar")) {
                    spatialCharActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("spmatching")) {
                    spatialMatchingActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("tpchar")) {
                    temporalCharActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                } else if (patternType.equalsIgnoreCase("tpmatching")) {
                    temporalMatchingActor.tell(new QueryMessage(masterQueryID, query), getSelf());
                }
            }
        }
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
