package com.jst.module3;

import android.content.Context;
import android.util.Log;

import com.jst.compinit.IComponent;
import com.jst.compinit.annotation.Component;

@Component(
        name = "Module3Component"
)
public class Module3Component implements IComponent {
    @Override
    public void init(Context context) {
        Log.i("compinit_test","Module3Component init");
    }
}
