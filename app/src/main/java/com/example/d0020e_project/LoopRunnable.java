package com.example.d0020e_project;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.widget.ImageView;

import java.util.concurrent.CountDownLatch;

public class LoopRunnable extends Thread implements Runnable {

    private CountDownLatch conditionLatch = new CountDownLatch( 1 );
    private int musicTrack;
    private final ImageView[] view;
    private final int idx;
    private SoundPlayer soundPlayer;
    private boolean run = false;
    private boolean isPlaying = false;
    private int sleepTime = 500;
    private boolean runThread = true;

    public LoopRunnable( int musicTrack, SoundPlayer s, ImageView[] v, int idx){
        this.musicTrack = musicTrack;
        this.soundPlayer = s;
        this.view = v;
        this.idx = idx;
    }

    synchronized void startLoop() { run = true; }

    synchronized boolean isRunning(){
        return run;
    }

    synchronized boolean isPlaying() { return isPlaying; }

    synchronized void setIsPlaying() { this.isPlaying = true; }

    synchronized void setIsNotPlaying() { this.isPlaying = false; }

    synchronized void stopLoop() { run = false; }

    synchronized void stopThread() { runThread = false;}

    synchronized void unBlock() { conditionLatch.countDown(); }

    synchronized void block(){ conditionLatch = new CountDownLatch( 1 ); }

    void toggleIcon(){
        synchronized (view) {
            if (isPlaying()) {
                view[idx].setColorFilter( Color.BLUE, PorterDuff.Mode.MULTIPLY );
            } else {
                view[idx].clearColorFilter();
            }
        }
    }

    synchronized public void setSleepTime(int time){
        sleepTime = time;
    }

    @Override
    public void run() {
        while(runThread) {
            try {
                // block thread until conditionLatch = 0
                conditionLatch.await();
                setIsPlaying();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
           toggleIcon();
            while (isRunning()) {
                soundPlayer.playSound( musicTrack );
                try {
                   Thread.currentThread().sleep( sleepTime );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // play sound once if loop variable is not set
            soundPlayer.playSound( musicTrack );
            // Sleep thread so that the sound is not played multiple
            // times in a small timeframe.
            try {
                Thread.currentThread().sleep( sleepTime );
                setIsNotPlaying(); // done playing sound
                toggleIcon();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
