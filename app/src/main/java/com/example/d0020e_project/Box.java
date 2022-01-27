package com.example.d0020e_project;

import org.opencv.core.Rect;

public class Box {
    Rect rectangle;
    Thread thread;
    CameraActivity.LoopRunnable loop;

    public Box(Rect r, Thread t, CameraActivity.LoopRunnable l){
        this.rectangle = r;
        this.thread = t;
        this.loop = l;
    }
}