package com.example.d0020e_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.Collection;
import java.util.HashMap;

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

    ImageView box1View, box2View, box3View,box4View,box5View,box6View,box7View;
    ImageView loopIcon;
    int loopColor = Color.parseColor("#99ffbb");
    int loopIconOn = Color.parseColor("#33cc33");
    int loopIconOff = Color.parseColor("#ff3300");

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

        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar

        setContentView(R.layout.camera_activity);
        javaCameraView = findViewById(R.id.javaCameraView);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(CameraActivity.this);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        HashMap<Integer, Integer> soundProfile = (HashMap<Integer, Integer> ) getIntent().getSerializableExtra( "SoundProfile" );
        Integer[] icons = soundProfile.values().toArray( new Integer[0] );

        soundPlayer = new SoundPlayer( this, soundProfile.keySet().toArray( new Integer[0] ) );

        Button btnBack = findViewById( R.id.btnBack );

        box1View = findViewById( R.id.imageView1 );
        box2View = findViewById( R.id.imageView2 );
        box3View = findViewById( R.id.imageView3 );
        box4View = findViewById( R.id.imageView4 );
        box5View = findViewById( R.id.imageView5 );
        box6View = findViewById( R.id.imageView6 );
        box7View = findViewById( R.id.imageView7 );

        box1View.setImageResource( icons[6] );
        box1View.setImageResource( icons[5] );
        box1View.setImageResource( icons[4] );
        box1View.setImageResource( icons[3] );
        box1View.setImageResource( icons[2] );
        box1View.setImageResource( icons[1] );
        box1View.setImageResource( icons[0] );


        loopIcon = findViewById(R.id.loopIcon);

        btnBack.setOnClickListener( v -> {
            searchThread.stopLoop();
            soundPlayer.onExit();
            startActivity( new Intent(CameraActivity.this, soundSelection.class));
            finish();
        } );

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        int BOXHEIGHT = height / 4;
        int BOXWIDTH = (int) Math.ceil(width / 6);

        boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer));
        boxes[1] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2 , 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer));
       // boxes[2] = new Box(new Rect(width - (BOXWIDTH * 3), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(2, soundPlayer));
        boxes[2] = new Box(new Rect(width - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer));

        boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer));
        boxes[4] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer));
       // boxes[6] = new Box(new Rect(width - (BOXWIDTH * 3), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(6, soundPlayer));
        boxes[5] = new Box(new Rect(width - (BOXWIDTH * 2), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer));

        // left
        box1View.setX( 0f );
        box1View.setY( (width - (BOXWIDTH * 5)) + 120 );

        box2View.setX( 0f );
        box2View.setY( (width - (BOXWIDTH * 4)) + 120 );

        box3View.setX( 0f );
        box3View.setY( (width - (BOXWIDTH * 3)) + 120 );

        // right
        box4View.setX( height - 50);
        box4View.setY( (width - (BOXWIDTH * 5)) + 120 );

        box5View.setX( height - 50);
        box5View.setY( (width - (BOXWIDTH * 4)) + 120 );

        box6View.setX( height - 50);
        box6View.setY( (width - (BOXWIDTH * 3)) + 120 );

        // top
        box7View.setX( height /2 - 50);
        box7View.setY( 0f );

        loopIcon.setX( height /2 + 20);
        loopIcon.setY( width + BOXWIDTH);

        boxes[6] = new Box(new Rect(width - BOXHEIGHT, (height /2) - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH), new LoopRunnable(6, soundPlayer));
        loopBox = new LoopBox(new Rect(0,  height /2 - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH));
        updateLoopIcon();
        searchThread = new Search(boxes, loopBox, BOXWIDTH, height, this);

    }


    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // use when testing on (some) emulator's.
        Imgproc.cvtColor( frame1, frame1, Imgproc.COLOR_BGR2RGB );

        /* Add the current frame to queue in search for object thread */
        if (frame1 != null) {
            searchThread.addFrame(frame1.clone());
        }
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
        Point coordinate = searchThread.getCurrentLocation();
        Point coordinate2 = searchThread.getSecondLocation();
        // For development purposes we draw a circle around the tracked object
        Imgproc.circle( frame1, coordinate, 20, WHITE );
        Imgproc.circle( frame1, coordinate2, 20, WHITE );

        // make the image not mirrored
        Core.flip(frame1, frame1, 1);
        return frame1;
    }

    public void updateIcon(int i){
        if ( boxes[i].loop.isRunning() ){
            box2View.setColorFilter(loopColor, PorterDuff.Mode.MULTIPLY);
            //box2View.setImageResource(R.drawable.reload_2);
        } else {
            box2View.clearColorFilter();
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