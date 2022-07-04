package com.plugin.inject

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

import java.lang.reflect.Type

public class ClassInjector {
    public static final TAG = "ClassInjector"
    private static ClassPool classPool = ClassPool.getDefault()
    private static List<Injector> sInjectors = null;

    public static void inject(String path, Project project) {
        classPool.appendClassPath(path)
        classPool.appendClassPath(project.android.bootClasspath[0].toString())
        classPool.importPackage("android.os.Bundle")
        genInjectors(project.getRootDir())

        File dir = new File(path)
        if (!dir.isDirectory()) {
            return;
        }
        printMsg("classesDir:" + path)
        dir.eachFileRecurse { File f ->
            if (!f.isDirectory()) {
                String classFullName = f.getPath().
                        replaceAll(path + "/", "").
                        replace("/", ".").
                        replace(".class", "")
                injectClass(classFullName, path)
            }
        }
    }

    private static boolean injectClass(String className, String classesDir) {
        boolean ret = false;
        Injector injector = findInjector(className)
        if (injector != null) {
            injector.inject(classesDir)
        }

        ret = true;
        return ret;
    }

    private static void genInjectors(File rootDir) {
        try {
            Gson gson = new Gson()
            Reader reader = new InputStreamReader(new FileInputStream(new File(rootDir, "ClassInjector.json")))
            Type type = new TypeToken<List<Injector>>(){}.getType()
            sInjectors = gson.fromJson(reader, type)
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

    public static Injector findInjector(String cls) {
        if (sInjectors == null) {
            return null
        }
        for (Injector injector : sInjectors) {
            if (injector.cls.equals(cls)) {
                return injector
            }
        }
        return null
    }

    private static class Injector {
        public String cls;
        public int cmd;
        public String method;
        public List<SourceCode> sourceCodes;
        public List<LocalVariable> localVariables;

        public static class SourceCode {
            public String code;
            public int lineNum;
        }

        public static class LocalVariable {
            public String name;
            public String type;
        }

        @Override
        String toString() {
            return new Gson().toJson(this)
        }

        public boolean inject(String classesDir) {
            printMsg("inject -> " + toString())
            boolean ret = false

            CtClass ctClass = null;
            try {
                ctClass = classPool.getCtClass(cls)
            } catch (Exception e) {
                e.printStackTrace()
            }
            if (ctClass == null) {
                return ret;
            }
            // 解冻
            if (ctClass.isFrozen()) {
                ctClass.defrost()
            }

            switch (cmd) {
                case 1:
                    insertMethod(ctClass, method, sourceCodes, localVariables)
                    break
            }

            ctClass.writeFile(classesDir)
            ctClass.detach()
            printMsg("inject success")
            return ret
        }

        private void insertMethod(CtClass ctClass, String method,
                                  List<SourceCode> sourceCodes,
                                  List<LocalVariable> localVariables) {
            try {
                CtMethod ctMethod = ctClass.getDeclaredMethod(method)

                printMsg(ctMethod.signature)

                if (localVariables != null) {
                    for (LocalVariable var : localVariables) {
                        ctMethod.addLocalVariable(var.name, getCtType(var.type))
                    }
                }

                if (sourceCodes != null) {
                    for (int i = 0; i < sourceCodes.size(); ++i) {
                        SourceCode sourceCode = sourceCodes.get(i)
                        if (sourceCode.lineNum == -2) {
                            ctMethod.insertAfter(sourceCode.code)
                        }

                        // 方法前插入需要逆序执行
                        sourceCode = sourceCodes.get(sourceCodes.size() - 1 - i)
                        if (sourceCode.lineNum == -1) {
                            ctMethod.insertBefore(sourceCode.code)
                        }
                    }
                }
            } catch(Exception e) {
                e.printStackTrace()
            }

        }
    }

    public static void printMsg(String msg) {
        String formatMsg = String.format("%s:%s", TAG, msg)
        System.out.println(formatMsg)
    }

    public static CtClass getCtType(String type) {
        CtClass ctType = CtClass.voidType
        switch (type) {
            case "int":
                ctType = CtClass.intType;
                break
            case "long":
                ctType = CtClass.longType;
                break
        }
        return ctType
    }
}
