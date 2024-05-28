package org.example;

import java.io.IOException;
import java.lang.reflect.*;
import java.util.*;

public class OOMetricsCalculator {

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
    public static double calculateMHF(Class<?> clazz) {
        double hiddenMethods = 0;

        Method[] methods = clazz.getDeclaredMethods();
        double totalMethods = methods.length;
        for (Method method : methods) {
            if (!Modifier.isPublic(method.getModifiers())) {
                hiddenMethods++;
            }
        }

        return totalMethods == 0 ? 0 : hiddenMethods / totalMethods;
    }

    // Method Inheritance Factor (MIF)
    public static double calculateMIF(Class<?> clazz) {
        Set<Method> inheritedMethods = new HashSet<>();
        Set<Method> declaredMethods = new HashSet<>(Arrays.asList(clazz.getDeclaredMethods()));
        Set<Method> allMethods = new HashSet<>(Arrays.asList(clazz.getMethods()));

        allMethods.removeAll(Arrays.asList(Object.class.getMethods()));

        for (Method method : allMethods) {
            if (!declaredMethods.contains(method)) {
                inheritedMethods.add(method);
            }
        }

        double totalMethods = allMethods.size();
        double numInheritedMethods = inheritedMethods.size();

        return totalMethods == 0 ? 0 : numInheritedMethods / totalMethods;
    }


    // Attribute Hiding Factor (AHF)
    public static double calculateAHF(Class<?> clazz) {
        double hiddenAttributes = 0;

        Field[] fields = clazz.getDeclaredFields();
        double totalAttributes = fields.length;
        for (Field field : fields) {
            if (!Modifier.isPublic(field.getModifiers())) {
                hiddenAttributes++;
            }
        }

        return totalAttributes == 0 ? 0 : hiddenAttributes / totalAttributes;
    }

    // Attribute Inheritance Factor (AIF)
    public static double calculateAIF(Class<?> clazz) {
        Set<Field> inheritedFields = new HashSet<>();
        Set<Field> declaredFields = new HashSet<>(Arrays.asList(clazz.getDeclaredFields()));
        Set<Field> allFields = new HashSet<>(Arrays.asList(clazz.getFields()));

        // Identify inherited fields
        for (Field field : allFields) {
            if (!declaredFields.contains(field)) {
                inheritedFields.add(field);
            }
        }

        double totalFields = allFields.size();
        double numInheritedFields = inheritedFields.size();

        return totalFields == 0 ? 0 : numInheritedFields / totalFields;
    }

    //Polymorphism Object Factor
    public static double calculatePOF(Class<?> clazz, Set<Class<?>> allClasses) {
        int newMethods = clazz.getDeclaredMethods().length;
        int totalDescendants = calculateNOC(clazz, allClasses);

        int overriddenMethods = getOverriddenMethods(clazz, allClasses);

        return newMethods == 0 || totalDescendants == 0 ? 0 : (double) overriddenMethods / (newMethods * totalDescendants);
    }

    private static int getOverriddenMethods(Class<?> clazz, Set<Class<?>> allClasses) {
        int overriddenMethods = 0;
        Method[] declaredMethods = clazz.getDeclaredMethods();

        for (Class<?> childClass : allClasses) {
            if (clazz.equals(childClass.getSuperclass())) {
                for (Method method : declaredMethods) {
                    try {
                        Method overriddenMethod = childClass.getMethod(method.getName(), method.getParameterTypes());
                        if (!overriddenMethod.equals(method) && !Modifier.isPrivate(overriddenMethod.getModifiers())) {
                            overriddenMethods++;
                        }
                    } catch (NoSuchMethodException ignored) {
                        // do nothing
                    }
                }
            }
        }
        return overriddenMethods;
    }

//    Unneeded after fix
//    private static Set<Method> getParentMethods(Class<?> clazz) {
//        Set<Method> parentMethods = new HashSet<>();
//        Class<?> superclass = clazz.getSuperclass();
//        if (superclass != null) {
//            parentMethods.addAll(Arrays.asList(superclass.getMethods()));
//        }
//        for (Class<?> iface : clazz.getInterfaces()) {
//            parentMethods.addAll(Arrays.asList(iface.getMethods()));
//        }
//        return parentMethods;
//    }


