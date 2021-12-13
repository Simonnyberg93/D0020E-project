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
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import static org.opencv.imgproc.Imgproc.boundingRect;

import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;

    private Rect objectBoundingRectangle = new Rect(0,0,0,0);

    private int BOXWIDTH = 0;
    private int BOXHEIGHT = 0;
    private int frameWidth = 0;
    private int frameHeight = 0;

    private int theFirstObject[] = {0,0};

    private boolean debugMode = false;
    private boolean trackingEnable = true;

    private Mat frame1, frame2, grayImage1, grayImage2, differenceImage, thresholdImage;
    private CameraBridgeViewBase.CvCameraViewFrame prevFrame;

    private final int SENSITIVITY_VALUE = 50;
    private final int BLUR_SIZE = 30;
    private final Scalar WHITE = new Scalar( 255,255,255,0 );

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

            // make sure screen does not go dark
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            Button btnBack = findViewById( R.id.btnBack );
            btnBack.setOnClickListener( v -> {
                startActivity( new Intent(CameraActivity.this, MainActivity.class));
                finish();
            } );

            Button btnDebug = findViewById( R.id.btnDebug );
            btnDebug.setOnClickListener( v -> {
                if (this.debugMode){this.debugMode = false;} else {this.debugMode = true;}
            } );
        }

    @Override
    public void onCameraViewStarted(int width, int height) {
        frameHeight = height;
        frameWidth  = width;
        BOXHEIGHT = frameHeight / 4;
        BOXWIDTH = ( int ) Math.ceil( frameWidth / 6 );
        frame1 = new Mat();
        frame2 = new Mat();
        grayImage1 = new Mat();
        grayImage2 = new Mat();
        differenceImage = new Mat();
        thresholdImage = new Mat();
    }

    public Mat searchForMovement(Mat thresholdImage, Mat camerafeed){
        Mat temp = new Mat();
        thresholdImage.copyTo( temp );
        List<MatOfPoint> contours = new ArrayList<>();
        Mat heirarchy = new Mat();
        //Imgproc.findContours( temp, contours, heirarchy, Imgproc.RETR_CCOMP   , Imgproc.CHAIN_APPROX_SIMPLE ); // retrieves all contours
        Imgproc.findContours( temp, contours, heirarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE ); // all external contours

        if (contours.size() > 0) {
            //the largest contour is found at the end of the contours vector
            //we will simply assume that the biggest contour is the object we are looking for.
            List<Mat> largestContourVec = new ArrayList<>();
            largestContourVec.add( contours.get( contours.size() - 1 ) );
            //largestContourVec.add( contours.get( contours.size() - 2 ) );

            //make a bounding rectangle around the largest contour then find its centroid
            //this will be the object's final estimated position.
            objectBoundingRectangle = boundingRect(largestContourVec.get( 0 ));
            //objectBoundingRectangle2 = boundingRect( largestContourVec.get( 1 ) );

            int xpos = objectBoundingRectangle.x+objectBoundingRectangle.width/2;
            int ypos = objectBoundingRectangle.y+objectBoundingRectangle.height/2;

            //int x1 = objectBoundingRectangle2.x+objectBoundingRectangle2.width/2;
            //int y1 = objectBoundingRectangle2.y+objectBoundingRectangle2.height/2;

            //update the objects positions by changing the 'theObject' array values
            theFirstObject[0] = xpos;
            theFirstObject[1] = ypos;

            //theSecondObject[0] = x1;
            //theSecondObject[1] = y1;
        }
        //make some temp x and y variables so we dont have to type out so much
        int x = theFirstObject[0];
        int y = theFirstObject[1];

        //int x1 = theSecondObject[0];
        //int y1 = theSecondObject[1];
        if (x != -1 && y != -1){
            if(y < BOXWIDTH){
                // left
                if (x > BOXWIDTH && x < BOXWIDTH * 2){
                    System.out.println("Bottom left corner");
                } else if (x > BOXWIDTH*2 && x < BOXWIDTH*3){
                    System.out.println("Second left from bottom.");
                }else if (x > BOXWIDTH*3 && x < BOXWIDTH*4){
                    System.out.println("Thrird left from bottom.");
                }else if (x > BOXWIDTH*4 && x < BOXWIDTH*5){
                    System.out.println("Top Left.");
                }

            } else if (y > (frameHeight - BOXWIDTH)){
                // right
                if (x > BOXWIDTH && x < BOXWIDTH * 2){
                    System.out.println("Bottom right corner");
                } else if (x > BOXWIDTH*2 && x < BOXWIDTH*3){
                    System.out.println("Second right from bottom.");
                }else if (x > BOXWIDTH*3 && x < BOXWIDTH*4){
                    System.out.println("Thrird right from bottom.");
                }else if (x > BOXWIDTH*4 && x < BOXWIDTH*5){
                    System.out.println("Top right.");
                }
            } else if(y > (frameHeight / 2) - (BOXWIDTH / 2) && y < (frameHeight / 2) + (BOXWIDTH / 2) && x > (frameHeight - BOXHEIGHT)) {
                // top box
                System.out.println("Top box.");
            }
        }

        theFirstObject[0] = -1;
        theFirstObject[1] = -1;
        return camerafeed;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // read first frame
        frame1 = inputFrame.rgba();
        // convert frame1 to grayscale
        Imgproc.cvtColor( frame1, grayImage1, Imgproc.COLOR_BGR2GRAY );
        if (prevFrame == null){
            prevFrame = inputFrame;
            return frame1;
        }
        // copy second frame
        frame2 = prevFrame.rgba();
        Imgproc.cvtColor( frame2, grayImage2 , Imgproc.COLOR_BGR2GRAY);

        // preform frame differencing with the sequential images. This will output an
        // "intensity image" do not confuse this with a threshold image, we will need
        // to perform thresholding afterwars.
        Core.absdiff( grayImage1, grayImage2, differenceImage );
        // threshold intensity image at a given sensitivity value
        Imgproc.threshold( differenceImage, thresholdImage, SENSITIVITY_VALUE, 255, Imgproc.THRESH_BINARY );
        // blur image to get rid of the noise, this will output an "Intensity image".
        Imgproc.blur(thresholdImage, thresholdImage, new Size(BLUR_SIZE, BLUR_SIZE) );
        Imgproc.threshold( thresholdImage, thresholdImage, SENSITIVITY_VALUE, 255, Imgproc.THRESH_BINARY );

        // draw our sensor locations
        // left
        Imgproc.rectangle( frame1, new Rect(frameWidth - (BOXWIDTH * 5),0,BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect(frameWidth - (BOXWIDTH * 4),0,BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect(frameWidth - (BOXWIDTH * 3),0,BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect(frameWidth - (BOXWIDTH * 2),0,BOXWIDTH,BOXHEIGHT), WHITE );
        // right
        Imgproc.rectangle( frame1, new Rect( frameWidth - (BOXWIDTH * 5), frameHeight - BOXWIDTH, BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect( frameWidth - (BOXWIDTH * 4), frameHeight - BOXWIDTH, BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect( frameWidth - (BOXWIDTH * 3), frameHeight - BOXWIDTH, BOXWIDTH,BOXHEIGHT), WHITE );
        Imgproc.rectangle( frame1, new Rect( frameWidth - (BOXWIDTH * 2), frameHeight - BOXWIDTH, BOXWIDTH,BOXHEIGHT), WHITE );
        // top
        Imgproc.rectangle( frame1, new Rect(frameWidth - BOXHEIGHT, (frameHeight/2) - (BOXWIDTH / 2), BOXHEIGHT, BOXWIDTH), WHITE );


        if (debugMode == true) {
            frame1.release();
            frame2.release();
            differenceImage.release();
            return thresholdImage;
        }
        // if enabled search for contours on our thresholded image
        if (trackingEnable == true) {
            return searchForMovement( thresholdImage, frame1 );
        }
        frame2.release();
        differenceImage.release();
        thresholdImage.release();
        return frame1;
    }

    public void releaseObjects() {
        frame1.release();
        frame2.release();
        grayImage1.release();
        grayImage2.release();
        differenceImage.release();
        thresholdImage.release();

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