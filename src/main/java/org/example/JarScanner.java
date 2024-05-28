package org.example;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarScanner {

    public static List<Class<?>> getClasses(String path) throws IOException {
        File jarFile = new File(path);
        List<Class<?>> allClasses = new ArrayList<>();

        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.isDirectory() &&!entry.getName().endsWith("!") && entry.getName().endsWith(".class")) {
                    String className = entry.getName().replace('/', '.').substring(0, entry.getName().length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        allClasses.add(clazz);
//                        System.out.println("Loaded class: " + className);
                    } catch (ClassNotFoundException e) {
                        System.err.println("Could not find class: " + className);
                    }
                }
            }
        }

//        for (Class<?> clazz : allClasses) {
//            System.out.println("Class name: " + clazz.getName());
//        }

        return allClasses;
    }
}
