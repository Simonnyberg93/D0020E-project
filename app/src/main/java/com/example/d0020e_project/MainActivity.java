package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    private static String TAG = "MainActivity";
    static {
        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Opencv installed successfully");
        } else {
            Log.d(TAG,"Opencv did not install successfully.");
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}