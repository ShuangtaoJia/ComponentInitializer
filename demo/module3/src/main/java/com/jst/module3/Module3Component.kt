package com.jst.module3

import android.content.Context
import android.util.Log
import com.jst.compinit.IComponent
import com.jst.compinit.annotation.Component

@Component(name = "Module3Component")
class Module3Component : IComponent {
    override fun init(context: Context) {
        Log.i("compinit_test", "Module3Component init")
    }
}