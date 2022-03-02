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
            case "Trumpet":
                profile[0] = new int[]{ R.raw.trumpeta3, R.drawable.trumpeta };
                profile[1] = new int[]{ R.raw.trumpeta4, R.drawable.trumpeta1 };
                profile[2] = new int[]{ R.raw.trumpetc4, R.drawable.trumpetc };
                profile[3] = new int[]{ R.raw.trumpetd4, R.drawable.trumpetd };
                profile[4] = new int[]{ R.raw.trumpete4, R.drawable.trumpete };
                profile[5] = new int[]{ R.raw.trumpetf4, R.drawable.trumpetf };
                profile[6] = new int[]{ R.raw.trumpetg3, R.drawable.trumpetg };
                break;
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
                profile[0] = new int[]{ R.raw.pianoa3, R.drawable.pianoa };
                profile[1] = new int[]{ R.raw.pianob3, R.drawable.pianob };
                profile[2] = new int[]{ R.raw.pianoc3, R.drawable.pianoc };
                profile[3] = new int[]{ R.raw.pianod3, R.drawable.pianod };
                profile[4] = new int[]{ R.raw.pianoe3, R.drawable.pianoe };
                profile[5] = new int[]{ R.raw.pianof3, R.drawable.pianof };
                profile[6] = new int[]{ R.raw.pianog4, R.drawable.pianog };
                break;
            case "Bass":
                profile[0] = new int[]{ R.raw.bass01, R.drawable.bass3 };
                profile[1] = new int[]{ R.raw.bass03, R.drawable.bass2 };
                profile[2] = new int[]{ R.raw.bass05, R.drawable.bass3 };
                profile[3] = new int[]{ R.raw.bass06, R.drawable.bass4 };
                profile[4] = new int[]{ R.raw.bass07, R.drawable.bass1 };
                profile[5] = new int[]{ R.raw.bass08, R.drawable.bass4 };
                profile[6] = new int[]{ R.raw.bass09, R.drawable.bass5 };
                break;
            default:
                break;
        }
        return profile;
    }

//    public int[] getSounds(String profile) {
//        switch (profile){
//            case "Drums":
//                return  new int[] {R.raw.drumsnare1, R.raw.drumshorthat, R.raw.drumsnare2, R.raw.drumsnare3, R.raw.drumsnare4, R.raw.drumsnarelong, R.raw.drumhihat, R.raw.drumkick, R.raw.drumlonghat};
//            case "Bass":
//                return  new int[] {R.raw.bass01, R.raw.bass02, R.raw.bass03, R.raw.bass04, R.raw.bass05, R.raw.bass06, R.raw.bass07, R.raw.bass08, R.raw.bass09};
//            case "Trumpet":
//                return  new int[] {R.raw.trumpeta3, R.raw.trumpeta4, R.raw.trumpetc4, R.raw.trumpetd4, R.raw.trumpete4, R.raw.trumpetf4, R.raw.trumpetg3, R.raw.trumpetg4, R.raw.trumpetg5};
//            case "Piano":
//                return  new int[] {R.raw.pianoa3, R.raw.pianob3, R.raw.pianoc3, R.raw.pianoc6, R.raw.pianod3, R.raw.pianoe3, R.raw.pianof3, R.raw.pianog4, R.raw.pianog5};
//            default:
//                return new int[] {0};
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_sound_selection);

        // make sure screen does not go dark
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Select color and test color
        Spinner testColorSelectionSpinner = findViewById( R.id.spinnerColor);
        String[] testColors = new String [] {"Blue", "Green", "Orange", "Pink", "Yellow", "Red"};

        ArrayAdapter<String> adapterColor = new ArrayAdapter<>(this, R.layout.spinner_selected_item, testColors);
        adapterColor.setDropDownViewResource(R.layout.spinner_dropdown_item);
        testColorSelectionSpinner.setAdapter (adapterColor);

        Button colorTestBtn = findViewById(R.id.colorTestBtn);
        colorTestBtn.setOnClickListener( v -> {
            startActivity( new Intent(soundSelection.this, SelectcolorTestview.class).putExtra("colorKey",
                    ( (String) testColorSelectionSpinner.getSelectedItem())));
            finish();
        });

        //Soundprofile selection
        Spinner soundProfileDropdown = findViewById( R.id.spinnerSP );

        String[] soundprofiles = new String[] {"Drums", "Piano", "Bass", "Trumpet"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>( this, R.layout.spinner_selected_item, soundprofiles );
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        soundProfileDropdown.setAdapter( adapter );

        Button camBtn = findViewById(R.id.continueBtn);

        camBtn.setOnClickListener( v -> {
            Intent camActIntent = new Intent(soundSelection.this, CameraActivity.class);
            camActIntent.putExtra("colorKey", ( (String) testColorSelectionSpinner.getSelectedItem()));
            camActIntent.putExtra("SoundProfile",  (String) soundProfileDropdown.getSelectedItem());
            camActIntent.putExtra( "profile", (int[][]) getSounds( (String) soundProfileDropdown.getSelectedItem() ) );

            startActivity(camActIntent);
        });
    }
}
