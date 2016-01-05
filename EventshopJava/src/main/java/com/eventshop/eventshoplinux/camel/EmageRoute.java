package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.model.Emage;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by aravindh on 5/13/15.
 */
public class EmageRoute extends RouteBuilder{

    private final static Logger LOGGER = LoggerFactory.getLogger(EmageRoute.class);

    @Override
    public void configure() throws Exception {
        from("direct:emageBuilder")
                .process(new Processor() {
                             @Override
                             public void process(Exchange exchange) throws Exception {
                                 //  System.out.println(("Out " + exchange.getIn().getBody(String.class)));
                                 DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
                                    exchange.getOut().setHeaders(exchange.getIn().getHeaders());
//                                 double latUnit = exchange.getIn().getHeader("latUnit", Double.class);
//                                 double longUnit = exchange.getIn().getHeader("longUnit", Double.class);
//                                 double nelat = exchange.getIn().getHeader("nelat", Double.class);
//                                 double nelong = exchange.getIn().getHeader("nelong", Double.class);
//                                 double swlat = exchange.getIn().getHeader("swlat", Double.class);
//                                 double swlong = exchange.getIn().getHeader("swlong", Double.class);


                                 Emage emage = new Emage();
                                 emage.setTheme(dataSource.getSrcTheme());
                                 emage.setLatUnit(exchange.getIn().getHeader("latUnit", Double.class));
                                 emage.setLongUnit(exchange.getIn().getHeader("longUnit", Double.class));
                                 emage.setNeLat(exchange.getIn().getHeader("nelat", Double.class));
                                 emage.setNeLong(exchange.getIn().getHeader("nelong", Double.class));
                                 emage.setSwLat(exchange.getIn().getHeader("swlat", Double.class));
                                 emage.setSwLong(exchange.getIn().getHeader("swlong", Double.class));
                                 int numOfCols = exchange.getIn().getHeader("numOfCols", Integer.class);
                                 emage.setCol(numOfCols);
                                 int numOfRows = exchange.getIn().getHeader("numOfRows", Integer.class);
                                 emage.setRow(numOfRows);
                                 ArrayList values = exchange.getIn().getBody(ArrayList.class);
                                 int cnt = 0;
                                 LOGGER.debug("Rows: " + numOfRows);
                                 LOGGER.debug("cols: " + numOfCols);

                                 double[] image = new double[values.size()];

                                 for (int i = 0; i < values.size(); i++) {
                                     LOGGER.debug(values.get(i).toString());
                                     image[i] = Double.parseDouble(values.get(i).toString());

                                 }
                                 emage.setImage(image);
                                 Double min = 999999.9;
                                 Double max = 0.0;
                                 for (int i = 0; i < values.size(); i++) {
                                     if (Double.parseDouble(values.get(i).toString()) < min) {
                                         min = Double.parseDouble(values.get(i).toString());
                                     }
                                     if (Double.parseDouble(values.get(i).toString()) > max) {
                                         max = Double.parseDouble(values.get(i).toString());
                                     }
                                 }

                                 emage.setMin(min);
                                 emage.setMax(max);

                                 long endTime = (long) Math.ceil(System.currentTimeMillis() / dataSource.getInitParam().getTimeWindow()) * dataSource.getInitParam().getTimeWindow() + dataSource.getInitParam().getSyncAtMilSec();
                                 Date startTimeStr = new Date(endTime - dataSource.getInitParam().getTimeWindow());
                                 Date endTimeStr = new Date(endTime);
                                 long startTime = startTimeStr.getTime();


                                 emage.setStartTime(startTime);
                                 emage.setEndTime(endTime);
                                 emage.setStartTimeStr(startTimeStr.toString());
                                 emage.setEndTimeStr(endTimeStr.toString());
                                 exchange.getOut().setBody(emage);

                                 exchange.getOut().setHeader("dsID", dataSource.getSrcID());
                                 String filepath = Config.getProperty("tempDir") + "ds" + dataSource.getSrcID();

                                 exchange.getOut().setHeader("filepath", filepath);
                                 exchange.getOut().setHeader("createEmageFile", exchange.getIn().getHeader("createEmageFile"));
                                 JsonParser parser = new JsonParser();
                                 JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();
                                 int zoomFactor=1;
                                 if(jObj.has("zoomFactor")){
                                     zoomFactor=jObj.get("zoomFactor").getAsInt();
                                 }
                                 exchange.getOut().setHeader("zoomFactor",zoomFactor);


                                 System.out.println("I was here until zoom factor..... ");


                                 //// Temp code added coz camel route  "direct:multiEmage is not connecting....

                                 int zoomValue=2;
//                                 Emage emage = exchange.getIn().getBody(Emage.class);
//                                 DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
//                                 JsonParser parser = new JsonParser();
//                                 JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();
                                 String spatialWrapper = jObj.get("spatial_wrapper").getAsString();
                                 System.out.println("Spatial Wrapper is : "+ spatialWrapper);
                                // int zoomFactor = exchange.getIn().getHeader("zoomfactor",Integer.class);
                                 List<Emage> emageList = new ArrayList<Emage>(zoomFactor);
                                 emageList.add(emage);
                                 int rows = dataSource.getInitParam().getNumOfRows();
                                 System.out.println("Intial Rows : "+rows);
                                 int cols = dataSource.getInitParam().getNumOfColumns();
                                 System.out.println("Initial Cols: "+cols);
                                 double[][] image2D = new double[rows][cols];
                                 double[] image1 = emage.getImage();
                                 cnt=0;
                                 for(int i=0;i<rows;i++){
                                     for(int j=0;j<cols;j++){
                                         image2D[i][j]=image1[cnt];
                                         cnt++;
                                     }
                                 }
                                 double[][] temp2DLayer = new double[(int)(Math.ceil(rows/zoomValue))][(int)Math.ceil(cols/zoomValue)];
                                 double[] tempElements = new double[zoomValue*zoomValue];
                                 int tempRows=rows;
                                 int tempCols = cols;
                                 int tempCnt=0;
                                 double tempLatUnit=exchange.getIn().getHeader("latUnit", Double.class);
                                 double tempLongUnit =exchange.getIn().getHeader("longUnit", Double.class);
                                 if(zoomFactor>1){
                                     for(int k=1;k<zoomFactor;k++){ // For Number of zoom layers....
                                         System.out.println("Zoom Count: "+ k);
                                         int tempRowCnt=0, tempColCnt=0;
                                         int colCnter=0,rowCnter=0;
                                         for(int l=0;l<tempRows-1;l=l+zoomValue){  // increments the rows based on zoom value.. lets say 2 in this case...
                                             for(int m=0;m<tempCols-1;m=m+zoomValue){// increments the cols based on zoom values. lets say 2 in this case..
                                                // System.out.println("Col Counter:" +colCnter++);
                                                 //System.out.println("M: "+ m);

                                                 tempCnt=0;
                                                 for(int n=l;n<l+zoomValue;n++){
                                                     for(int o=m;o<m+zoomValue;o++){
                                                         tempElements[tempCnt]= image2D[n][o];
                                                         tempCnt++;
                                                     }
                                                 }
                                                 temp2DLayer[tempRowCnt][tempColCnt] =applySpatialOperations(tempElements,spatialWrapper);
                                                 tempColCnt++;
                                             }
                                             tempColCnt=0;
                                             //System.out.println("L:"+l);
                                             tempRowCnt++;
                                             colCnter=0;
                                          //   System.out.println("Row Counter:"+rowCnter++);
                                         }
                                         cnt=0;
                                         double[] tempImage = new double[temp2DLayer.length*temp2DLayer[0].length];
                                         for(int i =0 ;i< temp2DLayer.length;i++){
                                             for(int j=0;j<temp2DLayer[0].length;j++){
                                                 tempImage[cnt]= temp2DLayer[i][j];
                                                 cnt++;
                                             }
                                         }
                                         Emage tempEmage = new Emage();
                                         tempEmage.setTheme(dataSource.getSrcTheme());
                                         tempEmage.setLatUnit(tempLatUnit*zoomValue);
                                         tempEmage.setLongUnit(tempLongUnit*zoomValue);
                                         tempEmage.setNeLat(exchange.getIn().getHeader("nelat", Double.class));
                                         tempEmage.setNeLong(exchange.getIn().getHeader("nelong", Double.class));
                                         tempEmage.setSwLat(exchange.getIn().getHeader("swlat", Double.class));
                                         tempEmage.setSwLong(exchange.getIn().getHeader("swlong", Double.class));
                                         tempEmage.setCol(temp2DLayer[0].length);
                                         tempEmage.setRow(temp2DLayer.length);
                                         tempEmage.setStartTime(emage.getStartTime());
                                         tempEmage.setEndTime(emage.getEndTime());
                                         tempEmage.setStartTimeStr(emage.getStartTimeStr());
                                         tempEmage.setEndTimeStr(emage.getEndTimeStr());
                                         double tempMin=tempImage[0];
                                         double tempMax=tempImage[0];
                                         for(int i=0;i<tempImage.length;i++){
                                             if(tempImage[i]<tempMin){
                                                 tempMin=tempImage[i];
                                             }
                                             if(tempImage[i]>tempMax){
                                                 tempMax=tempImage[i];
                                             }
                                         }
                                         tempEmage.setMin(tempMin);
                                         tempEmage.setMax(tempMax);
                                         tempEmage.setImage(tempImage);
                                         System.out.println("layer"+k+" size:"+tempImage.length);
                                         emageList.add(tempEmage);
                                         if (k!=zoomFactor){
                                             tempRows = temp2DLayer.length;
                                             tempCols = temp2DLayer[0].length;
                                             image2D=temp2DLayer;
                                             temp2DLayer = new double[(int)(Math.ceil(tempRows/zoomValue))][(int)Math.ceil(tempCols/zoomValue)];
                                             tempElements = new double[zoomValue*zoomValue];
                                             tempLatUnit=tempLatUnit*k;
                                             tempLongUnit=tempLongUnit*k;
                                         }
                                     }

                                 }
                                 int emageLayerCnt=0;
                                 for(Emage emg : emageList){
                                     ObjectMapper mapper = new ObjectMapper();
                                     if (emageLayerCnt == 0) {
                                         mapper.writeValue(new File(Config.getProperty("datasourceJsonLoc") + exchange.getIn().getHeader("dsID") + ".json"), emg);
                                     } else {
                                         mapper.writeValue(new File(Config.getProperty("datasourceJsonLoc") + exchange.getIn().getHeader("dsID") + "_layer" + emageLayerCnt + ".json"), emg);
                                     }
                                     System.out.println("Done Writting layer"+emageLayerCnt);

                                     emageLayerCnt++;
                                 }
                                 //  Double




                             }
                         }

                )
                .choice()
                .when(header("zoomFactor").isGreaterThan(1))
                .to("direct:multiEmage")
//                .choice()
//                .when(header("createEmageFile").isNotEqualTo(false))
//                .to("direct:emageJson")


//                .multicast().parallelProcessing().to("direct:emageJson", "direct:emageBin")
        ;



//                from("direct:multiEmage")
//                        .process(new Processor() {
//                            @Override
//                            public void process(Exchange exchange) throws Exception {
//                                int zoomValue=2;
//                                Emage emage = exchange.getIn().getBody(Emage.class);
//                                DataSource dataSource = exchange.getIn().getHeader("datasource", DataSource.class);
//                                JsonParser parser = new JsonParser();
//                                JsonObject jObj = parser.parse(dataSource.getWrapper().getWrprKeyValue()).getAsJsonObject();
//                                String spatialWrapper = jObj.get("spatial_wrapper").getAsString();
//
//                                int zoomFactor = exchange.getIn().getHeader("zoomfactor",Integer.class);
//                                List<Emage> emageList = new ArrayList<Emage>(zoomFactor);
//                                emageList.add(emage);
//                                int rows = dataSource.getInitParam().getNumOfRows();
//                                int cols = dataSource.getInitParam().getNumOfColumns();
//                                double[][] image2D = new double[rows][cols];
//                                double[] image = emage.getImage();
//                                System.out.println("Image Length : "+ image.length);
//                                int cnt=0;
//                                for(int i=0;i<rows;i++){
//                                    for(int j=0;j<cols;j++){
//                                        image2D[i][j]=image[cnt];
//                                        cnt++;
//                                    }
//                                }
//                                double[][] temp2DLayer = new double[rows/zoomValue][cols/zoomValue];
//                                double[] tempElements = new double[zoomValue*zoomValue];
//                                int tempCnt=0;
//                                int tempRows=rows;
//                                int tempCols = cols;
//                                if(zoomFactor>1){
//                                    for(int k=1;k<zoomFactor;k++){ // For Number of zoom layers....
//                                        int tempRowCnt=0, tempColCnt=0;
//                                        for(int l=0;l<tempRows;l=l+zoomValue){  // increments the rows based on zoom value.. lets say 2 in this case...
//                                            for(int m=0;m<tempCols;m=m+zoomValue){ // increments the cols based on zoom values. lets say 2 in this case..
//
//                                                tempCnt=0;
//                                                for(int n=l;n<l+zoomValue;n++){
//                                                    for(int o=m;o<m+zoomValue;o++){
//                                                        tempElements[tempCnt]= image2D[n][o];
//                                                        tempCnt++;
//                                                    }
//                                                }
//                                                temp2DLayer[tempRowCnt][tempColCnt] =applySpatialOperations(tempElements,spatialWrapper);
//                                                tempColCnt++;
//                                            }
//                                            tempRowCnt++;
//                                        }
//                                      //  System.out.println("");
//                                        cnt=0;
//                                        double[] tempImage = new double[temp2DLayer.length*temp2DLayer[0].length];
//                                        for(int i =0 ;i< temp2DLayer.length;i++){
//                                            for(int j=0;j<temp2DLayer[0].length;j++){
//                                                tempImage[cnt]= temp2DLayer[i][j];
//                                                cnt++;
//                                            }
//                                        }
//
//                                        Emage tempEmage = new Emage();
//                                        tempEmage.setTheme(dataSource.getSrcTheme());
//                                        tempEmage.setLatUnit(exchange.getIn().getHeader("latUnit", Double.class)*zoomValue*k);
//                                        tempEmage.setLongUnit(exchange.getIn().getHeader("longUnit", Double.class)*zoomFactor*k);
//                                        tempEmage.setNeLat(exchange.getIn().getHeader("nelat", Double.class));
//                                        tempEmage.setNeLong(exchange.getIn().getHeader("nelong", Double.class));
//                                        tempEmage.setSwLat(exchange.getIn().getHeader("swlat", Double.class));
//                                        tempEmage.setSwLong(exchange.getIn().getHeader("swlong", Double.class));
//                                        tempEmage.setCol(tempCols);
//                                        tempEmage.setRow(tempRows);
//                                        tempEmage.setImage(tempImage);
//                                        emageList.add(tempEmage);
//                                        System.out.println("Emage added...");
//                                        if (k<=zoomFactor){
//                                            tempRows = temp2DLayer.length;
//                                            tempCols = temp2DLayer[0].length;
//                                            image2D=temp2DLayer;
//                                            temp2DLayer = new double[rows/zoomValue][cols/zoomValue];
//                                            tempElements = new double[zoomValue*zoomValue];
//                                        }
//                                    }
//
//                                }
//                                int emageLayerCnt=0;
//                                for(Emage emg : emageList){
//                                    ObjectMapper mapper = new ObjectMapper();
//                                    mapper.writeValue(new File(Config.getProperty("datasourceJsonLoc")+exchange.getIn().getHeader("dsID")+"_layer"+emageLayerCnt+".json"), emg);
//                                    System.out.println("Done Writting layer"+emageLayerCnt);
//                                    emageLayerCnt++;
//
//                                }
//                              //  Double
//                            }
//                        });

                from("direct:emageJson")
                    .marshal().json(JsonLibrary.Jackson)
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                           LOGGER.info("Writing JSON!!!!");
                            exchange.getOut().setBody(exchange.getIn().getBody(String.class));
                            exchange.getOut().setHeader("CamelFileName", exchange.getIn().getHeader("dsID") + ".json");
                            exchange.getOut().setHeader("dsID", exchange.getIn().getHeader("dsID"));
                           LOGGER.info("Done...");
                        }
                    })
                        .to("file:" + Config.getProperty("datasourceJsonLoc") + "?noop=true&charset=iso-8859-1")
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                exchange.getOut().setHeader("dsID", exchange.getIn().getHeader("dsID"));
                            }
                        })
                        .multicast().to("direct:searchAndRunQuery", "direct:runAlertForDS")
                ;


