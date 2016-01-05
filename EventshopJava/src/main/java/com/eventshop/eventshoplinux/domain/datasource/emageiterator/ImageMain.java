package com.eventshop.eventshoplinux.domain.datasource.emageiterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/**
 * Created by nandhiniv on 6/22/15.
 */
public class ImageMain {
    /** The main method. */
    private final static Logger log = LoggerFactory.getLogger(ImageMain.class);

    public static void main(String[] args) throws Exception {
        ImageMain imageMain = new ImageMain();
        imageMain.execute();


    }

    public void execute() throws Exception{
        //Read from URL and write to file out.gif
        URL imageURL = new URL("http://pollen.com/images/usa_map.gif");
        RenderedImage img = ImageIO.read(imageURL);
        String fileName = "/home/aravindh/out.gif";
        ImageIO.write(img, "gif",new File(fileName));

        PlanarImage im2 = (PlanarImage)JAI.create("FileLoad", fileName);

        // Creates out_1.gif
     //   PlanarImage rectImage = createRectifiedImage(im2);
        // Create proc_out2
        //PlanarImage procImage = PostProcessEmage(rectImage);

        PlanarImage procImage = PostProcessEmage(im2);


        double[][] dataGrid = PlanarImage2DataGrid(procImage, fileName);

        gridToEmage(dataGrid, "test");

    }

    public PlanarImage gridToEmage(double[][] dataGrid, String imageName)
            throws IOException {

        int width = dataGrid.length;
        int height = dataGrid.length;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                System.out.print(dataGrid[i][j] + " , ");
            }
            System.out.println();
        }
