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
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;
    private LoopBox loopBox;
    private Box[] boxes = new Box[9];
    private SoundPlayer soundPlayer;
    private Mat frame1;
    private final Scalar WHITE      = new Scalar( 255,255,255,0 );
    private final Scalar BLUE = new Scalar( 0,0,255 );
    private final Scalar GREEN = new Scalar( 0,255,0 );
    private final Scalar RED = new Scalar( 255,0,0 );
    private CameraActivity camAct = this;
    private Search searchThread;

    class LoopRunnable implements Runnable {

        int musicTrack;
        boolean run = false;

        public LoopRunnable(int musicTrack){
           this.musicTrack = musicTrack;
        }

        synchronized int getMusicTrack (){
            return musicTrack;
        }

        void startRun(){
            run = true;
        }

        synchronized boolean isRunning(){
            return run;
        }
        synchronized void stopLoop(){
            run = false;
        }

        @Override
        public void run() {
            while (isRunning()) {
                soundPlayer.playSound(musicTrack);
                try {
                    Thread.currentThread().sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < boxes.length; i++){
                if ( ( Thread.currentThread().getName() == boxes[i].thread.getName() ) && searchThread.isRunning() ){
                    soundPlayer.playSound( i );
                }
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    };

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
            searchThread.stopLoop();
            soundPlayer.onExit();
            startActivity( new Intent(CameraActivity.this, MainActivity.class));
            finish();
        } );

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        int BOXHEIGHT = height / 4;
        int BOXWIDTH = (int) Math.ceil(width / 6);

        boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box1"), new LoopRunnable(0));
        boxes[1] = new Box(new Rect(width - (BOXWIDTH * 4), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box2"), new LoopRunnable(1));
        boxes[2] = new Box(new Rect(width - (BOXWIDTH * 3), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box3"), new LoopRunnable(2));
        boxes[3] = new Box(new Rect(width - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box4"), new LoopRunnable(3));

        boxes[4] = new Box(new Rect(width - (BOXWIDTH * 5), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box5"), new LoopRunnable(4));
        boxes[5] = new Box(new Rect(width - (BOXWIDTH * 4), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box6"), new LoopRunnable(5));
        boxes[6] = new Box(new Rect(width - (BOXWIDTH * 3), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box7"), new LoopRunnable(6));
        boxes[7] = new Box(new Rect(width - (BOXWIDTH * 2), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new Thread(runnable, "box8"), new LoopRunnable(7));

        boxes[8] = new Box(new Rect(width - BOXHEIGHT, (height /2) - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH),new Thread(runnable, "box9"), new LoopRunnable(8));
        loopBox = new LoopBox(new Rect(0,  height /2 - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH));
        searchThread = new Search(boxes, soundPlayer, loopBox, BOXWIDTH, height, runnable);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // Add the current frame to queue in search for object thread
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