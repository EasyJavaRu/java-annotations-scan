package ru.easyjava.java;

import org.reflections.Reflections;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    private static List<Class> findClasses(File directory, String packageName) {
        if (!directory.exists()) {
            return Collections.emptyList();
        }

        List<Class> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return Collections.emptyList();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class")) {
                try {
                    classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
                } catch (ClassNotFoundException e) {
                    //Skip, as we are searching for the classes
                }
            }
        }
        return classes;
    }

    private static void manualScan() throws IOException, URISyntaxException {
        String packageName = Main.class.getPackage().getName();
        String path = packageName.replace(".", "/");

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources(path);
        Iterable<URL> urls = resources::asIterator;
        List<File> dirs = new ArrayList<>();

        for (URL url: urls) {
            dirs.add(new File(url.toURI().getPath()));
        }

        List<Class> classes = dirs.stream().flatMap((File d) -> findClasses(d, packageName).stream()).collect(Collectors.toList());

        for(Class cls: classes) {
            if (cls.isAnnotationPresent(GreeterTarget.class)) {
                GreeterTarget target = (GreeterTarget) cls.getAnnotation(GreeterTarget.class);
                System.out.println(target.value());

            }
        }
    }

    private static void reflectionsScan() {
        Reflections reflections = new Reflections(Main.class.getPackage().getName());
        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(GreeterTarget.class);

        for(Class cls: classes) {
            GreeterTarget target = (GreeterTarget) cls.getAnnotation(GreeterTarget.class);
            System.out.println(target.value());
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException, ClassNotFoundException {
        manualScan();

        reflectionsScan();
    }
}
