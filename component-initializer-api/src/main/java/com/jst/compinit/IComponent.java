package com.jst.compinit;

import android.content.Context;

/**
 * 组件接口
 *
 * 所有的组件要实现该接口来完成初始化
 */
public interface IComponent {
    void init(Context context);
}
