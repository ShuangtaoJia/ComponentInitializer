package com.jst.module1

import android.content.Context
import android.util.Log
import com.jst.compinit.IComponent
import com.jst.compinit.annotation.Component

@Component(name = "Module1Component", dependencies = ["Module2Component", "Module3Component"])
class Module1Component : IComponent {
    override fun init(context: Context) {
        Log.i("compinit_test", "Module1Component init")
    }
}