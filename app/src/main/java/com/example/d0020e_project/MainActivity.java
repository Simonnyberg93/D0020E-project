package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button camBtn = findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CameraActivity.class)));
    }
}