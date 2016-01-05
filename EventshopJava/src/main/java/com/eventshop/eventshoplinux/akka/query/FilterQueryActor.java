package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.eventshop.eventshoplinux.DAO.datasource.DataSourceManagementDAO;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;


public class FilterQueryActor extends UntypedConsumerActor {

    private final static Logger LOG = LoggerFactory.getLogger(FilterQueryActor.class);
    private final FiniteDuration duration = Duration.create(100, TimeUnit.SECONDS);
    private final Timeout timeout = Timeout.durationToTimeout(duration);

    private ActorRef mongoQueryRouteProducerActor;

    FilterQueryActor(ActorRef mongoQueryRouteProducerActor) {
        this.mongoQueryRouteProducerActor = mongoQueryRouteProducerActor;
    }

    public static Props props(final ActorRef mongoQueryRouteProducerActor) {
        return Props.create(new Creator<FilterQueryActor>() {
            @Override
            public FilterQueryActor create() throws Exception {
                return new FilterQueryActor(mongoQueryRouteProducerActor);
            }
        });
    }


    @Override
    public String getEndpointUri() {
        return "direct:filterQueryActor";
    }


    @Override
    public void onReceive(Object message) throws Exception {
        long startTime = System.currentTimeMillis();
        if (message instanceof QueryActorMessage) {
            QueryActorMessage queryActorMessage = (QueryActorMessage) message;
            Emage emage = queryActorMessage.getEmageList().get(0);
            Double[] doubleArray = ArrayUtils.toObject(emage.getImage());
            List<Double> image = Arrays.asList(doubleArray);
            JsonObject query = queryActorMessage.getQuery();
            JsonArray normVals;
            double normMin = 0.0;
            double normMax = 0.0;
            JsonArray valRange;
            double valMin = 0.0;
            double valMax = 0.0;
            long startTimeRange = 0;

            String timeType = query.get("timeType").getAsString();
            String spatial_wrapper = query.get("spatial_wrapper").getAsString();



            String patternType = query.get("patternType").getAsString();
            if (patternType.equalsIgnoreCase("filter")) {
                if (query.get("boundMode").getAsString().equalsIgnoreCase("true")) {
                    valRange = query.get("valRange").getAsJsonArray();
                    valMin = valRange.get(0).getAsDouble();
                    valMax = valRange.get(1).getAsDouble();
                }

                if (query.get("normMode").getAsString().equalsIgnoreCase("true")) {
                    normVals = query.get("normVals").getAsJsonArray();
                    normMin = normVals.get(0).getAsDouble();
                    normMax = normVals.get(1).getAsDouble();
                }


                //Spatial Filter
                if (query.has("spatialFilter")) {
                    if (!query.get("spatialFilter").isJsonNull()) {
                        JsonArray spatialRange = query.get("spatialRange").getAsJsonArray();
                        double neLat = spatialRange.get(0).getAsDouble();
                        double neLon = spatialRange.get(1).getAsDouble();
                        double swLat = spatialRange.get(2).getAsDouble();
                        double swLon = spatialRange.get(3).getAsDouble();

                        int dsID = getDsID(query);
                        DataSource dataSource = getDataSource(dsID);

                        long endTimeToCheck = System.currentTimeMillis();
                        long timeToCheck = endTimeToCheck - dataSource.getInitParam().getTimeWindow();

                        MongoQueryMessage filterTemporal = new MongoQueryMessage(dsID, timeToCheck, endTimeToCheck
                                , neLat, neLon, swLat, swLon, 0.2, 0.2, spatial_wrapper);
                        Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, filterTemporal, timeout);

                        CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
                        Emage resultEmage = camelMessage.getBodyAs(Emage.class, getCamelContext());

                        Double[] resultArray = ArrayUtils.toObject(resultEmage.getImage());
                        image = Arrays.asList(resultArray);

                    }
                }

                if (!query.get("timeRange").isJsonNull()) {
                    JsonArray timeRangeArray = query.get("timeRange").getAsJsonArray();
//TODO DO NOT REMOVE THIS CODE
                    //Filter Absolute
                    String isAbs = timeRangeArray.get(0).getAsString();
                    if ((!isAbs.isEmpty()) && (!isAbs.contains(":"))) {
                    startTimeRange = timeRangeArray.get(0).getAsLong();
                    long stopTimeRange = timeRangeArray.get(1).getAsLong();

                    if (startTimeRange != 1) {
                        int dsID = getDsID(query);
                        DataSource dataSource = getDataSource(dsID);

                        Double neLat = dataSource.getInitParam().getNeLat();
                        Double neLon = dataSource.getInitParam().getNeLong();
                        Double swLat = dataSource.getInitParam().getSwLat();
                        Double swLon = dataSource.getInitParam().getSwLong();

                        Double latUnit = dataSource.getInitParam().getLatUnit();
                        Double lonUnit = dataSource.getInitParam().getLongUnit();

                        MongoQueryMessage filterTemporal = new MongoQueryMessage(dsID, (startTimeRange * 1000), System.currentTimeMillis()
                                , neLat, neLon, swLat, swLon, latUnit, lonUnit, spatial_wrapper);
                        Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, filterTemporal, timeout);

                        CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
                        Emage resultEmage = camelMessage.getBodyAs(Emage.class, getCamelContext());

                        Double[] resultArray = ArrayUtils.toObject(resultEmage.getImage());
                        image = Arrays.asList(resultArray);
                    }
                    }

//                    Filter Relative
                    String startDate = timeRangeArray.get(0).getAsString();

                    if (startDate != null) {
                        if (startDate.contains(":")) {
                            LOG.info("In fitler by relative time");
                            JsonArray timeRangeArrayRelaative = query.get("timeRange").getAsJsonArray();
                            String startDateR = timeRangeArrayRelaative.get(0).getAsString();
                            String endDate = timeRangeArrayRelaative.get(1).getAsString();

                            LOG.info("start date and end date {} , {}", startDateR, endDate);

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd:MM:yyyy HH:mm:ss");
                            Long startDateRLong = simpleDateFormat.parse(startDateR).getTime();
                            Long endDateLong = simpleDateFormat.parse(endDate).getTime();

                            LOG.info("start date and end date {} , {}", startDateRLong, endDateLong);


                            int dsID = getDsID(query);
                            DataSource dataSource = getDataSource(dsID);

                            Double neLat = dataSource.getInitParam().getNeLat();
                            Double neLon = dataSource.getInitParam().getNeLong();
                            Double swLat = dataSource.getInitParam().getSwLat();
                            Double swLon = dataSource.getInitParam().getSwLong();

                            Double latUnit = dataSource.getInitParam().getLatUnit();
                            Double lonUnit = dataSource.getInitParam().getLongUnit();


                            MongoQueryMessage filterTemporal = new MongoQueryMessage(dsID, startDateRLong, endDateLong
                                    , neLat, neLon, swLat, swLon, latUnit, lonUnit, spatial_wrapper);
                            Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, filterTemporal, timeout);

                            CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
                            Emage resultEmage = camelMessage.getBodyAs(Emage.class, getCamelContext());

                            Double[] resultArray = ArrayUtils.toObject(resultEmage.getImage());
                            image = Arrays.asList(resultArray);

                        }
                    }

                }

