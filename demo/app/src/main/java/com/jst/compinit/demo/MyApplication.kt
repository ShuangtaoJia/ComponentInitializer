package com.jst.compinit.demo;

import android.app.Application;

import com.jst.compinit.ComponentInitializer;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ComponentInitializer.setDebug(true);
        ComponentInitializer.initComponents(this);
    }
}
