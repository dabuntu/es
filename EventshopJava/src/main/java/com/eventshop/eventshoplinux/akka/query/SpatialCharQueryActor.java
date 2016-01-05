package com.eventshop.eventshoplinux.akka.query;

import akka.camel.javaapi.UntypedConsumerActor;
import com.eventshop.eventshoplinux.model.Emage;
import com.google.gson.JsonObject;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * Created by nandhiniv on 7/9/15.
 */

/**
 * This acotr performs the Spatial Characterization for an Emage
 */
public class SpatialCharQueryActor extends UntypedConsumerActor {

    private final static Logger LOGGER = LoggerFactory.getLogger(SpatialCharQueryActor.class);

    @Override
    public String getEndpointUri() {
        return "direct:spatialCharQueryActor";
    }

    @Override
    public void onReceive(Object message) throws Exception {

        if (message instanceof QueryActorMessage) {

            long startTime = System.currentTimeMillis();
            QueryActorMessage queryActorMessage = (QueryActorMessage) message;
            Emage inputEmage = queryActorMessage.getEmageList().get(0);

            Emage outputEmage = new Emage(inputEmage.getTheme()
                    , System.currentTimeMillis()
                    , 0
                    , ""
                    , ""
                    , inputEmage.getLatUnit()
                    , inputEmage.getLongUnit()
                    , inputEmage.getSwLat()
                    , inputEmage.getSwLong()
                    , inputEmage.getNeLat()
                    , inputEmage.getNeLong()
                    , inputEmage.getRow()
                    , inputEmage.getCol()
                    , inputEmage.getMin()
                    , inputEmage.getMax()
                    , null
                    , inputEmage.getValue()
                    , new ArrayList<>()
                    , inputEmage.getMapEnabled());


            JsonObject query = queryActorMessage.getQuery();
            String operation = query.get("spCharoperator").getAsString().toUpperCase();
            LOGGER.info("Operation to be performed : {} ", operation);

            //Initialize the Mat for OpenCV by reading from 1D image array
            double[] imageArray = inputEmage.getImage();
            int rows = inputEmage.getRow();
            int cols = inputEmage.getCol();

            Mat inputMatrix = Mat.eye(rows, cols, CvType.CV_64F);
            Mat outputMatrix = Mat.eye(rows, cols, CvType.CV_64F);

            //Convert image[] to OpenCv Mat
            int count = 0;
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    if (count == imageArray.length) {
                        break;
                    } else {
                        inputMatrix.put(i, j, imageArray[count]);
                        outputMatrix.put(i, j, 0);
                        count++;

                    }
                }
            }

            String value = inputEmage.getValue();
            boolean createEmage = false;

