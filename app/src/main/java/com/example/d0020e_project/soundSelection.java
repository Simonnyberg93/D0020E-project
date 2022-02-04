package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class soundSelection extends AppCompatActivity {

    public int[] getSounds(String profile) {
        switch (profile){
            case "Drums":
                return  new int[] {R.raw.drumsnare1, R.raw.drumshorthat, R.raw.drumsnare2, R.raw.drumsnare3, R.raw.drumsnare4, R.raw.drumsnarelong, R.raw.drumhihat, R.raw.drumkick, R.raw.drumlonghat};
            case "Bass":
                return  new int[] {R.raw.bass01, R.raw.bass02, R.raw.bass03, R.raw.bass04, R.raw.bass05, R.raw.bass06, R.raw.bass07, R.raw.bass08, R.raw.bass09};
            case "Trumpet":
                return  new int[] {R.raw.trumpeta3, R.raw.trumpeta4, R.raw.trumpetc4, R.raw.trumpetd4, R.raw.trumpete4, R.raw.trumpetf4, R.raw.trumpetg3, R.raw.trumpetg4, R.raw.trumpetg5};
            case "Piano":
                return  new int[] {R.raw.pianoa3, R.raw.pianob3, R.raw.pianoc3, R.raw.pianoc6, R.raw.pianod3, R.raw.pianoe3, R.raw.pianof3, R.raw.pianog4, R.raw.pianog5};
            default:
                return new int[] {0};
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_sound_selection);

        // make sure screen does not go dark
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Spinner soundProfileDropdown = findViewById( R.id.spinnerSP );

        String[] soundprofiles = new String[] {"Drums", "Piano", "Bass", "Trumpet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>( this, R.layout.spinner_selected_item, soundprofiles );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        soundProfileDropdown.setAdapter( adapter );

        Button camBtn = findViewById(R.id.continueBtn);
        camBtn.setOnClickListener( v -> {

            startActivity( new Intent(soundSelection.this, CameraActivity.class).putExtra("SoundProfile",
                    getSounds( (String) soundProfileDropdown.getSelectedItem())));
            finish();
        });
    }
}
