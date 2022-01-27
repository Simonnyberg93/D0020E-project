package com.example.d0020e_project;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

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

        Button buttonGuitar = (Button) findViewById(R.id.buttonGuitar);
        Button buttonDrums = (Button) findViewById(R.id.buttonDrums);
        Button buttonPiano = (Button) findViewById(R.id.buttonPiano);
        Button buttonTrumpet = (Button) findViewById(R.id.buttonTrumpet);

        buttonGuitar.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    Intent withGuitar = new Intent(soundSelection.this, CameraActivity.class);
                    withGuitar.putExtra("SoundProfile", getSounds("Guitar" ));
                    soundSelection.this.startActivity(withGuitar);
                }
            });

        buttonDrums.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent withDrums = new Intent(soundSelection.this, CameraActivity.class);
                withDrums.putExtra("SoundProfile", getSounds("Drums" ));
                soundSelection.this.startActivity(withDrums);
            }
        });

        buttonPiano.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent withPiano = new Intent(soundSelection.this, CameraActivity.class);
                withPiano.putExtra("SoundProfile", getSounds("Guitar" ));
                soundSelection.this.startActivity(withPiano);
            }
        });

        buttonTrumpet.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                Intent withTrumpet = new Intent(soundSelection.this, CameraActivity.class);
                withTrumpet.putExtra("SoundProfile", getSounds("Guitar" ));
                soundSelection.this.startActivity(withTrumpet);
            }
        });

    }
}