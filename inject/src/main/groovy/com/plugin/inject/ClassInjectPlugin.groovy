package com.plugin.inject


import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension

public class ClassInjectPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        // 注册Transform
        def classTransform = new ClassInjectTransform(project)
        android.registerTransform(classTransform)
        System.out.println("apply " + ClassInjectPlugin.name)
    }
}