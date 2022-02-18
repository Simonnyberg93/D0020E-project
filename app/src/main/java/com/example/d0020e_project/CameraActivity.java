package com.example.d0020e_project;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    private Box[] boxes = new Box[7];
    private SoundPlayer soundPlayer;
    private Mat frame1;
    private final Scalar WHITE      = new Scalar( 255,255,255,0 );
    private final Scalar BLUE = new Scalar( 0,0,255 );
    private final Scalar GREEN = new Scalar( 0,255,0 );
    private final Scalar RED = new Scalar( 255,0,0 );
    private CameraActivity camAct = this;
    private Search searchThread;
    private String instrumentName = "";

    private ImageView[] boxViews = new ImageView[boxes.length];

    ImageView loopIcon;
    private int loopColor = Color.parseColor("#99ffbb");
    private int playOnceColor = Color.parseColor( "#ff3399" );
    private final int BOX_PADDING = 15;

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

        instrumentName = getIntent().getAction();
        switch (instrumentName){
            case "Drums":
                break;
            case "Piano":
                setContentView( R.layout.camera_activity_piano );
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

    @Override
    public void onCameraViewStarted(int width, int height) {
        int BOXHEIGHT = height / 4;
        int BOXWIDTH = (int) Math.ceil(width / 6);
        switch (instrumentName){
            case "Drums":
                // left
                boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer));
                boxes[1] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer));
                boxes[2] = new Box(new Rect(width - (BOXWIDTH * 2), 0, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer));
                // right
                boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer));
                boxes[4] = new Box(new Rect((width - (BOXWIDTH * 3))- BOXWIDTH/2, height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer));
                boxes[5] = new Box(new Rect(width - (BOXWIDTH * 2), height - BOXWIDTH, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer));
                // top
                boxes[6] = new Box(new Rect(width - BOXHEIGHT, (height /2) - (BOXWIDTH / 2), BOXWIDTH, BOXHEIGHT), new LoopRunnable(6, soundPlayer));

                loopBox = new LoopBox(new Rect(0,  height /2 - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH));
                break;
            default:
                // Upp
                loopBox = new LoopBox(new Rect((width - (BOXWIDTH * 5/2)),  0, BOXWIDTH, BOXHEIGHT));
                boxes[4] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH + BOX_PADDING, BOXWIDTH, BOXHEIGHT), new LoopRunnable(4, soundPlayer));
                boxes[5] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH * 2 + BOX_PADDING*2, BOXWIDTH, BOXHEIGHT), new LoopRunnable(5, soundPlayer));
                boxes[6] = new Box(new Rect((width - (BOXWIDTH * 5/2)), BOXWIDTH * 3 + BOX_PADDING*3, BOXWIDTH, BOXHEIGHT), new LoopRunnable(6, soundPlayer));
                // Down
                boxes[0] = new Box(new Rect(width - (BOXWIDTH * 5), 0, BOXWIDTH, BOXHEIGHT), new LoopRunnable(0, soundPlayer));
                boxes[1] = new Box(new Rect(width - (BOXWIDTH * 5) , BOXWIDTH + BOX_PADDING, BOXWIDTH, BOXHEIGHT), new LoopRunnable(1, soundPlayer));
                boxes[2] = new Box(new Rect(width - (BOXWIDTH * 5), BOXWIDTH * 2 + BOX_PADDING*2, BOXWIDTH, BOXHEIGHT),  new LoopRunnable(2, soundPlayer));
                boxes[3] = new Box(new Rect(width - (BOXWIDTH * 5), BOXWIDTH * 3 + BOX_PADDING*3, BOXWIDTH, BOXHEIGHT), new LoopRunnable(3, soundPlayer));



                break;
        }

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
     // Core.flip(frame1, frame1, 1);
        return frame1;
    }


    public void iconHitIndicate(int i){
        switch (instrumentName){
            case "Drums":
                if ( boxes[i].loop.isPlaying() ){
                    boxViews[i].setColorFilter(playOnceColor, PorterDuff.Mode.MULTIPLY);
                } else {
                    boxViews[i].clearColorFilter();
                }
                break;
            case "Piano":
                Drawable drawable = boxViews[i].getDrawable();
                Drawable drawable1 = camAct.getBaseContext().getDrawable(R.drawable.orgpiano);
                if(drawable.equals(drawable1)){
                    System.out.println("SUCCESS!!");
                }
                int d = boxViews[i].getId();
                int d1 = 2131230943;
                switch (i){
                    case 0:

                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianoa );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }

                        break;
                    case 1:

                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianob );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }

                        break;
                    case 2:
                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianoc );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }
                        break;
                    case 3:
                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianod );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }
                        break;
                    case 4:
                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianoe );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }
                        break;
                    case 5:
                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianof );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }
                        break;
                    case 6:
                        if (d == d1) {
                            boxViews[i].setImageResource( R.drawable.pianog );
                        } else {
                            boxViews[i].setImageResource( R.drawable.orgpiano );
                        }
                        break;
                    default:
                        break;
                }
               // } else {
                   // boxViews[i].setImageResource( R.drawable.orgpiano );
               // }
                break;
            default:
                throw new IllegalStateException( "Unexpected value: " + instrumentName );
        }
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