//        // creates a gray-scale image of the values
////        int width = dataGrid[0].length;
////        int height = dataGrid.length;
//
//        // Get the number of bands on the image.
//        URL imgURL = new URL(Config.getProperty("imgURL"));
//        PlanarImage dummyImage = JAI.create("URL", imgURL);// dummy loaded. date
//        // to be edited.
//        SampleModel sm = dummyImage.getSampleModel();
//        int nbands = sm.getNumBands();
//
//        // We assume that we can get the pixels values in a integer array.
//        double[] pixelTemp = new double[nbands];
//
//        // Get an iterator for the image.
//        RandomIterFactory.create(dummyImage, null);
//        WritableRaster rasterData = RasterFactory.createBandedRaster(
//                DataBuffer.TYPE_BYTE, width, height, nbands, new Point(0, 0));
//
//        for (int i = 0; i < height; i++) {
//            for (int j = 0; j < width; j++) {
//                dataGrid[i][j] = (dataGrid[i][j] <= 255) ? dataGrid[i][j] : 255;
//
//                pixelTemp[0] = dataGrid[i][j];
//                pixelTemp[1] = dataGrid[i][j];
//                pixelTemp[2] = dataGrid[i][j];
//
//                rasterData.setPixel(j, i, pixelTemp);
//            }
//        }
//
//        SampleModel sModel2 = javax.media.jai.RasterFactory
//                .createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height,
//                        nbands);
//
//        // Try to create a compatible ColorModel - if the number of bands is
//        // larger than 4, it will be null.
//        ColorModel cModel2 = PlanarImage.createColorModel(sModel2);
//
//        // Create a TiledImage using the sample and color models.
//        TiledImage rectImage = new TiledImage(0, 0, width, height, 0, 0,
//                sModel2, cModel2);
//
//        // Set the data of the tiled image to be the raster.
//        rectImage.setData(rasterData);
//
//        // Save the image on a file.
//        try {
//            ImageIO.write(rectImage, "jpg", new File(imageName + ".jpg"));
//            log.info("debug save image : " + imageName + ".jpg");
//
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            log.error("Error in rectifying  image");
//        }
//
//        return rectImage;
        return null;
    }

    public double[][] PlanarImage2DataGrid(PlanarImage img, String EmageType) {
        double[][] BucketArray = new double[img.getHeight()][img.getWidth()];

        int width = img.getWidth();
        int height = img.getHeight();

        // Get the number of bands on the image.
        SampleModel sm = img.getSampleModel();
        int nbands = sm.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixel = new double[nbands];

        double[][] colorMatrix = { { 2.04, 138.45, 2.21 },
                { 155.65, 252.84, 50.32 }, { 254.37, 254.27, 2.78 },
                { 254.30, 152.94, 0.09 }, { 253.93, 0.08, 0.00 },
                { 190.85, 190.35, 147.51 }, { 254.67, 254.98, 254.58 }, };

        // Get an iterator for the image.
        RandomIter iterator = RandomIterFactory.create(img, null);
        int ignoreColorSamplesBeyond = 0;
        if (ignoreColorSamplesBeyond == 0)
            ignoreColorSamplesBeyond = colorMatrix.length;

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
            log.info("Ignoring any binning. Going with gray values");
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

    public PlanarImage PostProcessEmage(PlanarImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // Get the number of bands on the image.
        SampleModel sm = img.getSampleModel();
        int nbands = sm.getNumBands();

        // We assume that we can get the pixels values in a integer array.
        double[] pixel = new double[nbands];

        // Get an iterator for the image.
        RandomIter iterator = RandomIterFactory.create(img, null);
        WritableRaster rasterImage = RasterFactory.createBandedRaster(
                DataBuffer.TYPE_BYTE, width, height, nbands,
                new Point(0, 0));

        double[] pixelNeighb = new double[nbands];
        double[] pixelblack = new double[nbands];
        for (int i = 0; i < nbands; i++) {
            pixelNeighb[i] = 0;
            pixelblack[i] = 0;
        }

        //TODO Using same file as no mask file
//            PlanarImage mask =(PlanarImage) JAI.create("FileLoad", "out_1.gif");
//            RandomIter iteratorMask = RandomIterFactory.create(mask, null);
//            double[] pixelMask = new double[mask.getSampleModel().getNumBands()];
//
//            for (int i = 0; i < width; i++) {
//                for (int j = 0; j < height; j++) {
//                    iteratorMask.getPixel(i, j, pixelMask);
//                    if (!isBlack(pixelMask)) {
//                        iterator.getPixel(i, j, pixel);
//                        if (isWhite(pixel)) {
//                            ;
//                        } else {
//                            rasterImage.setPixel(i, j, pixel);
//                        }
//                    } else {
//                        rasterImage.setPixel(i, j, pixelblack);
//                    }
//                }
//            }
        SampleModel sModel2 = RasterFactory.createBandedSampleModel(
                DataBuffer.TYPE_BYTE, width, height, nbands);

        // Try to create a compatible ColorModel - if the number of bands is
        // larger than 4, it will be null.
        ColorModel cModel2 = PlanarImage.createColorModel(sModel2);

        // Create a TiledImage using the sample and color models.
        TiledImage processedImage = new TiledImage(0, 0, width, height, 0,
                0, sModel2, cModel2);

        // Set the data of the tiled image to be the raster.
        processedImage.setData(rasterImage);

        // Save the image to a file.
        try {
            ImageIO.write(processedImage, "gif", new File("/home/aravindh/proc_" + "out2"
                    + ".gif"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        // Save the image on a file.
        return processedImage;
    }

    public boolean isBlack(double[] pixel) {
        boolean blackFlag = true;
        for (int i = 0; i < pixel.length; i++) {
            if (pixel[i] > 20)
                blackFlag = false;

        }
        return blackFlag;
    }

    public boolean isWhite(double[] pixel) {
        boolean whiteFlag = true;
        for (int i = 0; i < pixel.length; i++) {
            if (pixel[i] < 190)
                whiteFlag = false;

        }
        return whiteFlag;
    }

    private PlanarImage createRectifiedImage(PlanarImage Image) {
        Point rectifiedPoint;

        // Get the image dimensions of the unrectified image
        int width = Image.getWidth();
        System.out.println("Image Width:"+Image.getWidth());
        System.out.println("Image Height:"+Image.getHeight());
        int height = Image.getHeight();
        log.info("\t" + width + ", " + height);
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
            ImageIO.write(rectPollenImage, "gif", new File("/home/aravindh/out_1" + ".gif"));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return rectPollenImage;
    }

    private static Point getMatchingCoord(Point unrectPoint) {
        double x = unrectPoint.x;
        double y = unrectPoint.y;
        double u = 0.0, v = 0.0;
        double[][] transMat = {
                { 54.169184929, -5.542199557 }, // 52.169184929
                { 0.496176521, 0.203104483 }, // 0.506176521, .196
                { -0.328315814, 0.868318964 },// , .87
                { 0.001014807, 0.000358314 }, { 0.000245955, -0.000381097 },
                { 0.000422211, -0.000662529 },
                { -0.00000001, -0.000000585 }, // -0.000000121, -0.000000785
                { 0.000000180, 0.000000555 }, { -0.000000270, 0.000000078 },
                { -0.000001276, 0.000001363 }, };

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

        Point matchedPoint = new Point((int) Math.round(u), (int) Math.round(v));
        // log.debug(unrectPoint.x + ", " + unrectPoint.y + "=>" +
        // matchedPoint.x + ", " + matchedPoint.y);
        return matchedPoint;
    }
}