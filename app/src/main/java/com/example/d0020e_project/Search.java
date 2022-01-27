package com.example.d0020e_project;

import static org.opencv.imgproc.Imgproc.boundingRect;
import static org.opencv.imgproc.Imgproc.filter2D;

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
    // private final Scalar DARKGREEN  = new Scalar( 64, 255, 255, 0 );

    private LinkedBlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
    private Point currentLocation = new Point( -1, -1 );
    private Point currentLocation2 = new Point( -1, -1 );
    private Box[] boxes;
    private LoopBox loopBox;
    private boolean run = true;
    private SoundPlayer soundPlayer;
    private final int BOXWIDTH, frameHeight;
    private int activeLoops = 0;
    private Runnable runnable;

    private Thread t1;
    private Thread t2;
    private Thread t3;

    private Thread[] threads = {t1, t2, t3};




    public Search( Box[] b, SoundPlayer s, LoopBox lb, int boxW, int frameH, Runnable r) {
        this.BOXWIDTH = boxW;
        this.frameHeight = frameH;
        this.boxes = b;
        this.soundPlayer = s;
        this.loopBox = lb;
        this.runnable = r;
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
                    Rect objectBoundingRectangle = boundingRect( largestContourVec.get( 0 ) );
                    Rect objectBoundingRectangle2 = boundingRect( largestContourVec.get( 1 ) );

                    coordinates.x = objectBoundingRectangle.x + objectBoundingRectangle.width / 2;
                    coordinates.y = objectBoundingRectangle.y + objectBoundingRectangle.height / 2;

                    coordinates2.x = objectBoundingRectangle2.x + objectBoundingRectangle2.width / 2;
                    coordinates2.y = objectBoundingRectangle2.y + objectBoundingRectangle2.height / 2;
                    // this is just for development purposes
                    this.currentLocation = coordinates;
                    this.currentLocation2 = coordinates2;
                }
                // is the object in topbox?
                boolean top = boxes[boxes.length - 1].rectangle.contains( coordinates );
                boolean top2 = boxes[boxes.length - 1].rectangle.contains( coordinates2 );

                boolean loop = loopBox.rectangle.contains( coordinates );
                boolean loop2 = loopBox.rectangle.contains( coordinates2 );

                if (loop || loop2) {
                    loopBox.press();
                }

                if (coordinates.y < BOXWIDTH || coordinates.y > ( frameHeight - BOXWIDTH ) || coordinates2.y < BOXWIDTH || coordinates2.y > ( frameHeight - BOXWIDTH ) || top || top2) {
                    for (int i = 0; i < boxes.length; i++) {
                        Thread t = boxes[i].thread;
                        Rect r = boxes[i].rectangle;
                        if (r.contains( coordinates ) || r.contains( coordinates2 )) {
                            System.out.println("Active Loops: " + activeLoops);
                            if (loopBox.isPressed() ) {
                                if ( !( boxes[i].loop.isRunning()) && (activeLoops < 3) ) {
                                    boxes[i].loop.startRun();
                                    for (int j = 0; j < threads.length; j++){
                                        if ( (threads[j] == null) || (threads[j].getState() == Thread.State.TERMINATED) ){
                                            boxes[i].loop.startRun();
                                            threads[j] = new Thread(boxes[i].loop, String.valueOf( i ) );
                                            threads[j].start();
                                            increaseActiveloops();
                                            break;
                                        } else if (threads[j].getState() != Thread.State.TIMED_WAITING) {
                                            try {
                                                threads[j].join();
                                                boxes[i].loop.startRun();
                                                threads[j] = new Thread( boxes[i].loop, String.valueOf( i ) );
                                                threads[j].start();
                                                increaseActiveloops();
                                            } catch (InterruptedException e) {
                                                e.printStackTrace();
                                            }
                                            break;
                                        }
                                    }
                                }
                                else if ( boxes[i].loop.isRunning() ) {
                                    for (int z = 0; z < threads.length; z++){
                                        if (threads[z] != null) {
                                            if (threads[z].getName() == String.valueOf( i )) {
                                                decreaseActiveloops();
                                                boxes[i].loop.stopLoop();
                                                threads[z] = null;
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (t.getState() == Thread.State.NEW) {
                                    t.start();
                                } else if (t.getState() != Thread.State.TIMED_WAITING) {
                                    try {
                                        t.join();
                                        boxes[i].thread = new Thread( runnable, "box" + ( i + 1 ) );
                                        boxes[i].thread.start();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            break;
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
