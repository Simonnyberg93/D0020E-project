package com.example.d0020e_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.boundingRect;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;

    private Rect objectBoundingRectangle  = new Rect(0,0,0,0);
    private Rect objectBoundingRectangle2 = new Rect(0,0,0,0);

    private class Box {
        Rect rectangle;
        Thread thread;

        public Box(Rect r, Thread t){
            this.rectangle = r;
            this.thread = t;
        }
    }

    private Box[] boxes = new Box[9];

    private int BOXWIDTH = 0;
    private int BOXHEIGHT = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;

    private SoundPlayer soundPlayer;

    private Mat frame1, blurred, hsv, mask;

    private final Scalar WHITE      = new Scalar( 255,255,255,0 );
    private final Scalar LIGHTGREEN = new Scalar( 29, 86, 6, 0 );
    private final Scalar DARKGREEN  = new Scalar( 64, 255, 255, 0 );

    private CameraActivity camAct = this;

    private class Search implements Runnable {

        LinkedBlockingQueue<Mat> queue = new LinkedBlockingQueue<Mat>();
        Point currentLocation = new Point(-1, -1);
        Point currentLocation2 = new Point(-1, -1);

        public Search(){
            new Thread(this).start();
        }

        public Point getCurrentLocation(){
            return currentLocation;
        }

        public Point getSecondLocation(){
            return currentLocation2;
        }


        public void addFrame(Mat frame){
            try {
                queue.put( frame );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while(true) {
                // block when que empty
                Mat frame1 = null;
                try {
                    // TODO: find better way for this
                    frame1 = queue.poll(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (frame1 != null) {
                    blurred = frame1.clone();
                    hsv = new Mat();
                    mask = new Mat();

                    Imgproc.GaussianBlur(frame1, blurred, new Size(11, 11), 0);
                    Imgproc.cvtColor( blurred, hsv, Imgproc.COLOR_BGR2HSV );

                    Core.inRange( hsv, LIGHTGREEN, DARKGREEN, mask);

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
                        objectBoundingRectangle = boundingRect( largestContourVec.get( 0 ) );
                        objectBoundingRectangle2 = boundingRect( largestContourVec.get( 1 ) );

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
                    if (coordinates.y < BOXWIDTH || coordinates.y > ( frameHeight - BOXWIDTH ) || coordinates2.y < BOXWIDTH || coordinates2.y > ( frameHeight - BOXWIDTH ) || top || top2 ){
                        for (int i = 0; i < boxes.length; i++) {
                            Thread t = boxes[i].thread;
                            Rect r = boxes[i].rectangle;
                            if (r.contains( coordinates ) || r.contains( coordinates2 ) ) {
                                if (t.getState() == Thread.State.NEW) {
                                    t.start();
                                } else if (t.getState() != Thread.State.TIMED_WAITING) {
                                    System.out.println( "STARTING THREAD: " + (i + 1) );
                                    try {
                                        t.join();
                                        boxes[i].thread = new Thread( runnable, "box" + (i + 1) );
                                        boxes[i].thread.start();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    }
                    hsv.release();
                    blurred.release();
                    mask.release();
                    temp.release();
                    heirarchy.release();
                }
            }
        }
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < boxes.length; i++){
                if (Thread.currentThread().getName() == boxes[i].thread.getName()){
                    soundPlayer.playSound( camAct, i );
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };


    private Search searchThread = new Search();

    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(CameraActivity.this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == BaseLoaderCallback.SUCCESS) {
                javaCameraView.enableView();
            } else {
                super.onManagerConnected( status );
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Might need this in order to ask for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 200);
        }

        setContentView(R.layout.camera_activity);
        javaCameraView = (JavaCameraView) findViewById(R.id.javaCameraView);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(CameraActivity.this);
        soundPlayer = new SoundPlayer( this, ( int[] ) getIntent().getSerializableExtra( "SoundProfile" ) );

        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button btnBack = findViewById( R.id.btnBack );
        btnBack.setOnClickListener( v -> {
            soundPlayer.onExit();
            startActivity( new Intent(CameraActivity.this, MainActivity.class));
            finish();
        } );

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frameHeight = height;
        frameWidth  = width;
        BOXHEIGHT = frameHeight / 4;
        BOXWIDTH = ( int ) Math.ceil( frameWidth / 6 );

        boxes[0] = new Box(new Rect(frameWidth - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box1"));
        boxes[1] = new Box(new Rect(frameWidth - (BOXWIDTH * 4), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box2"));
        boxes[2] = new Box(new Rect(frameWidth - (BOXWIDTH * 3), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box3"));
        boxes[3] = new Box(new Rect(frameWidth - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box4"));

        boxes[4] = new Box(new Rect(frameWidth - (BOXWIDTH * 5), frameHeight - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box5"));
        boxes[5] = new Box(new Rect(frameWidth - (BOXWIDTH * 4), frameHeight - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box6"));
        boxes[6] = new Box(new Rect(frameWidth - (BOXWIDTH * 3), frameHeight - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box7"));
        boxes[7] = new Box(new Rect(frameWidth - (BOXWIDTH * 2), frameHeight - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box8"));

        boxes[8] = new Box(new Rect(frameWidth - BOXHEIGHT, (frameHeight/2) - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH),new Thread(runnable, "box9"));
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // Add the current frame to queue in search for object thread
        searchThread.addFrame(frame1);

        // draw our sensor locations, this will be removed, we do not want to draw on every frame.
        for (Box box : boxes){
            Imgproc.rectangle(frame1, box.rectangle, WHITE);
        }
        // For development purposes we draw a circle around the tracked object
        Point coordinate = searchThread.getCurrentLocation();
        Point coordinate2 = searchThread.getSecondLocation();
        Imgproc.circle( frame1, coordinate, 20, WHITE );
        Imgproc.circle( frame1, coordinate2, 20, WHITE );
        return frame1;
    }

    public void releaseObjects() {
        frame1.release();
        mask.release();
        blurred.release();
        hsv.release();
    }

    @Override
    public void onCameraViewStopped() {
        releaseObjects();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture){ }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseObjects();
        if (javaCameraView != null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseObjects();
        if(javaCameraView != null) {
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onResume(){
        String TAG = "CameraActivity";
        super.onResume();
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV is working!");
            baseLoaderCallback.onManagerConnected(BaseLoaderCallback.SUCCESS);
        } else {
            Log.d(TAG,"Opencv is not working!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }
}