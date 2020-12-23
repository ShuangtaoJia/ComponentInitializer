package com.jst.compinit.gradle.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.billy.android.register1.AutoRegisterConfig
import com.billy.android.register1.RegisterTransform
import org.gradle.api.Project;

class Plugin implements org.gradle.api.Plugin<Project> {

    @Override
    void apply(Project project) {
        /**
         * 注册transform接口
         */
        def isApp = project.plugins.hasPlugin(AppPlugin)
        if (isApp) {
            println 'project(' + project.name + ') apply com.jst.component.initializer plugin'
            def android = project.extensions.getByType(AppExtension)
            def transformImpl = new RegisterTransform(project)

            AutoRegisterConfig config = new AutoRegisterConfig()
            Map<String, Object> map = new HashMap<>()
            map.put('scanInterface','com.jst.compinit.IComponentInfo')
            map.put('codeInsertToClassName','com.jst.compinit.ComponentInitializer')
            map.put('registerMethodName','register')

            config.registerInfo.add(map)
            config.project = project
            config.convertConfig()
            transformImpl.config = config
            android.registerTransform(transformImpl)
        }
    }
}