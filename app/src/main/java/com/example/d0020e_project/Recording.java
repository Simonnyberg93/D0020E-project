package com.example.d0020e_project;

import android.media.MediaRecorder;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;

public class Recording extends AppCompatActivity {
    private File filename;
    private MediaRecorder recorder = null;

    public void newRecording(String n){
        File path = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MUSIC);
        filename = new File(path, "/rec5.3gp");
        recorder = new MediaRecorder();
        recorder.setOutputFile(filename);
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    }
    public void startRecording(){
        try {
            recorder.prepare();
            recorder.start();
        }

        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void stopRecording(){
        recorder.stop();
        recorder.reset();
        recorder.release();
        recorder = null;
    }
}
