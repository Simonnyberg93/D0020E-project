package com.example.d0020e_project;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

public class LoopRunnable extends Thread implements Runnable {

    CountDownLatch conditionLatch = new CountDownLatch( 1 );
    int musicTrack;
    SoundPlayer soundPlayer;
    boolean run = false;

    public LoopRunnable(int musicTrack, SoundPlayer s){
        this.musicTrack = musicTrack;
        this.soundPlayer = s;
    }

    synchronized int getMusicTrack (){
        return musicTrack;
    }

    synchronized void setMusicTrack(int musicTrack){
        this.musicTrack = musicTrack;
    }

    void startRun(){
        run = true;
    }

    synchronized boolean isRunning(){
        return run;
    }

    synchronized void stopLoop(){
        run = false;
    }

    @Override
    public void run() {
        while(true) { // block instead on variable
            try {
                conditionLatch.await();
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
            this.conditionLatch = new CountDownLatch( 1 );
        }
    }
}
