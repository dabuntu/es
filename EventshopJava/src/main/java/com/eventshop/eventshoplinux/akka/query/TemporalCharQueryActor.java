package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.eventshop.eventshoplinux.DataCache;
import com.eventshop.eventshoplinux.akka.CommonUtil;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.TimeUnit;

/**
 * Created by abhisekmohanty on 13/7/15.
 */

/**
 * This class is an Actor which is used to perform temporal characterization on datasources and queries.
 */
public class TemporalCharQueryActor extends UntypedConsumerActor {

    private final static Logger LOGGER = LoggerFactory.getLogger(TemporalCharQueryActor.class);
    private final FiniteDuration duration = Duration.create(100, TimeUnit.SECONDS);
    private final Timeout timeout = Timeout.durationToTimeout(duration);

    private ActorRef mongoQueryRouteProducerActor;

    TemporalCharQueryActor(ActorRef mongoQueryRouteProducerActor) {
        this.mongoQueryRouteProducerActor = mongoQueryRouteProducerActor;
    }

    public static Props props(final ActorRef mongoQueryRouteProducerActor) {
        return Props.create(new Creator<TemporalCharQueryActor>() {
            @Override
            public TemporalCharQueryActor create() throws Exception {
                return new TemporalCharQueryActor(mongoQueryRouteProducerActor);
            }
        });
    }

    @Override
    public String getEndpointUri() {
        return "direct:temporalCharQueryActor";
    }

