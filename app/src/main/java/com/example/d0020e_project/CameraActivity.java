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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class CameraActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
        private JavaCameraView javaCameraView;
        private Mat mRGBA, mRGBAT;

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
            javaCameraView = (JavaCameraView) findViewById(R.id.my_camera_view);
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
        }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRGBA = inputFrame.rgba();
        mRGBAT = mRGBA.t();
        Core.flip(mRGBA.t(), mRGBAT, 1);
        Imgproc.resize(mRGBAT, mRGBAT, mRGBA.size());
        return mRGBAT;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture){

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (javaCameraView != null){
            javaCameraView.disableView();
        }
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