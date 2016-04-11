package com.unicorn;

import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

/**
 * Created by nandhini on 15/3/16.
 */
public class Main {

//    static {
//        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);
//    }

    public static void main(String args[]){
        System.out.println("In main");
        System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

        new Main().run();
    }

    public void run(){
        System.out.println("In run");

        //Load images to compare
        Mat img1 = Highgui.imread("html.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        Mat img2 = Highgui.imread("flash.png", Highgui.CV_LOAD_IMAGE_GRAYSCALE);

        MatOfKeyPoint keypoints1 = new MatOfKeyPoint();
        MatOfKeyPoint keypoints2 = new MatOfKeyPoint();
        Mat descriptors1 = new Mat();
        Mat descriptors2 = new Mat();

//Definition of ORB keypoint detector and descriptor extractors
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);

//Detect keypoints
        detector.detect(img1, keypoints1);
        detector.detect(img2, keypoints2);
//Extract descriptors
        extractor.compute(img1, keypoints1, descriptors1);
        extractor.compute(img2, keypoints2, descriptors2);

//Definition of descriptor matcher
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

//Match points of two images
        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1,descriptors2 ,matches);

    }
}
