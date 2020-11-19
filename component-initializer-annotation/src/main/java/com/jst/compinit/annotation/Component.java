package com.jst.compinit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface Component {

    /**
     * Component的name
     *
     * 一般情况下该name可以不设置
     * 如果该Component被其他Component所依赖，则该name必须设置
     */
    String name() default "";

    /**
     * 依赖的Component的name列表
     */
    String[] dependencies() default {};
}