    @Override
    public void onReceive(Object message) throws Exception {
        LOGGER.info("Inside TemporalCharQueryActor");
        long startTime = System.currentTimeMillis();
        if (message instanceof QueryActorMessage) {
            QueryActorMessage queryActorMessage = (QueryActorMessage) message;
            int size = queryActorMessage.getEmageList().size();
            Emage latestEmage = queryActorMessage.getEmageList().get(0);
            Emage resultEmage = latestEmage;

            JsonObject query = queryActorMessage.getQuery();
            int dsID = 0;
            DataCache.updateRegisteredSources();
            if (query.get("dataSources") != null) {
                JsonArray sources = query.get("dataSources").getAsJsonArray();
                if (sources != null) {
                    String source = sources.get(0).getAsString();
                    LOGGER.info("qid is : " + source);
                    if (source.toLowerCase().startsWith("ds")) {
                        source = source.toLowerCase().replace("ds", "");
                        dsID = Integer.parseInt(source);
                    }
                }
            }
            long timeWindow = (query.get("timeWindow").getAsLong()) * 1000;
            LOGGER.info("timeWindow is :: " + timeWindow);
            long tcTimeWindow = (query.get("tcTimeWindow").getAsLong()) * 1000;
            LOGGER.info("tcTimeWindow is :::: " + tcTimeWindow);
            long oldEmageStartTime = startTime - tcTimeWindow;
            long oldEmageEndTime = oldEmageStartTime - timeWindow;
            double neLat = latestEmage.getNeLat();
            double neLong = latestEmage.getNeLong();
            double swLat = latestEmage.getSwLat();
            double swLong = latestEmage.getSwLong();
            double latUnit = latestEmage.getLatUnit();
            double longUnit = latestEmage.getLongUnit();
            String spatial_wrapper = query.get("spatial_wrapper").getAsString();


            MongoQueryMessage mongoQueryMessage = new MongoQueryMessage(dsID, oldEmageEndTime, oldEmageStartTime
                    , neLat, neLong, swLat, swLong, latUnit, longUnit, spatial_wrapper);
            Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, mongoQueryMessage, timeout);

            CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
            Emage oldEmage = camelMessage.getBodyAs(Emage.class, getCamelContext());
            LOGGER.info("oldEmage array length is ::: " + oldEmage.getImage().length);

            String temporal_operation = query.get("tmplCharOperator").getAsString().toUpperCase();

            // Do the computation
            Mat out = Mat.zeros(1, 1, CvType.CV_64F);
            switch (temporal_operation) {
                case "DISPLACEMENT": {
                    if (size > 0) {
                        LOGGER.info("displacement(latestEmage, oldEmage) is " + displacement(latestEmage, oldEmage));
                        out.put(0, 0, displacement(latestEmage, oldEmage));
                    }
                    else
                        out.put(0, 0, 0);
                }
                break;
                case "VELOCITY": {
                    if (size > 0)
                        out.put(0, 0, velocity(latestEmage, oldEmage));
                    else
                        out.put(0, 0, 0);
                }
                break;
                case "ACCELERATION": {
                    if (size < 3) out.put(0, 0, 0);
                    else {
                        double velocity_begin = velocity(latestEmage, oldEmage);
                        double velocity_end = velocity(latestEmage, oldEmage);
                        out.put(0, 0, (velocity_begin - velocity_end) / (oldEmage.getEndTime() - latestEmage.getEndTime()));
                    }
                }
                break;
                case "GROWTHRATE": {
                    if (size < 2) out.put(0, 0, 0);
                    else {
                        CommonUtil commonUtil = new CommonUtil();
                        double first_sum = Core.sumElems(commonUtil.getArray(latestEmage)).val[0];
                        double last_sum = Core.sumElems(commonUtil.getArray(oldEmage)).val[0];
                        out.put(0, 0, (last_sum - first_sum) / (oldEmage.getEndTime() - latestEmage.getEndTime()));
                    }
                }
                break;

            }
            LOGGER.info("result is ::: " + out.get(0,0)[0]);

//            // Save the cur_emage for furture query when next is not available
//            cur_emage = out_emage;
//            cur_emage.copyArray(out);
//            is_last_available = true;

            resultEmage.setStartTime(startTime);
            resultEmage.setEndTime(System.currentTimeMillis());
            resultEmage.setRow(1);
            resultEmage.setCol(1);
            resultEmage.setSwLat(-999);
            resultEmage.setSwLong(-999);
            resultEmage.setImage(out.get(0,0));
            resultEmage.setMapEnabled("false");
            resultEmage.setValue(((Double) out.get(0,0)[0]).toString());

            getSender().tell(resultEmage, getSelf());

        }
    }


        public void getEpicenter(Mat in, Point point) {
            double sum_val_w = 0;
            double sum_val_h = 0;
            double sum_val = 0;

            for(int h = 0; h < in.rows(); h++)
            {
                for(int w = 0; w < in.cols(); w++)
                {
                    sum_val += in.get(h,w)[0];
                    sum_val_w += in.get(h,w)[0] * w;
                    sum_val_h += in.get(h,w)[0] * h;
                }
            }
            double lat_long_val_w = (sum_val_w / sum_val);
            double lat_long_val_h = (sum_val_h / sum_val);
            point.y = lat_long_val_h;
            point.x = lat_long_val_w;
        }


        public double displacement(Emage first_emage, Emage last_emage) {
            Point first = new Point();
            Point last = new Point();
            first.x = first_emage.getSwLong();
            first.y = first_emage.getSwLat();
            last.x = last_emage.getSwLong();
            last.y = last_emage.getSwLat();
            CommonUtil commonUtil = new CommonUtil();

            if(first_emage.getRow() > 1 || first_emage.getCol() > 1)
                getEpicenter(commonUtil.getArray(first_emage), first);
            if(last_emage.getRow() > 1 || last_emage.getCol() > 1)
                getEpicenter(commonUtil.getArray(last_emage), last);
            double result = Math.sqrt(Math.pow((first.y - last.y), 2) + Math.pow((first.x - last.x), 2));
            return result;
        }


        public double velocity(Emage first_emage, Emage last_emage) {
            double disp = displacement(first_emage, last_emage);
            return disp / (last_emage.getEndTime() - first_emage.getEndTime()) * 1000;
        }


}