            if (operation.equalsIgnoreCase("SPATIAL MAX")) {
                createEmage = true;
                Core.MinMaxLocResult mmr = Core.minMaxLoc(inputMatrix);
                double maxVal = mmr.maxVal;
                outputMatrix.put(new Double(mmr.maxLoc.x).intValue(), new Double(mmr.maxLoc.y).intValue(), maxVal);
                LOGGER.info("The maximum value is : {}", maxVal);
                value = String.valueOf(maxVal);
            } else if (operation.equalsIgnoreCase("SPATIAL MIN")) {
                createEmage = true;
                Core.MinMaxLocResult mmr = Core.minMaxLoc(inputMatrix);
                double minVal = mmr.minVal;
                outputMatrix.put(new Double(mmr.minLoc.x).intValue(), new Double(mmr.minLoc.y).intValue(), minVal);
                LOGGER.info("The minimum value is : {}", minVal);
                value = String.valueOf(minVal);
            } else if (operation.equalsIgnoreCase("SPATIAL AVERAGE")) {
                double average = (Core.mean(inputMatrix)).val[0];
//                outputMatrix.put(0, 0, average);
                value = String.valueOf(average);
                outputEmage.setMin(0);
                outputEmage.setMax(999);

                double[] image = new double[1];
                image[0] = 0;
                outputEmage.setImage(image);
                LOGGER.info("The average value is : {}", average);
            } else if (operation.equalsIgnoreCase("SPATIAL SUM")) {
                double sum = Core.sumElems(inputMatrix).val[0];
//                outputMatrix.put(0, 0, sum);
                LOGGER.info("The sum value is : {}", sum);
                value = String.valueOf(sum);
                outputEmage.setMin(0);
                outputEmage.setMax(999);
            } else if (operation.equalsIgnoreCase("COVERAGE")) {
                int coverage = Core.countNonZero((inputMatrix));
//                outputMatrix.put(0, 0, coverage);
                LOGGER.info("The sum value is : {}", coverage);
                value = String.valueOf(coverage);
                outputEmage.setMin(0);
                outputEmage.setMax(999);
            } else if (operation.equalsIgnoreCase("EPICENTER")) {
                createEmage = false;
                double sum_val_w = 0;
                double sum_val_h = 0;
                double sum_val = 0;
                for (int h = 0; h < rows; h++) {
                    for (int w = 0; w < cols; w++) {
                        double v = inputMatrix.get(h, w)[0];
                        sum_val += v;
                        sum_val_w += v * (w + 1);
                        sum_val_h += v * (h + 1);
                    }
                }
                double lat_long_val_w = (sum_val_w / sum_val) - 1;
                double lat_long_val_h = (sum_val_h / sum_val) - 1;
                outputEmage.setSwLat(outputEmage.getNeLat() - outputEmage.getLatUnit() * (lat_long_val_h + 1));
                outputEmage.setSwLong(outputEmage.getSwLong() + outputEmage.getLongUnit() * lat_long_val_w);
                outputEmage.setMapEnabled("false");
                outputEmage.setRow(1);
                outputEmage.setCol(1);
                value = "0";
                double[] image = new double[1];
                image[0] = 0;
                outputEmage.setImage(image);

            } else if (operation.equalsIgnoreCase("CIRCULARITY")) {
                int countNonZero = Core.countNonZero(inputMatrix);
                outputMatrix.put(0, 0, countNonZero);
                Mat circle = Mat.zeros(inputMatrix.rows(), inputMatrix.cols(), CvType.CV_32F);
                int min_val = 0;
                if ((inputMatrix.rows() / 2) < (inputMatrix.cols() / 2)) {
                    min_val = (inputMatrix.rows() / 2);
                } else {
                    min_val = (inputMatrix.cols() / 2);
                }
                Scalar color = new Scalar(0, 255, 255);
                Core.circle(inputMatrix, new Point((inputMatrix.rows() / 2), (inputMatrix.cols() / 2)),
                        min_val, new Scalar(5), -1, 8, 0);

                Mat output = Mat.eye(0, 0, CvType.CV_64F);
                inputMatrix.convertTo(inputMatrix, CvType.CV_32F, 1);
                Imgproc.matchTemplate(inputMatrix, circle, output, Imgproc.TM_SQDIFF_NORMED);
//                Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(inputMatrix);

                outputMatrix.put(0, 0, ((double) 1 - min_val));
                outputEmage.setMin(0);
                outputEmage.setMax(999);

            }
            //Convert Mat to 1D array

            if (createEmage == true) {
                double[] finalResult = new double[rows * cols];

                int index = 0;
                for (int h = 0; h < outputMatrix.rows(); h++) {
                    for (int w = 0; w < outputMatrix.cols(); w++) {
                        finalResult[index] = outputMatrix.get(h, w)[0];
                        index++;
                    }
                }
                outputEmage.setImage(finalResult);
                Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(outputMatrix);
                outputEmage.setMin(minMaxLocResult.minVal);
                outputEmage.setMax(minMaxLocResult.maxVal);
            }

            outputEmage.setValue(value);
            outputEmage.setStartTime(startTime);
            outputEmage.setEndTime(System.currentTimeMillis());
            //Send the result back to Master Actor
            getSender().tell(outputEmage, getSelf());
        }
    }
}
