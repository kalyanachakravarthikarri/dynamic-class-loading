package com.neophoenix.proxy;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;

public class SubClasser {
    
    public static Map<String, Object> registry = new HashMap<String, Object>(); 

    public static class Utilities{
        
        public static Object create(String classs) throws Exception{
            Class targetClass = Class.forName(classs);
            ClassLoader cl = targetClass.getClassLoader();
            System.out.println("classloader : "+cl);
            Object targetInstance = targetClass.newInstance();
            targetInstance = Enhancer.create(targetClass, new MyHandler(targetInstance));
            return targetInstance; 
        }
        
        public static class MyHandler implements InvocationHandler {
            private Object proxy;
            
            public MyHandler(Object proxy) {
                this.proxy = proxy;
                registry.put(proxy.getClass().getName(), this.proxy);
//                System.out.println("Entry set to registry : "+registry);
            }
            
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                System.out.println("This is MyHandler");
                Object target = this.proxy;
                System.out.println("method : "+method.getName());
//                System.out.println("target before retrieving "+target);
                if(registry.containsKey(target.getClass().getName())){
//                    System.out.println("Entry found in map "+registry.get(target.getClass().getName()));
                    target = registry.get(target.getClass().getName());
//                    System.out.println("target after retrieving "+target);
                }
                Method m = target.getClass().getMethod(method.getName(), method.getParameterTypes());
                return m.invoke(target, args);
            }
            
        }
    }
    

}



