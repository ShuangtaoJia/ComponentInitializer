package com.jst.module1;

import android.content.Context;
import android.util.Log;

import com.jst.compinit.IComponent;
import com.jst.compinit.annotation.Component;

@Component(
        name = "Module1Component",
        dependencies = {"Module2Component","Module3Component"}
)
public class Module1Component implements IComponent {
    @Override
    public void init(Context context) {
        Log.i("compinit_test","Module1Component init");
    }
}
