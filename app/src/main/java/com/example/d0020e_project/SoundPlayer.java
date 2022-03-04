package com.example.d0020e_project;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundPlayer {

    public SoundPool soundPool;
    private CameraActivity camact;
    int[] sounds;
    int sound0, sound1,sound2,sound3,sound4,sound5,sound6;

    public SoundPlayer( CameraActivity c, int[] soundProfile ) {
        this.camact = c;
        this.sounds = soundProfile;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage( AudioAttributes.USAGE_ASSISTANCE_SONIFICATION )
                    .setContentType( AudioAttributes.CONTENT_TYPE_SONIFICATION )
                    .build();
            soundPool = new SoundPool.Builder()
                    .setMaxStreams( 9 )
                    .setAudioAttributes( audioAttributes )
                    .build();
        } else {
            soundPool = new SoundPool( 9, AudioManager.STREAM_MUSIC, 0 );
        }
        sound0 = soundPool.load( camact,  sounds[0], 1);
        sound1 = soundPool.load( camact,  sounds[1], 1);
        sound2 = soundPool.load( camact,  sounds[2], 1);
        sound3 = soundPool.load( camact,  sounds[3], 1);
        sound4 = soundPool.load( camact,  sounds[4], 1);
        sound5 = soundPool.load( camact,  sounds[5], 1);
        sound6 = soundPool.load( camact,  sounds[6], 1);
    }

    public void onExit(){
        if(soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }


    public void playSound( int i ) {
        if (soundPool != null) {
            switch (i) {
                case 0:
                    soundPool.play( sound0, 1, 1, 0, 0, 1 );
                    break;
                case 1:
                    soundPool.play( sound1, 1, 1, 0, 0, 1 );
                    break;
                case 2:
                    soundPool.play( sound2, 1, 1, 0, 0, 1 );
                    break;
                case 3:
                    soundPool.play( sound3, 1, 1, 0, 0, 1 );
                    break;
                case 4:
                    soundPool.play( sound4, 1, 1, 0, 0, 1 );
                    break;
                case 5:
                    soundPool.play( sound5, 1, 1, 0, 0, 1 );
                    break;
                case 6:
                    soundPool.play( sound6, 1, 1, 0, 0, 1 );
                    break;
                default:
                    break;
            }
        }
    }
}
