package com.jst.compinit;

/**
 * 用户的Component的相关信息
 */
public interface IComponentInfo {
    /**
     * 用户配置的@Component注解里的name
     */
    String getName();
    /**
     * 用户配置的@Component注解里的dependencies
     */
    String[] getDependencies();
    /**
     * 用户配置的@Component注解所注解的类
     */
    IComponent getComponent();
}
