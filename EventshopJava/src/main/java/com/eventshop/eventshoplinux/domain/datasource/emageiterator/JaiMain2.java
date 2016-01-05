package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import com.eventshop.eventshoplinux.util.commonUtil.Config;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

/**
 * Created by aravindh on 7/24/15.
 */
public class JaiMain2 {

    public static void main(String[] args) throws Exception {
        JaiMain2 main = new JaiMain2();
        main.run();
    }

    private static Point getMatchingCoord(Point unrectPoint) {
        double x = unrectPoint.x;
        double y = unrectPoint.y;
        double u = 0.0, v = 0.0;
        double[][] transMat = {
                {54.169184929, -5.542199557}, // 52.169184929
                {0.496176521, 0.203104483}, // 0.506176521, .196
                {-0.328315814, 0.868318964},// , .87
                {0.001014807, 0.000358314}, {0.000245955, -0.000381097},
                {0.000422211, -0.000662529},
                {-0.00000001, -0.000000585}, // -0.000000121, -0.000000785
                {0.000000180, 0.000000555}, {-0.000000270, 0.000000078},
                {-0.000001276, 0.000001363},};

        u = 1.0 * transMat[0][0] + x * transMat[1][0] + y * transMat[2][0] + x
                * y * transMat[3][0] + x * x * transMat[4][0] + y * y
                * transMat[5][0] + y * x * x * transMat[6][0] + x * y
                * transMat[7][0] + x * x * x * transMat[8][0] + y * y * y
                * transMat[9][0];

        v = 1.0 * transMat[0][1] + x * transMat[1][1] + y * transMat[2][1] + x
                * y * transMat[3][1] + x * x * transMat[4][1] + y * y
                * transMat[5][1] + y * x * x * transMat[6][1] + x * y
                * transMat[7][1] + x * x * x * transMat[8][1] + y * y * y
                * transMat[9][1];

        /*double[][] ColMatAQI = {{46.22, 113.17, 64.82},
                {221.88, 232.50, 32.62}, {249.71, 102.66, 4.23},
                {248.93, 2.89, 3.44}, {153.07, 1.88, 76.04},
                {130.20, 8.82, 43.29}, {178.00, 178.00, 178.00},
                {191.00, 231.00, 255.00}, {0, 0, 0},};
        */
        double[][] ColMatAQI = {{48, 92, 46},
                {136, 238 , 38}, {242,244,11},
                {250,150,14}, {166,55,26},{0, 0, 0},};




        Point matchedPoint = new Point((int) Math.round(u), (int) Math.round(v));
        // log.debug(unrectPoint.x + ", " + unrectPoint.y + "=>" +
        // matchedPoint.x + ", " + matchedPoint.y);
        return matchedPoint;
    }

