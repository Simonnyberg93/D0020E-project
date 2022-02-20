package com.example.d0020e_project;


import org.opencv.core.Rect;

public class Box {
    Rect rectangle;
    LoopRunnable loop;
    int drawable;

    public Box(Rect r, LoopRunnable l, int d){
        this.rectangle = r;
        this.loop = l;
        this.drawable = d;
    }

    public Box(Rect r, LoopRunnable l){
        this.rectangle = r;
        this.loop = l;
    }
}