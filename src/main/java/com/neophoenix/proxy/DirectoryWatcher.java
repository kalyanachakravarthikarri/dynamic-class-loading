package com.neophoenix.proxy;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;

import javax.tools.JavaFileObject;

public class DirectoryWatcher extends Thread {

    @Override
    public void run() {
        try {
            WatchService watchService = FileSystems.getDefault()
                .newWatchService();

            Path path = Paths.get("D:\\Kalyan\\GIT\\dynamic-class-loading\\src\\main\\java\\com\\neophoenix\\proxy");
            System.out.println("Started watching : " + path);
            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.context()
                        .toString()
                        .endsWith(".java")) {
                        // System.out.println("Event kind:" + event.kind() + ". File affected: " + event.context() + ".");
                        Path dir = (Path) key.watchable();
                        Path changedFilePath = dir.resolve((Path) event.context());
                        JavaFileObject file = DynamicCompiler.getJavaFileObject(changedFilePath);
                        Iterable<? extends JavaFileObject> files = Arrays.asList(file);

                        // 2.Compile your files by JavaCompiler
                        DynamicCompiler.compile(files);
                        // System.out.println(changedFile+" exists "+changedFile.exists());
                        Class c = DynamicCompiler.loadClass("com.neophoenix.proxy.TargetClass");
                        Object obj = c.newInstance();
                        // System.out.println("changed object : "+obj.someMethod());
                        SubClasser.registry.put(obj.getClass()
                            .getName(), obj);
                    }
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
