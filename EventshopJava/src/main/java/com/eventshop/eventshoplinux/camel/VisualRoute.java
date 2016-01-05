package com.eventshop.eventshoplinux.camel;

import com.eventshop.eventshoplinux.domain.datasource.DataSource;
import com.eventshop.eventshoplinux.model.ELocation;
import com.eventshop.eventshoplinux.model.STT;
import com.eventshop.eventshoplinux.util.commonUtil.Config;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.media.jai.*;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.operator.MedianFilterDescriptor;
import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;

/**
 * Created by aravindh on 7/27/15.
 */
public class VisualRoute extends RouteBuilder {
    private final static Logger LOGGER = LoggerFactory.getLogger(VisualRoute.class);
    @Override
    public void configure() throws Exception {
        from("direct:toVisual")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {

                        exchange.getOut().setHeaders(exchange.getIn().getHeaders());
                        DataSource ds = exchange.getIn().getHeader("datasource", DataSource.class);
                        String filePath = ds.getUrl();
                        URL url = new URL(filePath);

                        RenderedImage img1 = ImageIO.read(url);
                        ;
                        ImageIO.write(img1, "gif", new File("orig_" + ds.getSrcID() + ".gif"));


                        //Read the above file and create Gray scale image
                        File file = new File("orig_" + ds.getSrcID() + ".gif");
                        BufferedImage orginalImage = ImageIO.read(file);


//                        BufferedImage orginalImage = new BufferedImage(img1.getWidth(),
//                                img1.getHeight(),
//                                BufferedImage.TYPE_INT_RGB);

                        BufferedImage grayScaleImg = new BufferedImage(
                                orginalImage.getWidth(), orginalImage.getHeight(),
                                BufferedImage.TYPE_BYTE_GRAY);

                        ImageIO.write(orginalImage, "png", new File("Orig" + ds.getSrcID() + ".png"));

                        Graphics2D graphics = grayScaleImg.createGraphics();
                        graphics.drawImage(orginalImage, 0, 0, null);
                        ImageIO.write(grayScaleImg, "png", new File("GrayScale_" + ds.getSrcID() + ".png"));


                        PlanarImage grayScaleInput = (PlanarImage) JAI.create("FileLoad", "GrayScale_" + ds.getSrcID() + ".png");
                        ROIShape in1 = new ROIShape(grayScaleInput.getBounds());
                        ParameterBlock pb1 = new ParameterBlock();
                        pb1.addSource(grayScaleInput);
                        pb1.add(in1);
                        pb1.add(MedianFilterDescriptor.MEDIAN_MASK_SQUARE);
                        pb1.add(3);
                        BufferedImage nImage = grayScaleInput.getAsBufferedImage();
                        BufferedImage filteredImage = new BufferedImage(nImage.getWidth(null), nImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
                        Graphics g = filteredImage.getGraphics();
                        g.drawImage(nImage, 0, 0, null);
                        ImageIO.write(filteredImage, "png", new File("GrayScale_Sharp" + ds.getSrcID() + ".png"));

                        JsonParser parser = new JsonParser();
                        JsonObject jObj = parser.parse(ds.getWrapper().getWrprKeyValue()).getAsJsonObject();
                        String colorMat = null, transMat = null;
                        if (jObj.has("color_matrix")) {
                            colorMat = jObj.get("color_matrix").getAsString();
                        }
                        boolean applyColorMatrix, applyTransMatrix;
                        double scalingFactor = 1;

                        LOGGER.debug("Color Matrix :" + colorMat);
                        if (colorMat.equals(null)) {
                            applyColorMatrix = false;
                        } else {
                            applyColorMatrix = true;
                        }

                        if (jObj.has("trans_matrix")) {
                            transMat = jObj.get("trans_matrix").getAsString();
                            applyTransMatrix = true;
                            if (jObj.has("scaling_factor")) {
                                scalingFactor = jObj.get("scaling_factor").getAsDouble();
                            }
                        } else {
                            applyTransMatrix = false;
                        }


                        PlanarImage pi = (PlanarImage) JAI.create("FileLoad", "GrayScale_Sharp" + ds.getSrcID() + ".png");
                        PlanarImage rectImage = null;
                        if (applyTransMatrix) {


                            List<String> x = Arrays.asList(transMat);

                            x = Arrays.asList(transMat.split("\\s*,\\s*"));


                            double[][] transMatrix = new double[x.size()][2];

                            int xcnt = 0;
                            for (String s : x) {
                                StringTokenizer st = new StringTokenizer(s, ":");
                                transMatrix[xcnt][0] = Double.valueOf(st.nextToken());
                                transMatrix[xcnt][1] = Double.valueOf(st.nextToken());
                                xcnt++;
                            }
                            rectImage = createRectifiedImage(pi, transMatrix, scalingFactor, ds);
                        } else {
                            rectImage = pi;
                        }


                        RandomIter iter = RandomIterFactory.create(rectImage, null);
                        SampleModel smO = rectImage.getSampleModel();
                        int nbands = smO.getNumBands();
                        int[] pixel = new int[nbands];
                        LOGGER.debug("Nbands:" + pixel.length);
                        double imgWidth = rectImage.getWidth();
                        double imgHeight = rectImage.getHeight();
                        double nelat = ds.getInitParam().getNeLat();
                        double nelong = ds.getInitParam().getNeLong();
                        double swlat = ds.getInitParam().getSwLat();
                        double swlong = ds.getInitParam().getSwLong();
                        double latunit = ds.getInitParam().getLatUnit();
                        double longunit = ds.getInitParam().getLongUnit();
                        //double[][] coordinates = new double[][];
                        double rows =  Math.ceil((nelat - swlat) / latunit);
                        double cols =  Math.ceil((nelong - swlong) / longunit);
                        LOGGER.debug("Rows: " + rows);
                        LOGGER.debug("Cols" + cols);
                        LOGGER.debug("Img Height : " + imgHeight);
                        LOGGER.debug("Img Width" + imgWidth);
                        double[] image = new double[(int)(rows * cols)];
                        double avgOfOneCoord = 0;
                        //get Coordinates
                        int rcnt = 0, ccnt = 0;
                        double geoCoordToImageCoordColRatio = imgWidth / cols;
                        double geoCoordToImageCoordRowRatio = imgHeight / rows;

                        LOGGER.debug("Geo Image Col Ratio " + geoCoordToImageCoordColRatio);
                        LOGGER.debug("Geo Image Row Ratio " + geoCoordToImageCoordRowRatio);

                        int imageCnt = 0;
                        int[] pixelVal;
                        int currHeight = 0;
                        int currWidth = 0;
                        List<STT> sttList = new ArrayList<STT>();

                        double[] minRange = null, maxRange = null, values = null;


                        //  String colorMat = "0-50:1,50-100:2,100-150:3,150-200:4,200-255:5";

                        String[] colMatArray = colorMat.split(",");
                        minRange = new double[colMatArray.length];
                        maxRange = new double[colMatArray.length];
                        values = new double[colMatArray.length];
                        for (int i = 0; i < colMatArray.length; i++) {
                            minRange[i] = Double.parseDouble(colMatArray[i].split("-")[0]);
                            maxRange[i] = Double.parseDouble(colMatArray[i].split(":")[0].split("-")[1]);
                            values[i] = Double.parseDouble(colMatArray[i].split(":")[1]);
                            // System.out.println("Min:" + minRange[i] + "   Max:" + maxRange[i] + "    values:" + values[i]);
                        }


                        for (double i = nelat; i > swlat; i = i - latunit) {
                            ccnt = 0;
                            for (double j = swlong; j < nelong; j = j + longunit) {
                                double sum = 0;
                                int max = 0;
                                for (int l = 0; l < geoCoordToImageCoordRowRatio; l++) {
                                    for (int k = 0; k < geoCoordToImageCoordColRatio; k++) {
                                        int c = (int) ((ccnt * geoCoordToImageCoordColRatio) + k);
                                        int r = (int) ((rcnt * geoCoordToImageCoordRowRatio) + l);
                                        pixelVal = iter.getPixel(c, r, pixel);

                                        for (int m = 0; m < pixelVal.length; m++) {
                                            if (pixelVal[m] > max) {
                                                max = pixelVal[m];
                                            }
                                        }
                                        sum += max;
                                    }
                                }
                                double avg = sum / (Math.round(geoCoordToImageCoordColRatio) * Math.round(geoCoordToImageCoordRowRatio));
                                if (applyColorMatrix) {
                                    //  System.out.println("values length: " + values.length);
                                    for (int z = 0; z < values.length; z++) {
                                        // System.out.println("Actual Value:" + avg);
                                        //System.out.println(minRange[z] + ":" + maxRange[z]);

                                        if (avg >= minRange[z] && avg <= maxRange[z]) {
                                            //System.out.println("Avg between " + minRange[z] + " and " + maxRange[z] + " Values:" + values[z]);
                                            avg = values[z];
                                            break;
                                        }

                                    }
                                }

                                STT stt = new STT();
                                ELocation eloc = new ELocation();
                                eloc.setLat(i);
                                eloc.setLon(j);
                                stt.setLoc(eloc);
                                stt.setTimestamp(new Date());

                                stt.setRawData(String.valueOf(avg));

                                double val = avg;
//                                if((avg>values[values.length-1]){
//                                    val=0;
//                                }
                                stt.setValue(val);
                                sttList.add(stt);
                                ccnt++;
                            }
                            //  System.out.println("Column count = " + ccnt);
                            rcnt++;
                        }
                        // System.out.println("Row Count =" + rcnt);

//                        double[][] transmatrix = { { 40.462234530, 36.608742073 },
//                                { 0.748263373, 0.222251869 },
//                                { -0.489894685, 1.208570277 },
//                                { 0.001507530, 0.000538853 },
//                                { 0.000193739, -0.000330355 },
//                                { 0.000799529, -0.000634850 },
//                                { -0.000000258, -0.000000979 },
//                                { 0.000000080, 0.000000062 },
//                                { -0.000000198, -0.000000100 },
//                                { -0.000002263, 0.000001772 }, };
//
//                        if(transmatrix != null){
//                            PlanarImage rectImage = createRectifiedImage();
//
//                        }

                        String mongoPath = "mongodb:mongoBean?database=" + Config.getProperty("DSDB") + "&collection=ds" + ds.getSrcID() + "&operation=insert";
                        exchange.getOut().setHeader("mPath", mongoPath);
                        exchange.getOut().setBody(sttList);
                        String operation = "avg";
                        exchange.getOut().setHeader("spatial_wrapper", operation);

                    }

                })
                .recipientList(header("mPath"));
    }


    private PlanarImage createRectifiedImage(PlanarImage Image,double[][] transMat, double scalingFactor, DataSource ds) {
        Point rectifiedPoint;

        // Get the image dimensions of the unrectified image
        int width = Image.getWidth();
        int height = Image.getHeight();

        LOGGER.debug("Width: " + width + ", Height: " + height);
        // Get an iterator for the image.
        RandomIter iterator = RandomIterFactory.create(Image, null);

        // Get the number of bands on the image.
        SampleModel smO = Image.getSampleModel();
        int nbandsO = smO.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixelO = new double[nbandsO];



        // Get an iterator for the image.
        int nCols = (int) (width * scalingFactor);// 515;
        int nRows = (int) (height * scalingFactor); //320;
        WritableRaster rasterPollenO = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_BYTE, nCols, nRows, nbandsO, new Point(0, 0));
        double u=0, v=0;
        for (int i = 0; i < nCols; i++) {
            for (int j = 0; j < nRows; j++) {
               // rectifiedPoint = getMatchingCoord(new Point(i + 1, j + 1));// coz
                // java
                // array
                // start
                // at
                // 0
                // matlab
                // its
                // 1.
//                double[][] transMat = {
//                        { 54.169184929, -5.542199557 },
//                        { 0.496176521, 0.203104483 },
//                        { -0.328315814, 0.868318964 },
//                        { 0.001014807, 0.000358314 },
//                        { 0.000245955, -0.000381097 },
//                        { 0.000422211, -0.000662529 },
//                        { -0.00000001, -0.000000585 },
//                        { 0.000000180, 0.000000555 },
//                        { -0.000000270, 0.000000078 },
//                        { -0.000001276, 0.000001363 }, };

                u = 1.0 * transMat[0][0] + (i+1) * transMat[1][0] + (j+1) * transMat[2][0] + (i+1)
                        * (j+1) * transMat[3][0] + (i+1) * (i+1) * transMat[4][0] + (j+1) * (j+1)
                        * transMat[5][0] + (j+1) * (i+1) * (i+1) * transMat[6][0] + (i+1) * (j+1)
                        * transMat[7][0] + (i+1) * (i+1) * (i+1) * transMat[8][0] + (j+1) * (j+1) * (j+1)
                        * transMat[9][0];

                v = 1.0 * transMat[0][1] + (i+1) * transMat[1][1] + (j+1) * transMat[2][1] + (i+1)
                        * (j+1) * transMat[3][1] + (i+1) * (i+1) * transMat[4][1] + (j+1) * (j+1)
                        * transMat[5][1] + (j+1) * (i+1) * (i+1) * transMat[6][1] + (i+1) * (j+1)
                        * transMat[7][1] + (i+1) * (i+1) * (i+1) * transMat[8][1] + (j+1) * (j+1) * (j+1)
                        * transMat[9][1];
                rectifiedPoint = new Point((int) Math.round(u), (int) Math.round(v));


                if (rectifiedPoint.x >= 1 && rectifiedPoint.x < width
                        && rectifiedPoint.y >= 1 && rectifiedPoint.y < height) {
                    iterator.getPixel(rectifiedPoint.x - 1,
                            rectifiedPoint.y - 1, pixelO);
                    rasterPollenO.setPixel(i, j, pixelO);
                    // log.info("setpixel: " + i + ", " + j + ", value " +
                    // pixelO[0] + ", " + pixelO[1] + ", " + pixelO[2] + ", " +
                    // pixelO[3]);
                } else {

                }
            }
        }
        //TODO
        SampleModel sModel2 = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_BYTE, nCols, nRows, nbandsO);

        // Try to create a compatible ColorModel - if the number of bands is
        // larger than 4, it will be null.
        ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

        // Create a TiledImage using the sample and color models.
        TiledImage rectPollenImage = new TiledImage(0, 0, nCols, nRows, 0, 0,
                sModel2, cModel2);

        // log.info(rasterPollenO.getMinX() + ", " + rasterPollenO.getMinY() );
        // Set the data of the tiled image to be the raster.
        rectPollenImage.setData(rasterPollenO);

        // Save the image to a file.
        try {
            ImageIO.write(rectPollenImage, "png", new File("Transformed_" +ds.getSrcID()+ ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rectPollenImage;
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }


}