                DoubleSummaryStatistics stats = image
                        .stream()
                        .mapToDouble(new ToDoubleFunction<Double>() {
                            @Override
                            public double applyAsDouble(Double x) {
                                return x;
                            }
                        })
                        .summaryStatistics();

                LOG.info("Highest number in List : {}", stats.getMax());
                LOG.info("Lowest number in List : {}", stats.getMin());
                final double arrayMin = stats.getMin();
                final double arrayMax = stats.getMax();
                final double normMin1 = normMin;
                final double normMax1 = normMax;
                final double valMin1 = valMin;
                final double valMax1 = valMax;

                //Value Filter
                List<Double> output = image.stream()
                        .map(new Function<Double, Double>() {
                            @Override
                            public Double apply(Double aDouble) {
                                if (aDouble > valMin1 && aDouble < valMax1) {
                                    return aDouble;
                                } else {
                                    return 0.0;
                                }
                            }
                        })
                        .collect(Collectors.toList());

                //Normalize Filter
                List<Double> normalizedOutput = new ArrayList<Double>();
                normalizedOutput.addAll(output);

                if (query.get("normMode").getAsString().equalsIgnoreCase("true")) {
                    output = normalizedOutput.stream()
                            .map(new Function<Double, Double>() {
                                @Override
                                public Double apply(Double x) {
                                    return normMin1 + ((x - arrayMin) / (arrayMax - arrayMin)) * ((normMax1 - normMin1));
                                }
                            })
                            .collect(Collectors.toList());
                }

                //Output Emage statistics
                DoubleSummaryStatistics outDoubleSummaryStatistics = output.stream()
                        .mapToDouble(new ToDoubleFunction<Double>() {
                            @Override
                            public double applyAsDouble(Double x) {
                                return x;
                            }
                        })
                        .summaryStatistics();

                Emage resultEmage = emage;
                resultEmage.setStartTime(startTime);
                resultEmage.setEndTime(System.currentTimeMillis());
                resultEmage.setImage(ArrayUtils.toPrimitive(output.toArray(new Double[output.size()])));
                resultEmage.setMax(outDoubleSummaryStatistics.getMax());
                resultEmage.setMin(outDoubleSummaryStatistics.getMin());
                resultEmage.setColors(new ArrayList<>());

                getSender().tell(resultEmage, getSelf());

            }
        }
    }

    public DataSource getDataSource(int dsID) {
        DataSourceManagementDAO dataSourceManagementDAO = new DataSourceManagementDAO();
        return dataSourceManagementDAO.getDataSource(dsID);
    }

    public int getDsID(JsonObject query) {
        int dsID = 0;
        if (query.get("dataSources") != null) {
            JsonArray sources = query.get("dataSources").getAsJsonArray();
            if (sources != null) {
                String source = sources.get(0).getAsString();
                LOG.info("qid is : " + source);
                if (source.toLowerCase().startsWith("ds")) {
                    source = source.toLowerCase().replace("ds", "");
                    dsID = Integer.parseInt(source);
                }
            }
        }
        return dsID;
    }
}
