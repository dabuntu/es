package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import javax.imageio.ImageIO;
import javax.media.jai.*;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.operator.MedianFilterDescriptor;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.net.URL;

/**
 * Created by nandhiniv on 7/21/15.
 */
public class JaiMain {

    static {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
    }

    public static void main(String[] args) throws Exception {
        JaiMain main = new JaiMain();
        main.run();
    }

    private void run() throws Exception {
     //   Imgproc.
        URL imageURL = new URL("http://pollen.com/images/usa_map.gif");
        //URL imageURL = new URL("http://www.goes.noaa.gov/GIFS/HUIR.JPG");

        //Read the file and store it locally
        RenderedImage img1 = ImageIO.read(imageURL);
        String fileName = "main.gif";
        ImageIO.write(img1, "gif", new File(fileName));


        //Read the above file and create Gray scale image
        File file = new File(fileName);
        BufferedImage orginalImage = ImageIO.read(file);
        BufferedImage blackAndWhiteImg = new BufferedImage(
                orginalImage.getWidth(), orginalImage.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = blackAndWhiteImg.createGraphics();
        graphics.drawImage(orginalImage, 0, 0, null);
        ImageIO.write(blackAndWhiteImg, "png", new File("blackWhite-.png"));

        //Read the above file and enrich it using Median Mask
        PlanarImage planarImage = (PlanarImage) JAI.create("FileLoad", "blackWhite-.png");
        ROIShape in1 = new ROIShape(planarImage.getBounds());
        ParameterBlock pb1 = new ParameterBlock();
        pb1.addSource(planarImage);
        pb1.add(in1);
        pb1.add(MedianFilterDescriptor.MEDIAN_MASK_SQUARE);
        pb1.add(3);
        BufferedImage nImage = planarImage.getAsBufferedImage();
        BufferedImage filteredImage = new BufferedImage(nImage.getWidth(null), nImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = filteredImage.getGraphics();
        g.drawImage(nImage, 0, 0, null);
        ImageIO.write(filteredImage, "png", new File("blackWhite2-.png"));

        System.out.println(planarImage.getWidth());
        System.out.println(planarImage.getHeight());


//Accessing all the pixels using Raster Image example


        int width = planarImage.getWidth();
        int height = planarImage.getHeight();

        int[] pixel = new int[5];
        RandomIter iterator = RandomIterFactory.create(planarImage, null);


        float[] kernelMatrix = {-1, -2, -1,
                0, 0, 0,
                1, 2, 1};
        KernelJAI kernel = new KernelJAI(3, 3, kernelMatrix);
        PlanarImage output = JAI.create("convolve", filteredImage, kernel);
        ImageIO.write(output, "png", new File("blackWhite3-.png"));

        File file2 = new File("blackWhite3-.png");
//

        PlanarImage finaloutput = (PlanarImage) JAI.create("FileLoad", "blackWhite3-.png");


        int[] pixel1 = new int[5];


        RandomIter edgeIter = RandomIterFactory.create(finaloutput, null);

        int edgeHeight = finaloutput.getHeight();
        int edgeWidth = finaloutput.getWidth();
        System.out.println("Edge height: " + edgeHeight);
        System.out.println("Edge width:" + edgeWidth);
        boolean[] edgeIdentification = new boolean[edgeWidth * edgeHeight];
        int cnt = 0;
        for (int i = 0; i < edgeHeight; i++) {
            for (int j = 0; j < edgeWidth; j++) {
                edgeIter.getPixel(j, i, pixel1);
                System.out.println("I was here...");
                if (pixel1[0] == 255) {
                    edgeIdentification[cnt] = true;
                } else {
                    edgeIdentification[cnt] = false;
                }
                cnt++;
            }
        }
//
//        Raster finalRaster = finaloutput.getData();
//        int[] pixels = new int[(nbands * (edgeWidth) * (edgeHeight))];
//        System.out.println(pixels.length);
//        System.out.println();
//        finalRaster.getPixels(0, 0, edgeWidth, edgeHeight, pixels);
//        int offset;
//        for (int h = 0; h < edgeHeight; h++)
//            for (int w = 0; w < edgeWidth; w++) {
//                offset = h * edgeWidth * nbands + w * nbands;
//                System.out.print("at (" + w + "," + h + "): ");
//                for (int band = 0; band < nbands; band++)
//                    System.out.print(pixels[offset + band] + " ");
//                System.out.println();
//            }


        for (int h = 0; h < height; h++)
            for (int w = 0; w < width; w++) {
                iterator.getPixel(w, h, pixel);
                System.out.print("at (" + w + "," + h + "): ");
              //  for (int band = 0; band < nbands; band++)
                for (int band = 0; band < 1; band++)
                    System.out.print(pixel[band] + " ");
                System.out.println();
            }

        double nelat = 50;
        double nelong = -66;
        double swlat = 21.5;
        double swlong = -129.5;
        double latunit = 0.5;
        double longunit = 0.5;
        //double[][] coordinates = new double[][];
        int rows = (int) ((nelat - swlat) / latunit);
        int cols = (int) ((nelong - swlong) / longunit);
        System.out.println("Rows: " + rows);
        System.out.println("Cols" + cols);
        double[] image = new double[rows * cols];
        double avgOfOneCoord = 0;
        //get Coordinates
        int rcnt = 0, ccnt = 0;
        int geoCoordToImageCoordColRatio = width / cols;
        int geoCoordToImageCoordRowRatio = height / rows;

        System.out.println("Geo Image Col Ratio " + geoCoordToImageCoordColRatio);
        System.out.println("Geo Image Row Ratio " + geoCoordToImageCoordRowRatio);

        int imageCnt = 0;
        int pixelVal = 0;
        int currHeight = 0;
        int currWidth = 0;
        for (double i = nelat; i > swlat; i = i - latunit) {
            ccnt = 0;
            for (double j = swlong; j < nelong; j = j + longunit) {
                double sum = 0;
                for (int l = 0; l < geoCoordToImageCoordRowRatio; l++) {
                    for (int k = 0; k < geoCoordToImageCoordColRatio; k++) {
                        if ((edgeIter.getPixel((ccnt * geoCoordToImageCoordColRatio + k), ((rcnt * geoCoordToImageCoordRowRatio) + l), pixel1)[0]) == 255) {

                            currHeight = (rcnt * geoCoordToImageCoordRowRatio) + l;
                            currWidth = (ccnt * geoCoordToImageCoordColRatio) + k;
                            //pixelVal = (edgeIter.getPixel(currWidth + 1, currHeight, pixel1)[0] + edgeIter.getPixel(currWidth - 1, currHeight, pixel1)[0] + edgeIter.getPixel(currWidth, currHeight + 1, pixel1)[0] + edgeIter.getPixel(currWidth, currHeight - 1, pixel1)[0] + edgeIter.getPixel(currWidth + 1, currHeight + 1, pixel1)[0] + edgeIter.getPixel(currWidth + 1, currHeight - 1, pixel1)[0] + edgeIter.getPixel(currWidth - 1, currHeight + 1, pixel1)[0] + edgeIter.getPixel(currWidth - 1, currHeight - 1, pixel1)[0]) / 8;
                            System.out.println("I was here..........");
                            pixelVal=0;
                        } else {
                            pixelVal = iterator.getPixel((ccnt * geoCoordToImageCoordColRatio + k), ((rcnt * geoCoordToImageCoordRowRatio) + l), pixel)[0];
                            sum += pixel[0];
                        }
                    }
                }
                avgOfOneCoord = sum / (geoCoordToImageCoordColRatio * geoCoordToImageCoordRowRatio);
                if (avgOfOneCoord == 255) {
                    image[imageCnt] = 0;
                } else {
                    image[imageCnt] = round(255 - avgOfOneCoord, 2);
                }
                imageCnt++;
                ccnt++;

            }
            System.out.println("Column count = " + ccnt);
            rcnt++;
        }
        System.out.println("Row Count =" + rcnt);


        for (int i = 0; i < image.length; i++) {
            System.out.print(image[i] + ",");
        }

        System.out.println("image size" + image.length);

        //For each cordinates get average of pixels.
    }

    private double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}