    private void run() throws Exception {
        PlanarImage pi = (PlanarImage) JAI.create("FileLoad", "main.gif");

        BufferedImage nImage = pi.getAsBufferedImage();

        BufferedImage filteredImage = new BufferedImage(nImage.getWidth(null), nImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
        Graphics g = filteredImage.getGraphics();
        g.drawImage(nImage, 0, 0, null);
        ImageIO.write(filteredImage, "png", new File("blackWhite2-.png"));


        PlanarImage rectImage = createRectifiedImage(pi);
        ImageIO.write(rectImage, "png", new File("blackWhite5.png"));
        double[][] grid = planarImage2DataGrid(rectImage);

        //PlanarImage finalImg = gridToEmage(grid, "new",rectImage);
        PlanarImage finalImg = (PlanarImage) JAI.create("FileLoad", "blackWhite2.png");
        ImageIO.write(finalImg, "png", new File("blackWhite6.png"));

    }

    private PlanarImage createRectifiedImage(PlanarImage Image) {
        Point rectifiedPoint;

        // Get the image dimensions of the unrectified image
        int width = Image.getWidth();
        int height = Image.getHeight();
        System.out.println("\t" + width + ", " + height);
        // Get an iterator for the image.
        RandomIter iterator = RandomIterFactory.create(Image, null);

        // Get the number of bands on the image.
        SampleModel smO = Image.getSampleModel();
        int nbandsO = smO.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixelO = new double[nbandsO];

        // Get an iterator for the image.
        int nCols = 515;
        int nRows = 320;
        WritableRaster rasterPollenO = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_BYTE, nCols, nRows, nbandsO, new Point(0, 0));

        for (int i = 0; i < nCols; i++) {
            for (int j = 0; j < nRows; j++) {
                rectifiedPoint = getMatchingCoord(new Point(i + 1, j + 1));// coz
                // java
                // array
                // start
                // at
                // 0
                // matlab
                // its
                // 1.
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
            ImageIO.write(rectPollenImage, "png", new File("final" + ".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rectPollenImage;
    }

    public double[][] planarImage2DataGrid(PlanarImage img) {
        double[][] BucketArray = new double[img.getHeight()][img.getWidth()];
        int width = img.getWidth();
        int height = img.getHeight();

        // Get the number of bands on the image.
        SampleModel sm = img.getSampleModel();
        int nbands = sm.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixel = new double[nbands];

        // Get an iterator for the image.
        RandomIter iterator = RandomIterFactory.create(img, null);
//        double[][] colorMatrix = { { 46.22, 113.17, 64.82 },
//                { 221.88, 232.50, 32.62 }, { 249.71, 102.66, 4.23 },
//                { 248.93, 2.89, 3.44 }, { 153.07, 1.88, 76.04 },
//                { 130.20, 8.82, 43.29 }, { 178.00, 178.00, 178.00 },
//                { 191.00, 231.00, 255.00 }, { 0, 0, 0 }, };
        double[][] colorMatrix = {{48, 92, 46},
                {136, 238 , 38}, {242,244,11},
                {250,150,14}, {166,55,26},{0, 0, 0},};
        int ignoreColorSamplesBeyond = colorMatrix.length;

        if (ignoreColorSamplesBeyond > -1) {




            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {

                    iterator.getPixel(i, j, pixel);

                    double curDiff = 999999999;
                    double diff = 0;
                    int curBucket = -1;
                    for (int i2 = 0; i2 < colorMatrix.length; i2++) {
                        for (int j2 = 0; j2 < Math.min(nbands,
                                colorMatrix[0].length); j2++) {
                            diff += (pixel[j2] - colorMatrix[i2][j2])
                                    * (pixel[j2] - colorMatrix[i2][j2]);
                        }
                        if (diff < curDiff) {
                            curDiff = diff;
                            curBucket = i2;
                        }
                        // log.info("i: " + i2 + ", val: " + diff);
                        diff = 0;
                    }
                    if (curBucket >= ignoreColorSamplesBeyond)
                        curBucket = -1;
                    BucketArray[j][i] = (curBucket + 1);// *255 /
                    // (colorMatrix.length);
                    // actually show the
                    // categories not
                    // normalized to 255
                    // now.
                    // if(curBucket >= 0) log.info(curBucket + "," +
                    // BucketArray[j][i]);
                }
            }
        } else {
           for (int i = 0; i < height; i++) {

                for (int j = 0; j < width; j++) {
                    int sumChannels = 0;

                    iterator.getPixel(j, i, pixel);
                    for (int j2 = 0; j2 < nbands; j2++) {
                        sumChannels += pixel[j2];
                    }
                    BucketArray[i][j] = (sumChannels) / (nbands);
                }
            }
        }
        return BucketArray;
    }

    public PlanarImage gridToEmage(double[][] dataGrid, String imageName, PlanarImage image)
            throws IOException {
        // creates a gray-scale image of the values
        int width = dataGrid[0].length;
        int height = dataGrid.length;

        // Get the number of bands on the image.
//        URL imgURL = new URL(Config.getProperty("imgURL"));
//        PlanarImage dummyImage = JAI.create("URL", imgURL);// dummy loaded. date
        // to be edited.
        SampleModel sm = image.getSampleModel();
        int nbands = sm.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixelTemp = new double[nbands];

        // Get an iterator for the image.
        RandomIterFactory.create(image, null);
        WritableRaster rasterData = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_BYTE, width, height, nbands, new Point(0, 0));

        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                dataGrid[i][j] = (dataGrid[i][j] <= 255) ? dataGrid[i][j] : 255;

                pixelTemp[0] = dataGrid[i][j];
                pixelTemp[1] = dataGrid[i][j];
                pixelTemp[2] = dataGrid[i][j];

                rasterData.setPixel(j, i, pixelTemp);
            }
        }

        SampleModel sModel2 = javax.media.jai.RasterFactory
                .createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height,
                        nbands);

        // Try to create a compatible ColorModel - if the number of bands is
        // larger than 4, it will be null.
        ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

        // Create a TiledImage using the sample and color models.
        TiledImage rectImage = new TiledImage(0, 0, width, height, 0, 0,
                sModel2, cModel2);

        // Set the data of the tiled image to be the raster.
        rectImage.setData(rasterData);

        // Save the image on a file.
        try {
            ImageIO.write(rectImage, "jpg", new File(imageName + ".jpg"));
            System.out.println("debug save image : " + imageName + ".jpg");

        } catch (IOException e) {
            System.out.println(e.getMessage());
            System.out.println("Error in rectifying  image");
        }

        return rectImage;
    }

}

