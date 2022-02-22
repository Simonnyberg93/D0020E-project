package com.example.d0020e_project;


import org.opencv.core.Rect;

public class Box {
    Rect rectangle;
    LoopRunnable loop;

    public Box(Rect r, LoopRunnable l){
        this.rectangle = r;
        this.loop = l;
    }

}