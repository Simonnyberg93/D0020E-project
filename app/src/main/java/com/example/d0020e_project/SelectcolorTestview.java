package com.example.d0020e_project;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
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
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class SelectcolorTestview extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;
    private LoopBox loopBox;
    private Box[] boxes = new Box[9];
    private SoundPlayer soundPlayer;
    private Mat frame1;
    private final Scalar WHITE      = new Scalar( 255,255,255,0 );
    private final Scalar BLUE = new Scalar( 0,0,255 );
    private final Scalar GREEN = new Scalar( 0,255,0 );
    private final Scalar RED = new Scalar( 255,0,0 );
    private Scalar lowerCR;     //lower color-range
    private Scalar upperCR;     //upper color-range
    private SelectcolorTestview camAct = this;
    private ColortestSearch searchThread;


    BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(SelectcolorTestview.this) {
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

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.activity_selectcolor_testview);
        javaCameraView = findViewById(R.id.javaCameraView);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(SelectcolorTestview.this);

        upperCR = getUpperCR(getIntent().getStringExtra( "colorKey" ));
        lowerCR = getLowerCR(getIntent().getStringExtra( "colorKey" ));

        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button btnBack = findViewById( R.id.btnBack );
        btnBack.setOnClickListener( v -> {
            searchThread.stopLoop();
            startActivity( new Intent(SelectcolorTestview.this, SoundSelection.class));
            finish();
        } );

    }

    //    private final Scalar LIGHTGREEN = new Scalar(64, 255, 255, 0);
    //    private final Scalar DARKGREEN  = new Scalar(29, 86, 6, 0);

    //Set lightcolor and darkcolor to the colors that are chosen in settings activity.
    public Scalar getLowerCR(String colorKey){       //lower color range
        switch(colorKey){
            case "Green":                               //works good for "normal" green
                return new Scalar(50, 125, 115);
            case "Orange":                              //works fine for "neon" orange
                return new Scalar(1, 140, 70);
            case "Blue":                                //this is bright blue
                return new Scalar(90, 195, 90);
            case "Pink":                                //Works well for neon pink
                return new Scalar(150, 140, 125);
            case "Yellow":                              //Good tuning for "yellow" yellow, not optimized for "green" yellow
                return new Scalar(23, 140, 125);
            case "Red":                                 //Really difficult to fine tune to "ignore" skin nuances, maybe remove
                return new Scalar(171, 200, 170);
            default:
                return new Scalar(100, 170, 125);
        }
    }

    public Scalar getUpperCR(String colorKey){       //upper color range
        switch(colorKey){
            case "Green":
                return new Scalar(70, 255, 255);
            case "Orange":
                return new Scalar(13, 255, 255);
            case "Blue":
                return new Scalar(127, 255, 255);
            case "Pink":
                return new Scalar(170, 215, 255);
            case "Yellow":
                return new Scalar(43, 255, 255);
            case "Red":
                return new Scalar(179, 255, 255);
            default:
                return new Scalar(0, 0, 0, 0);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        int BOXHEIGHT = height / 4;
        int BOXWIDTH = (int) Math.ceil(width / 6);
        searchThread = new ColortestSearch(BOXWIDTH, height, lowerCR, upperCR);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // use when testing on (some) emulator's.
       // Imgproc.cvtColor( frame1, frame1, Imgproc.COLOR_BGR2RGB );
        /* Add the current frame to queue in search for object thread */
        if (frame1 != null) {
            searchThread.addFrame(frame1.clone());
        }
        Point coordinate = searchThread.getCurrentLocation();
        // For development purposes we draw a circle around the tracked object
        Imgproc.circle( frame1, coordinate, 20, WHITE );

        // make the image not mirrored
        Core.flip(frame1, frame1, 1);
        return frame1;
    }

    public void releaseObjects() {
        frame1.release();
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