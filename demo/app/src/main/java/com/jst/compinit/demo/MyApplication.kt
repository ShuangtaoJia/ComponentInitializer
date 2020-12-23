package com.jst.compinit.demo

import android.app.Application
import com.jst.compinit.ComponentInitializer

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ComponentInitializer.setDebug(true)
        ComponentInitializer.initComponents(this)
    }
}