package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make sure screen does not go dark
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button camBtn = findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener( v -> {
            startActivity( new Intent(MainActivity.this, CameraActivity.class) );
            finish();
        } );

    }
}