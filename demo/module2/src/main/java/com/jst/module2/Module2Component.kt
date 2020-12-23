package com.jst.module2;

import android.content.Context;
import android.util.Log;

import com.jst.compinit.IComponent;
import com.jst.compinit.annotation.Component;

@Component(
        name = "Module2Component"
)
public class Module2Component implements IComponent {
    @Override
    public void init(Context context) {
        Log.i("compinit_test","Module2Component init");
    }
}
