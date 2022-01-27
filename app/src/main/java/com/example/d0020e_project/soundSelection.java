package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Camera;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class soundSelection extends AppCompatActivity {

    public int[] getSounds(String profile) {
        switch (profile){
            case "Drums":
                return  new int[] {R.raw.drumsnare1, R.raw.drumshorthat, R.raw.drumsnare2, R.raw.drumsnare3, R.raw.drumsnare4, R.raw.drumsnarelong, R.raw.drumhihat, R.raw.drumkick, R.raw.drumlonghat};
            default:
                // TODO insert different sound profile
                return new int[] {0};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // make sure screen does not go dark
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Button buttonGuitar=(Button)findViewById(R.id.buttonGuitar);
        Button buttonDrums=(Button)findViewById(R.id.buttonDrums);
        Button buttonPiano=(Button)findViewById(R.id.buttonPiano);
        Button buttonTrumpet=(Button)findViewById(R.id.buttonTrumpet);

        buttonGuitar.setOnClickListener( v -> {
        startActivity( new Intent(soundSelection.this, CameraActivity.class).putExtra("SoundProfile",
                getSounds("Guitar" )));
        finish();
        });

        buttonDrums.setOnClickListener( v -> {
            startActivity( new Intent(soundSelection.this, CameraActivity.class).putExtra("SoundProfile",
                    getSounds("Drums" )));
            finish();
        });

        buttonPiano.setOnClickListener( v -> {
            startActivity( new Intent(soundSelection.this, CameraActivity.class).putExtra("SoundProfile",
                    getSounds("Piano" )));
            finish();
        });

        buttonTrumpet.setOnClickListener( v -> {
            startActivity( new Intent(soundSelection.this, CameraActivity.class).putExtra("SoundProfile",
                    getSounds("Trumpet" )));
            finish();
        });

    }
}