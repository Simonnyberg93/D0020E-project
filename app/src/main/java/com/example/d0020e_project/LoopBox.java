package com.example.d0020e_project;

import org.opencv.core.Rect;

public class LoopBox {
    Boolean pressing = false;
    Rect rectangle;

    public LoopBox(Rect r){
        this.rectangle = r;
    }

    void press() { pressing = !pressing; }

    synchronized Boolean isPressed(){
        return pressing;
    }

}