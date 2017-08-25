package com.neophoenix.proxy;


import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public class Main {
    
    public static void main(String[] args) throws Exception{
//        changeClasses();
        new DirectoryWatcher().start();
        TargetClass target = (TargetClass)SubClasser.Utilities.create("com.neophoenix.proxy.TargetClass");
        System.out.println("target class instance : "+target);
        for(;;){
            System.out.println("target.someMethod() : " +target.someMethod());
            Thread.sleep(10000);
        }
    }
    
    public static void changeClasses() throws Exception{
        ClassPool cp = ClassPool.getDefault();
//        CtClass ctClass = cp.getCtClass("com.neophoenix.proxy.SubClasser$Utilities");
        CtClass ctClass = cp.getCtClass("com.neophoenix.proxy.TargetClass");
        CtMethod ctMethod = ctClass.getDeclaredMethod("someMethod");
        ctMethod.setBody("{System.out.println(\"I will return 1\");return 1;}");
        System.out.println("changeClassess : method -> "+ctMethod.toString());
        ctClass.toClass();
    }

}