    public static void calculateOverallMetrics(Class<?>[] classes) {
        double totalDIT = 0;
        double totalNOC = 0;
        double totalHiddenMethods = 0;
        double totalMethods = 0;
        double totalInheritedMethods = 0;
        double totalAttributes = 0;
        double totalHiddenAttributes = 0;
        double totalInheritedAttributes = 0;
        double totalOverriddenMethods = 0;
        double totalNewMethodsXChildren = 0;

        Set<Class<?>> allClassesSet = new HashSet<>(Arrays.asList(classes));

        for (Class<?> clazz : classes) {
            totalDIT += calculateDIT(clazz);
            totalNOC += calculateNOC(clazz, allClassesSet);

            Method[] newMethods = clazz.getDeclaredMethods();
            Method[] allMethods = clazz.getMethods();
            totalMethods += allMethods.length;
            for (Method method : newMethods) {
                if (!Modifier.isPublic(method.getModifiers())) {
                    totalHiddenMethods++;
                }
            }
            totalInheritedMethods += calculateMIF(clazz) * allMethods.length;

            Field[] fields = clazz.getDeclaredFields();
            Field[] allFields = clazz.getFields();
            totalAttributes += allFields.length;
            for (Field field : fields) {
                if (!Modifier.isPublic(field.getModifiers())) {
                    totalHiddenAttributes++;
                }
            }
            totalInheritedAttributes += calculateAIF(clazz) * allFields.length;

            int descendants = calculateNOC(clazz, allClassesSet);
            totalOverriddenMethods += getOverriddenMethods(clazz, allClassesSet);
            totalNewMethodsXChildren  += newMethods.length * descendants;
        }

        double avgDIT = totalDIT / classes.length;
        double mhf = totalMethods == 0 ? 0 : totalHiddenMethods / totalMethods;
        double mif = totalMethods == 0 ? 0 : totalInheritedMethods / totalMethods;
        double ahf = totalAttributes == 0 ? 0 : totalHiddenAttributes / totalAttributes;
        double aif = totalAttributes == 0 ? 0 : totalInheritedAttributes / totalAttributes;
        double pof = totalNewMethodsXChildren == 0 ? 0 : totalOverriddenMethods / totalNewMethodsXChildren;

        System.out.println("Overall Metrics for the JAR:");
        System.out.println("Average DIT: " + avgDIT);
        System.out.println("Total NOC: " + totalNOC);
        System.out.println("MHF: " + mhf);
        System.out.println("MIF: " + mif);
        System.out.println("AHF: " + ahf);
        System.out.println("AIF: " + aif);
        System.out.println("POF: " + pof);
        System.out.println();
    }




    public static void main(String[] args) {
        String path = "C:\\Users\\briks\\.m2\\repository\\org\\reflections\\reflections\\0.10.2\\reflections-0.10.2.jar";
        List<Class<?>> allClasses = null;
        try {
            allClasses = JarScanner.getClasses(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (allClasses != null) {
            Class<?>[] classes = allClasses.toArray(new Class<?>[0]);

            for (Class<?> clazz : classes) {
                System.out.println("Class: " + clazz.getName());
                System.out.println("DIT: " + calculateDIT(clazz));
                System.out.println("NOC: " + calculateNOC(clazz, new HashSet<>(allClasses)));
                System.out.println("MHF: " + calculateMHF(clazz));
                System.out.println("MIF: " + calculateMIF(clazz));
                System.out.println("AHF: " + calculateAHF(clazz));
                System.out.println("AIF: " + calculateAIF(clazz));
                System.out.println("POF: " + calculatePOF(clazz, new HashSet<>(allClasses)));
                System.out.println();
            }

            calculateOverallMetrics(classes);
        }
    }
}
