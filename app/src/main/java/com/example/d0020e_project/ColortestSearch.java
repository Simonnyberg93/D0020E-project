package com.example.d0020e_project;

import static org.opencv.imgproc.Imgproc.boundingRect;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ColortestSearch implements Runnable {

    // TENNISBALL = lower: (70, 100, 100), upper: (103, 255, 255)
    //private final Scalar LIGHTGREEN = new Scalar( 70, 100, 100 );
    //private final Scalar DARKGREEN = new Scalar( 103, 255, 255 );
    // Normal green?
    private Scalar upperCR; //upper color range
    private Scalar lowerCR;

    private LinkedBlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
    private Point currentLocation = new Point( -1, -1 );
    private Point currentLocation2 = new Point( -1, -1 );
    private boolean run = true;
    private final int BOXWIDTH, frameHeight;
    private int activeLoops = 0;

    public ColortestSearch(int boxW, int frameH, Scalar lowC, Scalar uppC) {
        this.BOXWIDTH = boxW;
        this.frameHeight = frameH;
        this.lowerCR = lowC;
        this.upperCR = uppC;
        new Thread( this ).start();
    }

    public Point getCurrentLocation() {
        return currentLocation;
    }

    public Point getSecondLocation() {
        return currentLocation2;
    }

    public void addFrame( Mat frame ) {
        try {
            queue.put( frame );
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    synchronized boolean isRunning() {
        return run;
    }

    synchronized void stopLoop() {
        run = false;
    }

    synchronized void increaseActiveloops(){
        this.activeLoops++;
    }

    synchronized void decreaseActiveloops(){
        if (activeLoops > 0){
            this.activeLoops--;
        }

    }

    public void run() {
        Mat blurred, hsv, mask;
        while (isRunning()) {
            // block when que empty
            Mat frame = null;
            try {
                // TODO: find better way for this
                frame = queue.poll( 200, TimeUnit.MILLISECONDS );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (frame != null) {
                blurred = frame.clone();
                hsv = new Mat();
                mask = new Mat();
                Imgproc.GaussianBlur( frame, blurred, new Size( 11, 11 ), 0 );
                Imgproc.cvtColor( blurred, hsv, Imgproc.COLOR_RGB2HSV );

                /* The main functions to track colour object. */
                Core.inRange( hsv, lowerCR, upperCR, mask );   // colormap: HSV
                Imgproc.erode( mask, mask, new Mat() );
                Imgproc.dilate( mask, mask, new Mat() );

                Mat temp = new Mat();
                        mask.copyTo( temp );
                List<MatOfPoint> contours = new ArrayList<>();
                Mat heirarchy = new Mat();
                Imgproc.findContours( temp, contours, heirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE ); // all external contours

                Point coordinates = new Point();
                Point coordinates2 = new Point();
                if (contours.size() > 1) {
                    //the largest contour is found at the end of the contours vector
                    //we will simply assume that the biggest contour is the object we are looking for.
                    List<Mat> largestContourVec = new ArrayList<>();
                    largestContourVec.add( contours.get( contours.size() - 1 ) );
                    largestContourVec.add( contours.get( 0 ) );
                    //largestContourVec.add( contours.get( contours.size() - 2 ) );

                    //make a bounding rectangle around the largest contour then find its centroid
                    //this will be the object's final estimated position.
                    coordinates = boundingRect( largestContourVec.get( 0 ) ).middle();
                    coordinates2 = boundingRect( largestContourVec.get( 1 ) ).middle();

                    // this is just for development purposes
                    this.currentLocation = coordinates;
                    this.currentLocation2 = coordinates2;
                }
                hsv.release();
                blurred.release();
                mask.release();
                temp.release();
                heirarchy.release();
                frame.release();
            }
        }
    }
}
