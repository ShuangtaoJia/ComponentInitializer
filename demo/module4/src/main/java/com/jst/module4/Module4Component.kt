package com.jst.module4;

import android.content.Context;
import android.util.Log;

import com.jst.compinit.IComponent;
import com.jst.compinit.annotation.Component;

@Component
public class Module4Component implements IComponent {
    @Override
    public void init(Context context) {
        Log.i("compinit_test","Module4Component init");
    }
}
