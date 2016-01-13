package com.example.allen.helloworld;

/**
 * Created by allen on 1/9/16.
 */
public class MyNDK {

    static {
        System.loadLibrary("MyLibrary");
    }

    public native String getMyString();
}
