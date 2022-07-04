package com.plugin.inject

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.Transform
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.gradle.api.Project

public class ClassInjectTransform extends Transform {

    Project project

    /**
     * 构造方法，保留原project备用
     */
    ClassInjectTransform(Project project) {
        this.project = project
    }

    /**
     * 设置自定义 Transform 对应的 Task 名称
     * 类似：TransformClassesWithPreDexForXXX，对应的 task 名称为：transformClassesWithMyTransformForDebug
     * 会生成目录 build/intermediates/transforms/MyTransform/
     */
    @Override
    String getName() {
        return "ClassInjectTransform"
    }

    /**
     * 指定输入的类型，可指定我们要处理的文件类型（保证其他类型文件不会传入）
     * CLASSES - 表示处理java的class文件
     * RESOURCES - 表示处理java的资源
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    /**
     * 指定 Transform 的作用范围
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.PROJECT_ONLY
    }

    /**
     * 是否支持增量编译
     */
    @Override
    boolean isIncremental() {
        return false
    }

    /**
     * 核心方法，具体如何处理输入和输出
     * @param inputs          为传过来的输入流，两种格式，一种jar包格式，一种目录格式
     * @param outputProvider  获取到输出目录，最后将修改的文件复制到输出目录，这一步必须执行，不然编译会报错
     */
    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {

        System.out.println("----------Transform begin----------- ")
        // Transform 的 inputs 分为两种类型，一直是目录，一种是 jar 包。需要分开遍历

        inputs.each { TransformInput input ->

            input.directoryInputs.each { DirectoryInput dirInput ->
                System.out.println("dir input:" + dirInput.file.absolutePath)
                ClassInjector.inject(dirInput.file.absolutePath, project)
                def dest = outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
                System.out.println("dir output:" + dest)
                FileUtils.copyDirectory(dirInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                def dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                System.out.println("jar input:" + jarInput.file.absolutePath)
                System.out.println("jar output:" + dest)
                FileUtils.copyFile(jarInput.file, dest)
            }

        }

        System.out.println("----------Transform end-----------")
    }
}
