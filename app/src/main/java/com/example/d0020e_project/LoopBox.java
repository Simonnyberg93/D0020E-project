package com.example.d0020e_project;

import android.graphics.Color;
import android.widget.ImageView;

import org.opencv.core.Rect;

import java.util.concurrent.CountDownLatch;

public class LoopBox implements Runnable{
    Boolean pressing = false;
    Rect rectangle;
    private CountDownLatch conditionLatch = new CountDownLatch( 1 );
    boolean isRunning = false;
    ImageView view;

    public LoopBox( Rect r, ImageView v ){
        this.rectangle = r;
        this.view = v;
    }

    synchronized void setIsrunning(){ this.isRunning = true; }

    synchronized void stop() { this.isRunning = false; }

    synchronized void start(){
        setIsrunning();
        new Thread(this).start();
    }

    synchronized void block(){ conditionLatch = new CountDownLatch( 1 ); }

    synchronized void press() { conditionLatch.countDown(); }

    synchronized Boolean isPressed(){
        return pressing;
    }

    synchronized boolean isRunning() { return isRunning; }

    @Override
    public void run() {
        while (isRunning()){
            try {
                // block thread until conditionLatch = 0
                conditionLatch.await();
                pressing = !pressing;

                if (isPressed()){
                    view.setColorFilter( Color.GREEN);
                } else {
                    view.setColorFilter( Color.RED );
                }

                Thread.sleep( 500 );
                block();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}