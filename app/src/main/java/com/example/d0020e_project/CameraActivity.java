package com.example.d0020e_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

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
    private Scalar lowerCR;     //lower color-range
    private Scalar upperCR;     //upper color-range
    private CameraActivity camAct = this;
    private Search searchThread;
    private String instrumentName = "";

    private ImageView[] boxViews = new ImageView[boxes.length];

    ImageView loopIcon;
    private int loopColor = Color.parseColor("#99ffbb");
    private final int BOX_PADDING = 20;

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

        instrumentName =  getIntent().getStringExtra( "SoundProfile" );
        switch (instrumentName){
            case "Mixed":
            case "Bass":
            case "Piano":
                setContentView( R.layout.camera_activity_secondlayout );
                break;
            default:
                setContentView(R.layout.camera_activity);
                break;
        }
        javaCameraView = findViewById(R.id.javaCameraView);
        javaCameraView.setCameraPermissionGranted();
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(CameraActivity.this);
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        upperCR = getUpperCR(getIntent().getStringExtra( "colorKey" ));
        lowerCR = getLowerCR(getIntent().getStringExtra( "colorKey" ));
        System.out.println("Upper: " + upperCR);
        System.out.println("Lower: " + lowerCR);

        int[][] soundProfile = (int[][]) getIntent().getSerializableExtra( "profile" );
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
            startActivity( new Intent(CameraActivity.this, SoundSelection.class));
            finish();
        } );

    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        int BOXHEIGHT = (height / 4);
        int BOXWIDTH = (int) (Math.ceil(width / 6));
        switch (instrumentName){
            case "Trumpet":
            case "Drums":
                // left
                boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer, boxViews,0));
                boxes[1] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer, boxViews,1));
                boxes[2] = new Box(new Rect(width - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer, boxViews,2));
                // right
                boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer, boxViews,3));
                boxes[4] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer, boxViews,4));
                boxes[5] = new Box(new Rect(width - (BOXWIDTH * 2), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer, boxViews,5));
                // top
                boxes[6] = new Box(new Rect(width - BOXHEIGHT, (height /2) - (BOXWIDTH / 2), BOXWIDTH, BOXHEIGHT), new LoopRunnable(6, soundPlayer, boxViews,6));
                loopBox = new LoopBox(new Rect(0,  height /2 - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH), loopIcon);
                loopBox.start();
                break;
            case "Mixed":
            case "Bass":
            case "Piano":
                // Down
                boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer, boxViews,0));
                boxes[1] = new Box(new Rect(width - (BOXWIDTH * 5) , BOXWIDTH + BOX_PADDING, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer, boxViews,1));
                boxes[2] = new Box(new Rect(width - (BOXWIDTH * 5), BOXWIDTH * 2 + BOX_PADDING*2, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer, boxViews,2));
                boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), BOXWIDTH * 3 + BOX_PADDING*3, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer, boxViews,3));
                // Upp
                boxes[4] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH + BOX_PADDING, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer, boxViews,4));
                boxes[5] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH * 2 + BOX_PADDING*2, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer, boxViews,5));
                boxes[6] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH * 3 + BOX_PADDING*3, BOXWIDTH, BOXHEIGHT), new LoopRunnable(6, soundPlayer, boxViews,6));
                loopBox = new LoopBox(new Rect((width - (BOXWIDTH * 5/2)),  0, BOXWIDTH, BOXHEIGHT), loopIcon);
                loopBox.start();
                break;
            default:
                System.out.println("Not yet implemented!");
                break;
        }
        SeekBar seekBar = findViewById( R.id.seekBar );
        seekBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if( fromUser ){
                    int time;
                    switch (progress){
                        case 0:
                            time = 100;
                            break;
                        case 1:
                            time = 300;
                            break;
                        case 2:
                            time = 500;
                            break;
                        case 3:
                            time = 700;
                            break;
                        case 4:
                            time = 900;
                            break;
                        default:
                            time = 1000;
                            break;
                    }
                    for( int i = 0; i < boxes.length; i++ ){
                        boxes[i].loop.setSleepTime(time);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        } );

        searchThread = new Search(boxes, loopBox, BOXWIDTH, height, this, lowerCR, upperCR);
    }

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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // use when testing on (some) emulator's.
       // Imgproc.cvtColor( frame1, frame1, Imgproc.COLOR_BGR2RGB );
        /* Add the current frame to queue in search for object thread */
        if (frame1 != null) {
            searchThread.addFrame(frame1.clone());
        }
        // draw our sensor locations, this will be removed, we do not want to draw on every frame.
//        for (Box box : boxes){
//            if ( box.loop.isRunning() ){
//                Imgproc.rectangle(frame1, box.rectangle, BLUE);
//            } else {
//                Imgproc.rectangle(frame1, box.rectangle, WHITE);
//            }
//        }
//        if (loopBox.isPressed()) {
//            Imgproc.rectangle(frame1, loopBox.rectangle, GREEN);
//        } else {
//            Imgproc.rectangle(frame1, loopBox.rectangle, RED);
//        }
        //Point coordinate = searchThread.getCurrentLocation();
        //Point coordinate2 = searchThread.getSecondLocation();
        // For development purposes we draw a circle around the tracked object
        //Imgproc.circle( frame1, coordinate, 20, WHITE );
        //Imgproc.circle( frame1, coordinate2, 20, WHITE );
        // make the image not mirrored
        Core.flip(frame1, frame1, 1);
        return frame1;
    }


    @Override
    public void onCameraViewStopped() {
        for(int i = 0; i < boxes.length; i++){
            boxes[i].loop.stopLoop();
            boxes[i].loop.stopThread();
        }
        searchThread.stopLoop();
        finish();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture){ }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null){
            javaCameraView.disableView();
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
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