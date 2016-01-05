package com.eventshop.eventshoplinux.akka.query;

import akka.actor.UntypedActor;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.opencv.core.Core.minMaxLoc;
import static org.opencv.highgui.Highgui.imread;

/**
 * Created by aravindh on 7/8/15.
 */
public class SpatialPatternQueryActor extends UntypedActor {
    private final static Logger LOGGER = LoggerFactory.getLogger(SpatialPatternQueryActor.class);
//    private ActorRef masterActor;
//
//    SpatialPatternQueryActor(ActorRef masterActor) {
//        this.masterActor = masterActor;
//    }
//
//    public static Props props(final ActorRef masterActor) {
//        return Props.create(new Creator<SpatialPatternQueryActor>() {
//            @Override
//            public SpatialPatternQueryActor create() throws Exception {
//                return new SpatialPatternQueryActor(masterActor);
//            }
//        });
//    }


    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof QueryActorMessage) {
            QueryActorMessage spatialPatternParams = (QueryActorMessage) message;
            List<Emage> emages = spatialPatternParams.getEmageList();
            JsonObject query = spatialPatternParams.getQuery();
            Emage outputEmage = emages.get(0);

            boolean sizeNorm = false;
            boolean valueNorm = false;
            int rows;
            int cols;
            String paramtype;
            JsonParser parser = new JsonParser();
            //JsonObject query = (JsonObject) parser.parse(queryTree);
            final float pi = 3.14159265f;
            //JsonObject query = queryArr.get(i).getAsJsonObject();
            if (query.get("sizeNorm").toString().equals("true")) {
                sizeNorm = true;
            }
            if (query.get("valueNorm").toString().equals("true")) {
                valueNorm = true;
            }
            rows = Integer.parseInt(query.get("numRows").toString().replace("\"", ""));
            cols = Integer.parseInt(query.get("numCols").toString().replace("\"", ""));
            Mat pattern = Mat.zeros(rows, cols, CvType.CV_32F);
            Mat in = Mat.zeros(rows, cols, CvType.CV_32F);

            if (Integer.parseInt(query.get("patternSrc").toString()) == 0) {
                pattern = imread(query.get("filePath").toString(), 0);
                pattern.convertTo(pattern, CvType.CV_8U, 1);
                pattern.convertTo(pattern, CvType.CV_32F, 1);
            } else if (Integer.parseInt(query.get("patternSrc").toString()) == 1) {
                paramtype = query.get("parmType").toString();
                if (paramtype.equals("Gaussian")) {
                    double centerX, centerY, varX, varY, amplitude;
                    JsonObject gaussianParam = query.getAsJsonObject("gaussParam");
                    centerX = gaussianParam.get("centerX").getAsDouble();
                    centerY = gaussianParam.get("centerY").getAsDouble();
                    varX = gaussianParam.get("varX").getAsDouble();
                    varY = gaussianParam.get("varY").getAsDouble();
                    amplitude = gaussianParam.get("amplitude").getAsDouble();
                    int imageLength = emages.get(0).getImage().length;
                    int count = 0;
                    for (int i = 0; i < rows; i++) {
                        for (int j = 0; j < cols; j++) {
                            if (count == imageLength) {
                                break;
                            } else {
                                in.put(i, j, emages.get(0).getImage()[count]);
                                count++;
                            }
                        }
                    }
                    in.convertTo(in, CvType.CV_32F, 1);

                    for (int r = 0; r < rows; ++r) {
                        for (int c = 0; c < cols; ++c) {
                            pattern.put(r, c, amplitude * Math.exp(-0.5 * (Math.pow((r - centerX) / varX, 2) + Math.pow((c - centerY) / varY, 2))));
                        }
                    }
                } else if (paramtype.equals("Linear")) {
                    float startX, startY, startValue, dirGradient, valGradient;
                    JsonObject linearParam = query.getAsJsonObject("linearParam");
                    startX = linearParam.get("startX").getAsFloat();
                    startY = linearParam.get("startY").getAsFloat();
                    startValue = linearParam.get("startValue").getAsFloat();
                    dirGradient = linearParam.get("dirGradient").getAsFloat();
                    valGradient = linearParam.get("valGradient").getAsFloat();
                    //  "startX":"3","startY":"3","startValue":"40","dirGradient":"3","valGradient":"40"
                    float arc_direction = (float) Math.atan(dirGradient);
                    float arc_point = 0;
                    for (int i = 0; i < rows; ++i) {
                        for (int j = 0; j < cols; ++j) {
                            float inter_y = i - startY;
                            float inter_x = j - startX;
                            if (inter_x != 0) {
                                arc_point = (float) Math.atan(inter_y / inter_x);
                            } else {
                                if (inter_y == 0) arc_point = (float) (arc_direction + 0.5 * pi);
                                else arc_point = (float) 0.5 * pi;
                            }

                            float radius = (float) Math.pow((Math.pow(inter_y, 2) + Math.pow(inter_x, 2)), (float) 0.5);
                            float distance = (float) (radius * Math.cos(arc_point - arc_direction));
                            //    pattern.at<float>(i, j) = distance * value_gradient + start_value;
                            pattern.put(i, j, (distance * valGradient + startValue));
                        }
                    }
                }

            }
            if (sizeNorm) {
                Mat pattern_copy = pattern.clone();
                Imgproc.resize(pattern_copy, pattern, new Size(in.cols(), in.rows()), 0, 0, Imgproc.INTER_LINEAR);
            }
            double sizeRatio = (in.rows() * in.cols()) / ((pattern.rows() * pattern.cols()));
            if (sizeRatio < 1) {
                LOGGER.error("ERROR: input image is too small for pattern matching");
            }
            if (valueNorm) {
                // do not care about the difference in amplitude.
                // hence we will normalize so that the sum is 1 for the inputEmage
                Core.divide(in, Scalar.all(Core.sumElems(in).val[0]), in);
                Core.divide(pattern, Scalar.all(Core.sumElems(pattern).val[0] * sizeRatio), pattern);

            }

            Mat matching_result = Mat.zeros(in.rows(), in.cols(), CvType.CV_64F);
            int match_method;
            Imgproc.matchTemplate(in, pattern, matching_result, 1);
            //CV_TM_SQDIFF_NORMED = 1,
            double min_val, max_val;
            Point minLoc, maxLoc;
            Core.MinMaxLocResult minMaxresult = minMaxLoc(matching_result);
            min_val = minMaxresult.minVal;
            minLoc = minMaxresult.minLoc;
            maxLoc = minMaxresult.maxLoc;

            Mat out = Mat.zeros(1, 1, CvType.CV_64F);

            out.put(0, 0, 1 - min_val);
            double[] target = new double[out.rows() * out.cols()];
            int cnt = 0;
            for (int i = 0; i < out.rows(); ++i) {
                for (int j = 0; j < out.cols(); ++j) {

                    target[cnt] = out.get(i, j)[0];
                    LOGGER.info(target[cnt] + "");
                    cnt++;
                }
            }

            outputEmage.setImage(target);

            outputEmage.setRow(1);
            outputEmage.setCol(1);
            //double sw_lat = emage.getSwLat();
            double sw_long = emages.get(0).getSwLong();
            double ne_lat = emages.get(0).getNeLat();
            double lat_unit = emages.get(0).getLatUnit();
            double long_unit = emages.get(0).getLongUnit();
            outputEmage.setSwLat(ne_lat - lat_unit * (minLoc.y + pattern.rows() / 2 + 1));
            outputEmage.setSwLong(sw_long + long_unit * (minLoc.x + pattern.cols() / 2));
            LOGGER.info("Output Emage: " + outputEmage.getImage().length);
            getSender().tell(outputEmage, getSelf());
        }
        //String patternType = query.get("patternType").getAsString();
    }


//    public static final class SpatialPatternParams {
//
//        private List<Emage> emages;
//        private String queryTree;
//
//        public SpatialPatternParams(List<Emage> emages, String queryTree) {
//            this.emages = emages;
//            this.queryTree = queryTree;
//        }
//
//        public List<Emage> getEmages() {
//            return emages;
//        }
//
//        public String getQueryTree() {
//            return queryTree;
//        }
//
//    }
}
