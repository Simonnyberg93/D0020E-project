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

    public int[][] getSounds(String s) {
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
            case "Piano":
                profile[0] = new int[]{ R.raw.pianoa3, R.drawable.orgpiano };
                profile[1] = new int[]{ R.raw.pianob3, R.drawable.orgpiano };
                profile[2] = new int[]{ R.raw.pianoc3, R.drawable.orgpiano };
                profile[3] = new int[]{ R.raw.pianod3, R.drawable.orgpiano };
                profile[4] = new int[]{ R.raw.pianoe3, R.drawable.orgpiano };
                profile[5] = new int[]{ R.raw.pianof3, R.drawable.orgpiano };
                profile[6] = new int[]{ R.raw.pianog4, R.drawable.orgpiano };
                break;
            default:
                break;
        }
        return profile;
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
                    getSounds( (String) soundProfileDropdown.getSelectedItem())).setAction( (String) soundProfileDropdown.getSelectedItem() ));
            finish();
        });
    }
}
