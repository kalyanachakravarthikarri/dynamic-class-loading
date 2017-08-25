package com.neophoenix.proxy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Dynamic java class compiler and executer  <br>
 * Demonstrate how to compile dynamic java source code, <br>
 * instantiate instance of the class, and finally call method of the class <br>
 *
 * http://www.beyondlinux.com
 *
 * @author david 2011/07
 *
 */
public class DynamicCompiler {
    /** where shall the compiled class be saved to (should exist already) */
    private static String classOutputFolder = "classes/demo";

    public static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {

            System.out.println("Line Number->" + diagnostic.getLineNumber());
            System.out.println("code->" + diagnostic.getCode());
            System.out.println("Message->" + diagnostic.getMessage(Locale.ENGLISH));
            System.out.println("Source->" + diagnostic.getSource());
            System.out.println(" ");
        }
    }

    /** java File Object represents an in-memory java source file <br>
     * so there is no need to put the source file on hard disk  **/
    public static class InMemoryJavaFileObject extends SimpleJavaFileObject {
        private String contents = null;

        public InMemoryJavaFileObject(String className, String contents) throws Exception {
            super(URI.create("string:///" + className.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.contents = contents;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return contents;
        }
    }

    /** Get a simple Java File Object ,<br>
     * It is just for demo, content of the source code is dynamic in real use case */
    private static JavaFileObject getJavaFileObject() {
        StringBuilder contents = new StringBuilder("package math;" + "public class Calculator { " + "  public void testAdd() { " + "    System.out.println(200+300); " + "  } " + "  public static void main(String[] args) { "
            + "    Calculator cal = new Calculator(); " + "    cal.testAdd(); " + "  } " + "} ");
        JavaFileObject so = null;
        try {
            so = new InMemoryJavaFileObject("math.Calculator", contents.toString());
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return so;
    }

    /** compile your files by JavaCompiler */
    public static void compile(Iterable<? extends JavaFileObject> files) {

        File file = new File(classOutputFolder);
        if (!file.exists()) {
            System.out.println("created " + file.getPath() + " " + file.mkdirs());
        }
        // get system compiler:
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

        // for compilation diagnostic message processing on compilation WARNING/ERROR
        MyDiagnosticListener c = new MyDiagnosticListener();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(c, Locale.ENGLISH, null);
        // specify classes output folder
        Iterable options = Arrays.asList("-d", classOutputFolder);
        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, c, options, null, files);
        Boolean result = task.call();
        if (result == true) {
            // System.out.println("Succeeded");
        }
    }

    /** run class from the compiled byte code file by URLClassloader */
    public static Class loadClass(String className) {
        // Create a File object on the root of the directory
        // containing the class file
        File file = new File(classOutputFolder);
        Class thisClass = null;

        try {
            // Convert File to a URL
            URL url = file.toURL(); // file:/classes/demo
            URL[] urls = new URL[] { url };

            // Create a new class loader with the directory
            ClassLoader loader = new URLClassLoader(urls, null);
            thisClass = loader.loadClass(className);
        } catch (MalformedURLException e) {
        } catch (ClassNotFoundException e) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return thisClass;
    }

    public static JavaFileObject getJavaFileObject(Path path) {
        return new MyJavaFile(path, JavaFileObject.Kind.SOURCE);
    }

    public static class MyJavaFile extends SimpleJavaFileObject {
        Path path = null;

        protected MyJavaFile(Path path, Kind kind) {
            super(path.toUri(), kind);
            this.path = path;
        }

        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            byte[] encoded = Files.readAllBytes(this.path);
            return new String(encoded);
        }

    }

    public static void main(String[] args) throws Exception {
        // 1.Construct an in-memory java source file from your dynamic code
        JavaFileObject file = getJavaFileObject();
        Iterable<? extends JavaFileObject> files = Arrays.asList(file);

        // 2.Compile your files by JavaCompiler
        compile(files);

        // 3.Load your class by URLClassLoader, then instantiate the instance, and call method by reflection
        loadClass("math.Calculator");
    }
}
