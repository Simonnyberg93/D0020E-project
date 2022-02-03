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


    public HashMap<Integer, Integer> getSoundProfile(String s) {
        HashMap<Integer, Integer> profile = new HashMap<>();
        switch (s) {
            case "Drums":
                profile.put( R.raw.drumhihat, R.drawable.drumhihat );
                profile.put( R.raw.drumkick, R.drawable.drumkick );
                profile.put( R.raw.drumkick2, R.drawable.drumkick );
                profile.put( R.raw.drumshorthat, R.drawable.drumhihat );
                profile.put( R.raw.drumsnare1, R.drawable.drumsnare );
                profile.put( R.raw.drumsnare3, R.drawable.drumsnare );
                profile.put( R.raw.drumsnare2, R.drawable.drumsnare );
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