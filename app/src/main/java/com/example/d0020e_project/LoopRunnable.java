package com.example.d0020e_project;

import java.util.concurrent.CountDownLatch;

public class LoopRunnable extends Thread implements Runnable {

    private CountDownLatch conditionLatch = new CountDownLatch( 1 );
    private int musicTrack;
    private SoundPlayer soundPlayer;
    private boolean run = false;
    private boolean isPlaying = false;

    public LoopRunnable(int musicTrack, SoundPlayer s){
        this.musicTrack = musicTrack;
        this.soundPlayer = s;
    }

    synchronized void startLoop() { run = true; }

    synchronized boolean isRunning(){
        return run;
    }

    synchronized boolean isPlaying() { return isPlaying; }

    synchronized void setIsPlaying() { this.isPlaying = true; }

    synchronized void setIsNotPlaying() { this.isPlaying = false; }

    synchronized void stopLoop() { run = false; }

    synchronized void unBlock() { conditionLatch.countDown(); }

    synchronized void block(){ conditionLatch = new CountDownLatch( 1 ); }

    @Override
    public void run() {
        while(true) {
            try {
                // block thread until conditionLatch = 0
                conditionLatch.await();
                setIsPlaying();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            while (isRunning()) {
                soundPlayer.playSound( musicTrack );
                try {
                   Thread.currentThread().sleep( 500 );
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            // play sound once if loop variable is not set
            soundPlayer.playSound( musicTrack );
            // Sleep thread so that we cannot play sound multiple times in
            // a small timeframe.
            try {
                Thread.currentThread().sleep( 500 );
                setIsNotPlaying(); // done playing sound
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
