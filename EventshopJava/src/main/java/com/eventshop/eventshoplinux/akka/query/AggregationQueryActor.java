package com.eventshop.eventshoplinux.akka.query;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.camel.javaapi.UntypedConsumerActor;
import akka.japi.Creator;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.ArrayUtils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;

/**
 * Created by aravindh on 6/29/15.
 */
public class AggregationQueryActor extends UntypedConsumerActor {

    @Override
    public String getEndpointUri() {
        return "direct:aggregationQueryActor";
    }


    private final static Logger LOGGER = LoggerFactory.getLogger(AggregationQueryActor.class);


    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof QueryActorMessage) {
            QueryActorMessage aggregationParams = (QueryActorMessage) message;
//            Emage emage = groupingParams.getEmage();
//            double[] imageArray = emage.getImage();

            List<Emage> emages =  aggregationParams.getEmageList();
          //  List<List<Double>> normalizedList =new ArrayList<List<Double>>(emages.size());
            Emage outputEmage = emages.get(0);
          //  ArrayList<ArrayList<Double>> allvalues = new ArrayList<ArrayList<Double>>(listOfImages.get(0).size());
            ArrayList<Double> result = new ArrayList<Double>();

            int numOfDataSource = emages.size();
            String operator="";
            double normalizationMin=9999;
            double normalizationMax=-9999;
            String isNormalized="false";
            ArrayList<Double> normalizationRange = new ArrayList<Double>();


            JsonObject query = aggregationParams.getQuery();
            JsonArray querySrcs= query.get("dataSources").getAsJsonArray();
             String patternType = query.get("patternType").getAsString();
            if (patternType.equalsIgnoreCase("aggregation")) {
                operator=query.get("aggOperator").getAsString();
                JsonArray jsonArray = query.getAsJsonArray("normedRange");
                for (int j=0;j < jsonArray.size();j++){
                    normalizationRange.add(jsonArray.get(j).getAsDouble());
                }
                normalizationMin=normalizationRange.get(0);
                normalizationMax=normalizationRange.get(1);
                isNormalized=query.get("valueNorm").getAsString();
            }


            LOGGER.info("Switching Operator: "+ operator);
            switch(operator){
                case "AggMax":
                   // allvalues.forEach((eachValue) -> result.add(Collections.max(eachValue)));
                        for(int i=0;i<emages.get(0).getImage().length;i++){
                            double max=0;
                            for(int j=0;j<numOfDataSource;j++){
                                if (j==0){
                                    max=emages.get(j).getImage()[i];
                                }
                                else{
                                    if(emages.get(j).getImage()[i] > max){
                                        max=emages.get(j).getImage()[i];
                                    }
                                }
                            }
                            result.add(max);
                        }
                    break;
                case "AggMin":
                //    allvalues.forEach((eachValue) -> result.add(Collections.min(eachValue)));
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double min=0;
                        for(int j=0;j<numOfDataSource;j++){
                            if (j==0){
                                min=emages.get(j).getImage()[i];
                            }
                            else{
                                if(emages.get(j).getImage()[i] < min){
                                    min=emages.get(j).getImage()[i];
                                }
                            }
                        }
                        result.add(min);
                    }
                    break;
                case "AggAvg":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double sum=0;
                        for(int j=0;j<numOfDataSource;j++){
                           sum+=emages.get(j).getImage()[i];
                        }
                        result.add(sum/numOfDataSource);
                    }
                    break;
                case "AggSum":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double sum=0;
                        for(int j=0;j<numOfDataSource;j++){
                            sum+=emages.get(j).getImage()[i];
                        }
                        result.add(sum);
                    }
                    break;
                case "AggMultiplication":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double mul=1;
                        for(int j=0;j<numOfDataSource;j++){
                            mul*= emages.get(j).getImage()[i];
                        }
                        result.add(mul);
                    }
                    break;
                case "AggSubtraction":
                    //
                    if (numOfDataSource == 2){
                        double sub;
                        for(int i=0;i<emages.get(0).getImage().length;i++){
                             sub=emages.get(0).getImage()[i] - emages.get(1).getImage()[i];
                            result.add(sub);

                        }
                    }else{
                       LOGGER.info("List size not handled. You should have 2 emages to perform subtraction...");
                    }

                    break;
                case "AggDivision":
                    if (numOfDataSource == 2){
                        double div;
                        for(int i=0;i<emages.get(0).getImage().length;i++){
                             div=emages.get(0).getImage()[i] / emages.get(1).getImage()[i];
                            result.add(div);
                        }
                    }else{
                        LOGGER.info("List size not handled. You should have 2 emages to perform division...");
                    }
                    break;
                case "AggAnd":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double andResult=0;
                        for(int j=0;j<numOfDataSource;j++){
                            if (emages.get(j).getImage()[i] !=0){
                                andResult=1;
                            }
                        }
                        result.add(andResult);
                    }
                    break;
                case "AggOR":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double andResult=0;
                        for(int j=0;j<numOfDataSource;j++){
                            if (emages.get(j).getImage()[i] ==1){
                                andResult=1;
                            }
                        }
                        result.add(andResult);
                    }
                    break;
                case "AggXOR":
                    for(int i=0;i<emages.get(0).getImage().length;i++){
                        double prev=0;
                        double curr=0;
                        for(int j=0;j<numOfDataSource;j++){
                            if(j==0){
                                prev=emages.get(j).getImage()[i];
                            }else{
                                curr=emages.get(j).getImage()[i];
                                if ((curr == prev) && ((curr == 0 || curr == 1) && (prev == 0 || prev== 1))){
                                    prev=1;
                                }else{
                                    prev=0;
                                }
                            }
                        }
                        result.add(prev);
                    }
                    break;
                case "AggNot":
                    if(emages.size() == 1){
                        for(int i=0;i<emages.get(0).getImage().length;i++){
                            if (emages.get(0).getImage()[i] == 0){
                                result.add(1.0);
                            }
                            else{
                                result.add(0.0);
                            }
                        }
                    }else{
                        LOGGER.info("List size not handled. You should have only 1 emage to perform Not operation.");
                    }
                    break;
                case "AggConvolution":
                    //open cv implementation needed....


                    Mat src = Mat.zeros(emages.get(0).row, emages.get(0).col, CvType.CV_32F);
                    int count=0;
                    for (int i = 0; i < emages.get(0).row; i++) {
                        for (int j = 0; j < emages.get(0).col; j++) {
                                src.put(i, j, emages.get(0).getImage()[count]);
                                count++;
                        }
                    }



                    Mat kernel = Mat.zeros(emages.get(0).row, emages.get(0).col, CvType.CV_32F);;
                    if (emages.size()==2){
                        count=0;
                        for (int i = 0; i < emages.get(1).row; i++) {
                            for (int j = 0; j < emages.get(1).col; j++) {
                                kernel.put(i, j, emages.get(1).getImage()[count]);
                                count++;
                            }
                        }
                    }
                    else
                    {
                        for(int i = 0; i < kernel.rows(); ++i)
                        {
                            for(int j = 0; j < kernel.cols(); ++j)
                            {
                                kernel.put(i, j, 1);
                            }
                        }
                    }
                    LOGGER.info("Before Conv:");
                    Mat convOut = Mat.zeros(src.rows(), src.cols(), CvType.CV_64F);
                    for(int i = 0; i < convOut.rows(); ++i)
                    {
                        for(int j = 0; j < convOut.cols(); ++j)
                           LOGGER.info(convOut.get(i,j)[0]+"");
                    }

                    Imgproc.filter2D(src, convOut, -1, kernel);
                    Core.divide(convOut, Scalar.all(kernel.rows() * kernel.cols()), convOut);

                    LOGGER.info("After Conv:");
                    for(int i = 0; i < convOut.rows(); ++i)
                    {
                        for(int j = 0; j < convOut.cols(); ++j)
                        {
                            LOGGER.info(convOut.get(i,j)[0]+"");

                        }
                    }
                    Mat abc = convOut.clone();

                    for(int i=0;i<convOut.rows();i++){
                        for(int j=0;j<convOut.cols();j++){
                            result.add(convOut.get(i,j)[0]);
                        }
                    }
                    convOut.release();
                    break;
                default:
                    break;

            }

            double[] imageResult= new double[result.size()];
            if (isNormalized.equalsIgnoreCase("true")){
                //Normalization logic goes here....
                for(int i=0;i<emages.size();i++){
                    double arrayMin;
                    double min=9999999;
                    for(int j=0;j<emages.get(i).getImage().length;j++){
                        if (emages.get(i).getImage()[j] < min){
                            min=emages.get(i).getImage()[j];
                        }
                    }
                    arrayMin=min;
                    double arrayMax;
                    double max=-9999999;
                    for(int j=0;j<emages.get(i).getImage().length;j++){
                        if (emages.get(i).getImage()[j] > max){
                            max=emages.get(i).getImage()[j];
                        }
                    }
                    arrayMax=max;




                    for(int j=0;j<result.size();j++){

                        imageResult[j]=normalizationMin + ((result.get(j)-arrayMin)/(arrayMax-arrayMin))*(normalizationMax-normalizationMin);
                      //  LOGGER.info(imageResult[j]+"");

                        //Below is a reference from old approach. Just to ensure formula is not changed.
                        // outMat.at<double>(i, j) = normMin +((inMat.at<double>(i, j)-arrayMin)/(arrayMax-arrayMin))*(normMax - normMin);
                    }
                }
            }else{
                for(int k=0;k<result.size(); k++) {
                    LOGGER.debug(""+result.get(k));
                }
                for(int j=0;j<result.size();j++){

                    imageResult[j]=result.get(j);
                    LOGGER.info(imageResult[j]+"");
                    //Below is a reference from old approach. Just to ensure formula is not changed.
                    // outMat.at<double>(i, j) = normMin +((inMat.at<double>(i, j)-arrayMin)/(arrayMax-arrayMin))*(normMax - normMin);
                }
            }



            outputEmage.setMax(Collections.max(result));
            outputEmage.setMin(Collections.min(result));
            outputEmage.setImage(imageResult);
            for(int i=0;i<outputEmage.getImage().length;i++){
                LOGGER.debug(""+outputEmage.getImage()[i]);
            }
            getSender().tell(outputEmage, getSelf());
//            List<Object> output = image.stream()
//                    .map(new Function<Double, Object>() {
//                        @Override
//                        public Object apply(Double aDouble) {
//                            int i=0;
//                            for (double group : thresholds) {
//                                if(aDouble > group){
//                                    i++;
//                                }else{
//                                    break;
//                                }
//                            }
//                            return i;
//
//                        }
//                    })
//                    .collect(Collectors.toList());
//
//            System.out.println(image);
//            System.out.println("*************");
//            System.out.println(output);



        }
    }



    public static final class AggregationParams {

        private List<Emage> emages;
        private String queryTree;


        public AggregationParams(List<Emage> emages, String queryTree) {
            this.emages = emages;
            this.queryTree = queryTree;
        }

        public List<Emage> getImages() {
            return emages;
        }

        public String getQueryTree() {
            return queryTree;
        }

    }
}