//                from("direct:emageBin")
//                        .process(new Processor() {
//                            @Override
//                            public void process(Exchange exchange) throws Exception {
//                                System.out.println("Started generating Binary!!!!!");
//                                exchange.getOut().setHeader("dsID", exchange.getIn().getHeader("dsID"));
//                                Emage emage = exchange.getIn().getBody(Emage.class);
//                                Message.EmageMsg.Builder builder = Message.EmageMsg
//                                        .newBuilder().setTheme(emage.getTheme())
//                                        .setStartTime(emage.getStartTime())
//                                        .setEndTime(emage.getEndTime())
//                                        .setLatUnit(emage.getLatUnit())
//                                        .setLongUnit(emage.getLongUnit())
//                                        .setSwLat(emage.getSwLat())
//                                        .setSwLong(emage.getSwLong())
//                                        .setNeLat(emage.getNeLat())
//                                        .setNeLong(emage.getNeLong())
//                                        .setNumRows(emage.getRow())
//                                        .setNumCols(emage.getCol());
//                                // Get the cell values and add it to emage
//                                double[] cells = emage.getImage();
//                                int cnt = 0;
//                                for (int i = 0; i < emage.getRow(); i++) {
//                                    for (int j = 0; j < emage.getCol(); j++) {
////                                        if (cells[cnt] == 0.0) {
////                                            builder.addCell(Double.NaN);
////                                        } else {
//                                            builder.addCell(cells[cnt]);
////                                        }
//                                        cnt++;
//                                    }
//
//                                }
//
//                                // Build the Message
//                                Message.EmageMsg msg = builder.build();
//
//                                // Output the data
//                                byte[] data = msg.toByteArray();
//                                byte[] size = ByteBuffer.allocate(4)
//                                        .order(ByteOrder.LITTLE_ENDIAN).putInt(data.length)
//                                        .array();
//                                ByteArrayOutputStream result = new ByteArrayOutputStream(
//                                        data.length + 4);
//                                try {
//                                    result.write(size);
//                                    result.write(data);
//                                } catch (IOException e1) {
//                                    log.error(e1.getMessage());
//                                }
//                                String filepath = Config.getProperty("tempDir") + "ds" + exchange.getIn().getHeader("dsID");
//                                FileOutputStream output = null;
//                                try {
//                                    output = new FileOutputStream(filepath);
//                                } catch (FileNotFoundException e1) {
//                                    log.error(e1.getMessage());
//                                }
//
////                                 Lock the file for writing
//                                FileLock lock = null;
//                                try {
//                                    while (lock == null)
//                                        lock = output.getChannel().tryLock();
//                                    output.write(result.toByteArray());
//                                    output.flush();
//                                    lock.release();
//
//                                    // 08/19/2011 Mingyan
//                                    output.close();
//                                    System.out.println("Successfully write the (binary) output file " + filepath);
//                                    System.out.println("I am the last before writting json..");
//                                } catch (IOException e1) {
//                                    log.error(e1.getMessage());
//                                    // add by Siripen, to solve runnning method calling twice
//                                    // from the front UI, cause this locking throw an error
//                                    // will need to resolve this issue later
//                                }
//                            }
//
//                        })
//                        .to("direct:searchAndRunQuery")
//                ;


//                .to("file:" + Config.getProperty("tempDir") + "?noop=true&charset=iso-8859-1");
    }

    private double applySpatialOperations(double[] tempElements, String spatialWrapper) {
        double result=0;
        switch (spatialWrapper){
            case "sum":
                for(int i=0; i<tempElements.length;i++){
                    result += tempElements[i];
                }
                break;
            case "count":
                for(int i=0; i<tempElements.length;i++){
                    result += tempElements[i];
                }
                break;
            case "avg":
                for(int i=0;i<tempElements.length;i++){
                    result+=tempElements[i];
                }
                result=result/tempElements.length;
                break;
            case "min":
                double min=999999999.999999;
                for(int i=0;i<tempElements.length;i++){
                    if(i==0){
                        min=tempElements[i];
                    }else{
                        if (tempElements[i]<min){
                            min=tempElements[i];
                        }
                    }

                }
                result=min;
                break;
            case "max":
                double max=-999999999.999999;
                for(int i=0;i<tempElements.length;i++){
                    if(i==0){
                        max=tempElements[i];
                    }else{
                        if (tempElements[i]>max){
                            max=tempElements[i];
                        }
                    }

                }
                result=max;
                break;
            default:
                result=0;
                break;

        }
        return  result;
    }


}

