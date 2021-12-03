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
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.line;
import static org.opencv.imgproc.Imgproc.putText;

import java.util.ArrayList;
import java.util.List;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private JavaCameraView javaCameraView;

    private Rect objectBoundingRectangle = new Rect(0,0,0,0);
    private int theObject[] = {0,0};

    private boolean objectDetected = false;
    private boolean debugMode = false;
    private boolean trackingEnable = true;

    Mat frame1, frame2, grayImage1, grayImage2, differenceImage, thresholdImage;
    CameraBridgeViewBase.CvCameraViewFrame prevFrame;

    int SENSITIVITY_VALUE = 20;
    int BLUR_SIZE = 10;

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

        //if contours vector is not empty, we have found some objects
        if (contours.size() > 0) {
            objectDetected = true;
        }else {
            objectDetected = false;
        }

        if (objectDetected) {
            //the largest contour is found at the end of the contours vector
            //we will simply assume that the biggest contour is the object we are looking for.
            List<Mat> largestContourVec = new ArrayList<>();
            largestContourVec.add( contours.get( contours.size() - 1 ) );

            //make a bounding rectangle around the largest contour then find its centroid
            //this will be the object's final estimated position.
            objectBoundingRectangle = boundingRect(largestContourVec.get( 0 ));
            int xpos = objectBoundingRectangle.x+objectBoundingRectangle.width/2;
            int ypos = objectBoundingRectangle.y+objectBoundingRectangle.height/2;

            //update the objects positions by changing the 'theObject' array values
            theObject[0] = xpos;
            theObject[1] = ypos;
        }
        //make some temp x and y variables so we dont have to type out so much
        int x = theObject[0];
        int y = theObject[1];

        // Here we will instead of drawing on screen send the coordinates to a function
        // that plays sound based on the location of movement.
        //draw some crosshairs around the object
        circle(camerafeed, new Point(x,y),20, new Scalar(0,255,0),2);
        line(camerafeed, new Point(x,y), new Point(x,y-25), new Scalar(0,255,0),2);
        line(camerafeed, new Point(x,y), new Point(x,y+25), new Scalar(0,255,0),2);
        line(camerafeed, new Point(x,y), new Point(x-25,y), new Scalar(0,255,0),2);
        line(camerafeed, new Point(x,y), new Point(x+25,y), new Scalar(0,255,0),2);

        //write the position of the object to the screen
        String s = "Tracking object at ("+ x + "," + y + ")";
        putText(camerafeed, s, new Point(x, y), 1, 1, new Scalar( 255, 0, 0 ), 2);
        return camerafeed;
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

        if (debugMode == true) {
            frame1.release();
            frame2.release();
            differenceImage.release();
            return thresholdImage;
        }
        // if enabled search for contours on our thresholded image
        if (trackingEnable == true) {
            differenceImage.release();
            return searchForMovement( thresholdImage, frame1 );
        }
        frame2.release();
        differenceImage.release();
        thresholdImage.release();
        return frame1;
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