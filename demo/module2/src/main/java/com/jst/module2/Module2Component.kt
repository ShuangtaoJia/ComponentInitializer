package com.jst.module2

import android.content.Context
import android.util.Log
import com.jst.compinit.IComponent
import com.jst.compinit.annotation.Component

@Component(name = "Module2Component")
class Module2Component : IComponent {
    override fun init(context: Context) {
        Log.i("compinit_test", "Module2Component init")
    }
}