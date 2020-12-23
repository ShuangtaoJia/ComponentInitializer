package com.billy.android.register1
/**
 * 已扫描到接口或者codeInsertToClassName jar的信息
 * @author zhangkb
 * @since 2018/04/17
 */
class ScanJarHarvest {
    List<Harvest> harvestList = new ArrayList<>()
    class Harvest {
        String className
        String interfaceName
        boolean isInitClass
    }
}