package com.jst.compinit;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.jst.compinit.util.TopSortUtil;

import java.util.ArrayList;
import java.util.List;

public class ComponentInitializer {
    private static List<IComponentInfo> componentInfoList= new ArrayList<>();
    private static boolean isDebug = false;
    public static final String LOG_TAG = "compinit_test";

    /**
     * 初始化所有的Component
     *
     * Step 1:检测用户配置的@Component注解参数是否合法，不合法将抛异常提醒用户更改
     * Step 2:根据用户配置的@Component注解里的dependencies参数对所有的Component进行拓扑排序
     * Step 3:按照拓扑排序的结果循环调用@Component注解所注解的组件类的init(Context context)方法
     * @param context
     */
    public static void initComponents(Context context){
        checkDataValid();

        componentInfoList = TopSortUtil.topSort(componentInfoList);

        for (IComponentInfo componentInfo : componentInfoList) {
            if (componentInfo == null ) {
                continue;
            }
            if (componentInfo.getComponent() == null) {
                continue;
            }
            if (isDebug) {
                Log.d(LOG_TAG,"init " + componentInfo.getComponent().getClass().getName());
            }
            componentInfo.getComponent().init(context);
        }
    }

    /**
     * 设置debug模式
     *
     * debug模式下会输出日志，tag 为 ComponentInitializer.LOG_TAG
     * @param isDebug
     */
    public static void setDebug(boolean isDebug){
        ComponentInitializer.isDebug = isDebug;
    }

    /**
     * 检测用户配置的数据是否合法
     * 
     * 1.component name是否重复
     * 2.dependencies 里面配置的name是否存在
     * 3.dependencies 里面配置的name是否是自己
     */
    private static void checkDataValid(){
        //检测component name是否重复
            //取出非空name集合
        List<IComponentInfo> nonNullNameList= new ArrayList<>();
        for (IComponentInfo componentInfo : componentInfoList) {
            if (!TextUtils.isEmpty(componentInfo.getName())) {
                nonNullNameList.add(componentInfo);
            }
        }

        for (int i = 0; i < nonNullNameList.size()-1; i++) {
            for (int j = i+1; j < nonNullNameList.size(); j++) {
                if (nonNullNameList.get(i).getName().equals(nonNullNameList.get(j).getName())) {
                    String errorMsg = nonNullNameList.get(i).getComponent().getClass().getName()
                            + "和"
                            + nonNullNameList.get(j).getComponent().getClass().getName()
                            + "存在相同的component name "
                            + nonNullNameList.get(i).getName();

                    throw new IllegalArgumentException(errorMsg);
                }
            }
        }

        //检测dependencies 里面配置的name是否存在
        for (IComponentInfo componentInfo : componentInfoList) {
            if (componentInfo.getDependencies().length == 0) {
                continue;
            }
            for (String dependency : componentInfo.getDependencies()) {
                boolean isExist = false;
                for (IComponentInfo info : nonNullNameList) {
                    if (dependency.equals(info.getName())) {
                        if (info == componentInfo){//自己依赖了自己
                            String errorMsg = "自己不能依赖自己:"
                                    + componentInfo.getComponent().getClass().getName()
                                    + " 里配置的dependencies 里的component name "
                                    + dependency
                                    + " 是自己";

                            throw new IllegalArgumentException(errorMsg);
                        }

                        isExist = true;
                    }
                }
                if (!isExist) {
                    String errorMsg = componentInfo.getComponent().getClass().getName()
                            + " 里配置的dependencies 里的component name "
                            + dependency
                            + " 不存在";

                    throw new IllegalArgumentException(errorMsg);
                }
            }
        }

    }

    /**
     * 该方法会被该类里自动注入的代码所调用，以完成组件的自动注册
     * 自动注入代码的逻辑是在自定义的gradle plugin 里实现的
     * @param componentInfo
     */
    private static void register(IComponentInfo componentInfo){
        componentInfoList.add(componentInfo);
    }

}
