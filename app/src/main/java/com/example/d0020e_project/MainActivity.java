package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.HashMap;


public class MainActivity extends AppCompatActivity {


    public int[][] getSoundProfile(String s) {
        int[][] profile = new int[7][2];
        switch (s) {
            case "Drums":
                profile[0] = new int[]{ R.raw.drumhihat, R.drawable.drumhihat };
                profile[1] = new int[]{ R.raw.drumkick, R.drawable.drumkick };
                profile[2] = new int[]{ R.raw.drumkick2, R.drawable.drumkick };
                profile[3] = new int[]{ R.raw.drumshorthat, R.drawable.drumhihat };
                profile[4] = new int[]{ R.raw.drumsnare1, R.drawable.drumsnare };
                profile[5] = new int[]{ R.raw.drumsnare3, R.drawable.drumsnare };
                profile[6] = new int[]{ R.raw.drumsnare2, R.drawable.drumsnare };
                break;
            default:
                break;
        }
        return profile;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make sure screen does not go dark
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Spinner soundProfileDropdown = findViewById( R.id.spinner1 );
        String[] soundprofiles = new String[] {"Drums", "Piano", "Guitar", "Trumpet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>( this, android.R.layout.simple_spinner_dropdown_item, soundprofiles );
        soundProfileDropdown.setAdapter( adapter );

        Button camBtn = findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener( v -> {

            startActivity( new Intent(MainActivity.this, CameraActivity.class)
                    .putExtra("SoundProfile",
                    getSoundProfile( (String) soundProfileDropdown.getSelectedItem() )));
            finish();
        });
    }
}