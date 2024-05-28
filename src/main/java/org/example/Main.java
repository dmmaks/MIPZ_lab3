package org.example;

import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Main {

    private static Set<Class<?>> getAllClassesInPackage(String packageName) {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .forPackage(packageName)
                .addScanners(Scanners.SubTypes, Scanners.TypesAnnotated));
        return reflections.getSubTypesOf(Object.class)
                .stream()
                .filter(c -> !c.isInterface() && !c.isEnum() && !Modifier.isAbstract(c.getModifiers()))
                .collect(Collectors.toSet());
    }

    // Depth of Inheritance Tree (DIT)
    public static int calculateDIT(Class<?> clazz) {
        int depth = 0;
        while (clazz.getSuperclass() != null) {
            depth++;
            clazz = clazz.getSuperclass();
        }
        return depth;
    }

    // Number of Children (NOC)
    public static int calculateNOC(Class<?> clazz, Set<Class<?>> allClasses) {
        int count = 0;
        for (Class<?> c : allClasses) {
            if (clazz.equals(c.getSuperclass())) {
                count++;
            }
        }
        return count;
    }

    // Method Hiding Factor (MHF)
    public static double calculateMHF(Class<?>[] classes) {
        double visibleMethods = 0;
        double totalMethods = 0;

        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            totalMethods += methods.length;
            for (Method method : methods) {
                if (Modifier.isPrivate(method.getModifiers())) {
                    visibleMethods++;
                }
            }
        }

        return visibleMethods / totalMethods;
    }

    // Method Inheritance Factor (MIF)
    public static double calculateMIF(Class<?>[] classes) {
        double inheritedMethods = 0;
        double totalMethods = 0;

        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            Method[] allMethods = clazz.getMethods();
            totalMethods += allMethods.length;
            inheritedMethods += allMethods.length - methods.length;
        }

        return inheritedMethods / totalMethods;
    }

    // Attribute Hiding Factor (AHF)
    public static double calculateAHF(Class<?>[] classes) {
        double hiddenAttributes = 0;
        double totalAttributes = 0;

        for (Class<?> clazz : classes) {
            Field[] fields = clazz.getDeclaredFields();
            totalAttributes += fields.length;
            for (Field field : fields) {
                if (Modifier.isPrivate(field.getModifiers())) {
                    hiddenAttributes++;
                }
            }
        }

        return hiddenAttributes / totalAttributes;
    }

    // Attribute Inheritance Factor (AIF)
    public static double calculateAIF(Class<?>[] classes) {
        double inheritedAttributes = 0;
        double totalAttributes = 0;

        for (Class<?> clazz : classes) {
            Field[] fields = clazz.getDeclaredFields();
            Field[] allFields = clazz.getFields();
            totalAttributes += allFields.length;
            inheritedAttributes += allFields.length - fields.length;
        }

        return inheritedAttributes / totalAttributes;
    }

    // Polymorphism Object Factor (POF)
    public static double calculatePOF(Class<?>[] classes) {
        double polymorphicMethods = 0;
        double totalMethods = 0;
        double totalDescendants = 0;

        for (Class<?> clazz : classes) {
            Method[] methods = clazz.getDeclaredMethods();
            totalMethods += methods.length;
            int descendants = calculateNOC(clazz, new HashSet<>(Arrays.asList(classes)));
            totalDescendants += descendants;

            for (Method method : methods) {
                if (Modifier.isAbstract(method.getModifiers())) {
                    polymorphicMethods++;
                }
            }
        }

        return polymorphicMethods / (totalMethods * totalDescendants);
    }

    public static void main(String[] args) {
        String packageName = "java.lang.reflect";
        Set<Class<?>> allClasses = getAllClassesInPackage(packageName);

        Class<?>[] classes = allClasses.toArray(new Class<?>[0]);

        // Calculate and print metrics
        for (Class<?> clazz : classes) {
            System.out.println("Class: " + clazz.getName());
            System.out.println("DIT: " + calculateDIT(clazz));
            System.out.println("NOC: " + calculateNOC(clazz, allClasses));
            System.out.println("MHF: " + calculateMHF(classes));
            System.out.println("MIF: " + calculateMIF(classes));
            System.out.println("AHF: " + calculateAHF(classes));
            System.out.println("AIF: " + calculateAIF(classes));
            System.out.println("POF: " + calculatePOF(classes));
            System.out.println();
        }
    }
}
