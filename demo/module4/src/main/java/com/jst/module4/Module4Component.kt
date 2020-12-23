package com.jst.module4

import android.content.Context
import android.util.Log
import com.jst.compinit.IComponent
import com.jst.compinit.annotation.Component

@Component
class Module4Component : IComponent {
    override fun init(context: Context) {
        Log.i("compinit_test", "Module4Component init")
    }
}