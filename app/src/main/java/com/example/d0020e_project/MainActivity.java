package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;


public class MainActivity extends AppCompatActivity {

    public int[] getSounds(String profile) {
        switch (profile){
            case "Drums":
                return  new int[] {R.raw.drumsnare1, R.raw.drumshorthat, R.raw.drumsnare2, R.raw.drumsnare3, R.raw.drumhihat, R.raw.drumkick, R.raw.drumlonghat};
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

        Spinner soundProfileDropdown = findViewById( R.id.spinner1 );
        String[] soundprofiles = new String[] {"Drums", "Piano", "Guitar", "Trumpet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>( this, android.R.layout.simple_spinner_dropdown_item, soundprofiles );
        soundProfileDropdown.setAdapter( adapter );

        Button camBtn = findViewById(R.id.cameraBtn);
        camBtn.setOnClickListener( v -> {

            startActivity( new Intent(MainActivity.this, CameraActivity.class).putExtra("SoundProfile",
                    getSounds( (String) soundProfileDropdown.getSelectedItem())));
            finish();
        });
    }
}