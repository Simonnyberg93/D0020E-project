package com.example.d0020e_project;

import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

public class SoundPlayer {

    public SoundPool soundPool;
    int[] sounds;
    int sound0, sound1,sound2,sound3,sound4,sound5,sound6,sound7,sound8;

    public SoundPlayer( CameraActivity cameraActivity, int[] soundProfile ) {
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
        sound0 = soundPool.load( cameraActivity,  sounds[0], 1);
        sound1 = soundPool.load( cameraActivity,  sounds[1], 1);
        sound2 = soundPool.load( cameraActivity,  sounds[2], 1);
        sound3 = soundPool.load( cameraActivity,  sounds[3], 1);
        sound4 = soundPool.load( cameraActivity,  sounds[4], 1);
        sound5 = soundPool.load( cameraActivity,  sounds[5], 1);
        sound6 = soundPool.load( cameraActivity,  sounds[6], 1);
        sound7 = soundPool.load( cameraActivity,  sounds[7], 1);
        sound8 = soundPool.load( cameraActivity,  sounds[8], 1);
    }

    public void onExit(){
        soundPool.release();
        soundPool = null;
    }

    public int getSound(int i){
        return sounds[i];
    }

    public void playSound( CameraActivity cameraActivity, int i ) {
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
            case 7:
                soundPool.play( sound7, 1, 1, 0, 0, 1 );
                break;
            case 8:
                soundPool.play( sound8, 1, 1, 0, 0, 1 );
                break;
            default:
                break;
        }

    }
}
