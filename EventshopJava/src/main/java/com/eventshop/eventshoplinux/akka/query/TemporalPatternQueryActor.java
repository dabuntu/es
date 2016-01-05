package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import akka.pattern.Patterns;
import akka.util.Timeout;
import com.eventshop.eventshoplinux.akka.query.message.MongoQueryMessage;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.highgui.Highgui.imread;

/**
 * Created by aravindh on 7/15/15.
 */
public class TemporalPatternQueryActor extends UntypedConsumerActor {
    private final static Logger LOGGER = LoggerFactory.getLogger(TemporalPatternQueryActor.class);
    private ActorRef mongoQueryRouteProducerActor;
    private final FiniteDuration duration = Duration.create(100, TimeUnit.SECONDS);
    private final Timeout timeout = Timeout.durationToTimeout(duration);

    TemporalPatternQueryActor(ActorRef mongoQueryRouteProducerActor) {
        this.mongoQueryRouteProducerActor = mongoQueryRouteProducerActor;
    }

    public static Props props(final ActorRef mongoQueryRouteProducerActor) {
        return Props.create(new Creator<TemporalPatternQueryActor>() {
            @Override
            public TemporalPatternQueryActor create() throws Exception {
                return new TemporalPatternQueryActor(mongoQueryRouteProducerActor);
            }
        });
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof QueryActorMessage) {
            //LOGGER.info("I am in....");
            QueryActorMessage temporalPatternParams = (QueryActorMessage) message;
            List<Emage> emages =  temporalPatternParams.getEmageList();
            JsonObject query = temporalPatternParams.getQuery();

            List<Emage> buffer;

            //
            double dataDuration=0, patternDuration=0, patternSamplingRate=0;
            //Mongo Implementation to get all the emages....
            String patternSrc= query.get("patternSrc").toString();
            String paramType= query.get("parmType").toString();
            boolean durationNorm = query.get("durationNorm").getAsBoolean();
            boolean valueNorm = query.get("valueNorm").getAsBoolean();
            String spatialWrapper = query.get("spatial_wrapper").getAsString();

            dataDuration= query.get("dataDuration").getAsDouble();
            patternDuration=query.get("patternDuration").getAsDouble();
            patternSamplingRate=query.get("patternSamplingRate").getAsDouble();
            int numOfFrames= (int)(dataDuration/patternSamplingRate);
            buffer= new ArrayList<Emage>(numOfFrames);

            double neLat = emages.get(0).getNeLat();
            double neLon = emages.get(0).getNeLong();
            double swLat = emages.get(0).getSwLat();
            double swLon = emages.get(0).getSwLong();
            double latUnit = emages.get(0).getLatUnit();
            double longUnit = emages.get(0).getLongUnit();
            Mat pattern = Mat.zeros(1, numOfFrames, CvType.CV_32F);


            if (patternSrc.equalsIgnoreCase("create")){


                for(double i=numOfFrames;i>0;i--){
                    int dsID=getDsID(query);

                    long endTimeToCheck = System.currentTimeMillis();
                    long timeToCheck = (long) (endTimeToCheck - (patternDuration * 1000));
                    MongoQueryMessage bufferEmage = new MongoQueryMessage(dsID, timeToCheck, endTimeToCheck
                            , neLat, neLon, swLat, swLon, latUnit, longUnit, spatialWrapper);



                    Future<Object> future = Patterns.ask(mongoQueryRouteProducerActor, bufferEmage, timeout);

                    CamelMessage camelMessage = (CamelMessage) Await.result(future, duration);
                    buffer.add(camelMessage.getBodyAs(Emage.class, getCamelContext()));
                }


                if (paramType.equalsIgnoreCase("Linear")){
                    JsonObject linearParams = query.get("linearParam").getAsJsonObject();
                    double slope = linearParams.get("slope").getAsDouble();
                    double yIntercept = linearParams.get("yIntercept").getAsDouble();
                    for (int i=0; i<numOfFrames; ++i)
                    {
                        pattern.put(0,i,i*slope+yIntercept);
                    }
                }
                else if (paramType.equalsIgnoreCase("Exponential")){
                    JsonObject exponentialParams = query.get("exponentialParam").getAsJsonObject();
                    double scale = exponentialParams.get("scale").getAsDouble();
                    double base = exponentialParams.get("base").getAsDouble();

                    for (int i=0; i<numOfFrames; ++i)
                    {
                        pattern.put(0,i,scale* Math.pow(base, i));
                    }
                }
                else if (paramType.equalsIgnoreCase("Periodic")){
                    JsonObject periodicParams = query.get("periodicParam").getAsJsonObject();
                    double frequency = periodicParams.get("frequency").getAsDouble();
                    double amplitude = periodicParams.get("amplitude").getAsDouble();
                    double phaseDelay = periodicParams.get("phaseDelay").getAsDouble();

                    int numSamplesReqd = (int) (4*(1/frequency));

                    for (int i=0; i < numSamplesReqd; ++i)
                    {
                        pattern.put(0,i, (amplitude * (Math.sin((i % 4 - phaseDelay) * 3.14 / 2))));
                    }
                }
            }else if (patternSrc.equalsIgnoreCase("file")){
                //File Implementation Goes here...

                pattern = imread(query.get("filePath").toString(), 0);

//                ifstream patternFile(loadPath.c_str());
//
//                char ch;
//                int count = 0;
//                int samplingRate = 0;
//                patternFile >> count >> ch;
//                patternFile >> samplingRate;
//
//                timeBetweenFrames = samplingRate * 1000;
//                timeWindow = count * samplingRate * 1000;
//
//                tempPatternVals.create(1, count, CV_32F);
//                for(int i = 0; i < count; ++i)
//                {
//                    float value = 0;
//                    patternFile >> value >> ch;
//                    tempPatternVals.at<float>(0, i) = value;
//                }
//                patternFile.close();
            }

            int size = buffer.size(); // Buffer size...
            Mat out = Mat.zeros(1, 1, CvType.CV_64F);
            Emage outEmage = emages.get(emages.size()-1);
            if(size < 2 || size < dataDuration/patternDuration )
            {
                outEmage.setSwLat(-999);
                outEmage.setSwLong(-999);
                //return out_emage;
            }

            if (buffer.get(0).getCol() > 1 || buffer.get(0).getRow() > 1 )
            {
                LOGGER.error("ERROR:The incoming data for temporal pattern matching contains more than 1 pixel. Do spatial/temporal characterization first! ");
                LOGGER.error("Continuing with just the first pixel for now.");
            }
            Mat in = Mat.zeros(1, size, CvType.CV_32F);
            for (int i=0; i < size; ++i)
            {
                in.put(0,i,buffer.get(i).getImage()[0]);
            }

            if (durationNorm || paramType.equalsIgnoreCase("periodic"))
            {
                // so we will resize the kernel to match up with the input image and do ONLY one comparison
                Mat pattern_copy = pattern.clone();
                pattern = Mat.zeros(1, size, CvType.CV_32F);
                float match = (float)in.cols() / (float)pattern_copy.cols();
                for (int i=0; i < in.cols(); i++)
                {
                    pattern.put(0,i, pattern_copy.get(0, (int) Math.floor((float) i / match)) );
                }
            }
//
            double sizeRatio = (in.rows() * in.cols())/ ((pattern.rows() * pattern.cols()));
            if (sizeRatio < 1)
            {
                LOGGER.error("ERROR: input duration is too small for pattern matching");
            }

            if (valueNorm)
            {
                Mat temp=in;
                Core.absdiff(in, Scalar.all(0), temp);
                double sum=0;
                for (int col = 0; col < temp.cols(); col++) {
                        sum += temp.get(0,col)[0];
                }
                Core.divide(in, Scalar.all(sum), in);

                temp=pattern;
                Core.absdiff(pattern, Scalar.all(0), temp);
                sum=0;
                for (int col = 0; col < temp.cols(); col++) {
                    sum += temp.get(0,col)[0];
                }
                sum=sum*sizeRatio;
                Core.divide(pattern, Scalar.all(sum), pattern);

            }

            Mat matching_result = Mat.zeros(in.rows(),in.cols(), CvType.CV_32F);
            Imgproc.matchTemplate(in, pattern, matching_result, 1);
            //CV_TM_SQDIFF_NORMED=1

            double min_val;
            double max_val;
            Point minLoc;
            Point maxLoc;
//

            Core.MinMaxLocResult minMaxresult = minMaxLoc(matching_result);
            min_val = minMaxresult.minVal;
            minLoc = minMaxresult.minLoc;
            maxLoc = minMaxresult.maxLoc;

            out.put(0, 0, 1-min_val);
            outEmage.setSwLat(-999);
            outEmage.setSwLong(-999);
            double[] target = new double[out.rows() * out.cols()];
            int cnt=0;
            for (int i = 0; i < out.rows(); ++i) {
                for (int j = 0; j < out.cols(); ++j) {

                    target[cnt] = out.get(i, j)[0];
                    LOGGER.info(target[cnt]+"");
                    cnt++;
                }
            }
            outEmage.setImage(target);
            getSender().tell(outEmage, getSelf());
//        }
        }
    }
    public int getDsID(JsonObject query) {
        int dsID = 0;
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
        return dsID;
    }

    @Override
    public String getEndpointUri() {
        return "direct:temporalPatternQueryActor";
    }
}
