package com.example.d0020e_project;

import static org.opencv.imgproc.Imgproc.boundingRect;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Search implements Runnable {

    // TENNISBALL = lower: (70, 100, 100), upper: (103, 255, 255)
    private final Scalar LIGHTGREEN = new Scalar( 70, 100, 100 );
    private final Scalar DARKGREEN = new Scalar( 103, 255, 255 );

    // Normal green?
    // private final Scalar LIGHTGREEN = new Scalar( 29, 86, 6, 0 );
     //private final Scalar DARKGREEN  = new Scalar( 64, 255, 255, 0 );

    private LinkedBlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
    private Point currentLocation = new Point( -1, -1 );
    private Point currentLocation2 = new Point( -1, -1 );
    private Box[] boxes;
    private LoopBox loopBox;
    private boolean run = true;
    private final int BOXWIDTH, frameHeight;
    private int activeLoops = 0;
    /* For now we just use a counter to make loopbutton more user friendly. */
    private int btnPressCount = 0;

    private CameraActivity camAct;

    public Search( Box[] b, LoopBox lb, int boxW, int frameH, CameraActivity camAct) {
        this.BOXWIDTH = boxW;
        this.frameHeight = frameH;
        this.boxes = b;
        this.loopBox = lb;
        new Thread( this ).start();
        this.camAct = camAct;
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

    @Override
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
                Imgproc.cvtColor( blurred, hsv, Imgproc.COLOR_BGR2HSV );

                /* The main functions to track colour object. */
                Core.inRange( hsv, LIGHTGREEN, DARKGREEN, mask );
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
                // is an object in topbox?
                boolean top = boxes[boxes.length - 1].rectangle.contains( coordinates );
                boolean top2 = boxes[boxes.length - 1].rectangle.contains( coordinates2 );

                boolean loop = loopBox.rectangle.contains( coordinates );
                boolean loop2 = loopBox.rectangle.contains( coordinates2 );

                boolean left = (coordinates.y < BOXWIDTH) || coordinates.y > ( frameHeight - BOXWIDTH );
                boolean rightOrTop = (coordinates2.y < BOXWIDTH) || coordinates2.y > ( frameHeight - BOXWIDTH ) || top || top2;

                if ((loop || loop2)) {
                    if(btnPressCount == 0) {
                        loopBox.press();
                        btnPressCount = 4;
                        camAct.updateLoopIcon();
                    } else {
                        btnPressCount--;
                    }
                }

                if (left || rightOrTop) {
                    for (int i = 0; i < boxes.length; i++) {
                        Rect r = boxes[i].rectangle;
                        LoopRunnable l = boxes[i].loop;
                        if (r.contains( coordinates ) || r.contains( coordinates2 )) {
                            if (loopBox.isPressed() ) {
                                if ( !( l.isRunning()) && (activeLoops < 3) ) {
                                    // Start playing sound in loop
                                    if(l.getState() == Thread.State.NEW){ // if thread is not started yet, do so.
                                        boxes[i].loop.start();
                                    }
                                    boxes[i].loop.startLoop();
                                    boxes[i].loop.unBlock();
                                    increaseActiveloops();
                                    camAct.updateIcon(i);

                                }
                                else if ( l.isRunning() ) {
                                    // Stop playing sound in loop
                                    boxes[i].loop.stopLoop();
                                    boxes[i].loop.block();
                                    decreaseActiveloops();
                                    camAct.updateIcon(i);
                                }

                            } else if ( ( !l.isRunning() ) && ( !l.isPlaying() ) ) {
                                // Play sound once
                                if(l.getState() == Thread.State.NEW){ // if thread is not started yet, do so.
                                    boxes[i].loop.start();
                                }
                                boxes[i].loop.unBlock();
                               // boxes[i].loop.showHit( camAct, i );
                                boxes[i].loop.block(); // set block for next iteration
                               // boxes[i].loop.hitDone( camAct, i );
                            }
                        }
                    }
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
