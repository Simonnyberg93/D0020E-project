package com.example.d0020e_project;

import static org.opencv.imgproc.Imgproc.boundingRect;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Trace;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;
    private LoopBox loopBox;
    private Box[] boxes = new Box[7];
    private SoundPlayer soundPlayer;
    private Mat frame1;
    private final Scalar WHITE      = new Scalar( 255,255,255,0 );
    private final Scalar BLUE = new Scalar( 0,0,255 );
    private final Scalar GREEN = new Scalar( 0,255,0 );
    private final Scalar RED = new Scalar( 255,0,0 );
    private CameraActivity camAct = this;
    private Search searchThread;

    private ImageView[] boxViews = new ImageView[boxes.length];

    ImageView loopIcon;
    private int loopColor = Color.parseColor("#99ffbb");

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

    @SuppressLint("ResourceType")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Might need this in order to ask for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 200);
        }

        requestWindowFeature( Window.FEATURE_NO_TITLE );//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.camera_activity);
        javaCameraView = findViewById(R.id.javaCameraView);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(CameraActivity.this);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        int[][] soundProfile = (int[][]) getIntent().getSerializableExtra( "SoundProfile" );
        int[] icons = new int[soundProfile.length];
        int[] tracks = new int[soundProfile.length];
        for(int i = 0; i < soundProfile.length; i++){
            tracks[i] = soundProfile[i][0];
            icons[i] = soundProfile[i][1];
        }

        soundPlayer = new SoundPlayer( this, tracks );

        Button btnBack = findViewById( R.id.btnBack );

        boxViews[0] = findViewById( R.id.imageView1 );
        boxViews[1] = findViewById( R.id.imageView2 );
        boxViews[2] = findViewById( R.id.imageView3 );
        boxViews[3] = findViewById( R.id.imageView4 );
        boxViews[4] = findViewById( R.id.imageView5 );
        boxViews[5] = findViewById( R.id.imageView6 );
        boxViews[6] = findViewById( R.id.imageView7 );

        for (int i = 0; i < boxViews.length; i++){
            boxViews[i].setImageResource( icons[i] );
        }

        loopIcon = findViewById(R.id.loopIcon);

        btnBack.setOnClickListener( v -> {
            searchThread.stopLoop();
            soundPlayer.onExit();
            startActivity( new Intent(CameraActivity.this, soundSelection.class));
            finish();
        } );

    }

    private int BOXWIDTH = 0;
    private int BOXHEIGHT = 0;
    private int frameHeight = 0;
    @Override
    public void onCameraViewStarted(int width, int height) {
        BOXHEIGHT = height / 4;
        BOXWIDTH = (int) Math.ceil(width / 6);
        frameHeight = height;

        // left
        boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer));
        boxes[1] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2 , 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer));
        boxes[2] = new Box(new Rect(width - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer));
        // right
        boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer));
        boxes[4] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer));
        boxes[5] = new Box(new Rect(width - (BOXWIDTH * 2), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer));
        // top
        boxes[6] = new Box(new Rect(width - BOXHEIGHT, (height /2) - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH), new LoopRunnable(6, soundPlayer));

        loopBox = new LoopBox(new Rect(0,  height /2 - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH));
        updateLoopIcon();
        //searchThread = new Search(boxes, loopBox, BOXWIDTH, height, this);

    }

    private Point currentLocation = new Point(-1,-1);
    private Point currentLocation2 = new Point(-1,-1);
    /* For now we just use a counter to make loopbutton more user friendly. */
    private int btnPressCount = 0;
    private int activeLoops = 0;

    public void trackObject(Mat frame){
        Mat blurred, hsv, mask;
        if (frame != null) {
            blurred = frame.clone();
            hsv = new Mat();
            mask = new Mat();
            Imgproc.GaussianBlur( frame, blurred, new Size( 11, 11 ), 0 );
            // phone
            //Imgproc.cvtColor( blurred, hsv, Imgproc.COLOR_RGB2HSV );
            // emulator
            Imgproc.cvtColor( blurred, hsv, Imgproc.COLOR_BGR2HSV );
            /* The main functions to track colour object. */
            Core.inRange( hsv, new Scalar( 70, 100, 100 ), new Scalar( 103, 255, 255 ), mask );
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
                                activeLoops++;
                                camAct.updateIcon(i);

                            }
                            else if ( l.isRunning() ) {
                                // Stop playing sound in loop
                                boxes[i].loop.stopLoop();
                                boxes[i].loop.block();
                                activeLoops--;
                                camAct.updateIcon(i);
                            }

                        } else if ( ( !l.isRunning() ) && ( !l.isPlaying() ) ) {
                            // Play sound once
                            if(l.getState() == Thread.State.NEW){ // if thread is not started yet, do so.
                                boxes[i].loop.start();
                            }
                            boxes[i].loop.unBlock();
                            boxes[i].loop.block(); // set block for next iteration
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

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat blurred, hsv, mask;
        // read first frame
        frame1 = inputFrame.rgba();
        // use when testing on (some) emulator's.
        Imgproc.cvtColor( frame1, frame1, Imgproc.COLOR_BGR2RGB );
        trackObject( frame1.clone() );
        /* Add the current frame to queue in search for object thread */
//        if (frame1 != null) {
//            searchThread.addFrame(frame1.clone());
//        }
        // draw our sensor locations, this will be removed, we do not want to draw on every frame.
        for (Box box : boxes){
            if ( box.loop.isRunning() ){
                Imgproc.rectangle(frame1, box.rectangle, BLUE);
            } else {
                Imgproc.rectangle(frame1, box.rectangle, WHITE);
            }
        }
        if (loopBox.isPressed()) {
            Imgproc.rectangle(frame1, loopBox.rectangle, GREEN);
        } else {
            Imgproc.rectangle(frame1, loopBox.rectangle, RED);
        }
       // Point coordinate = searchThread.getCurrentLocation();
       // Point coordinate2 = searchThread.getSecondLocation();
        // For development purposes we draw a circle around the tracked object
        Imgproc.circle( frame1, this.currentLocation, 20, WHITE );
        Imgproc.circle( frame1, this.currentLocation2, 20, WHITE );

        // make the image not mirrored
        Core.flip(frame1, frame1, 1);
        return frame1;
    }

    public void updateIcon(int i){
        if ( boxes[i].loop.isRunning() ){
            boxViews[i].setColorFilter(loopColor, PorterDuff.Mode.MULTIPLY);
        } else {
            boxViews[i].clearColorFilter();
        }
    }

    public void updateLoopIcon(){
        if (loopBox.isPressed()) {
            loopIcon.setColorFilter(Color.GREEN);
        } else {
            loopIcon.setColorFilter(Color.RED);
        }
    }

    public void releaseObjects() {
        //frame1.release();
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