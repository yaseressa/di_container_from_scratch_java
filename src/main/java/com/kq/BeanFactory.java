package com.kq;

import com.kq.annotations.ComponentScan;
import com.kq.annotations.Singleton;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class BeanFactory {
    private final static Map<String, Object> singletonBeans = new ConcurrentHashMap<>();

    public static BeanFactory run(Class<?> applicationClass) throws InstantiationException, InvocationTargetException, IllegalAccessException {
        String basePackage = applicationClass.getPackageName();
        if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = applicationClass.getAnnotation(ComponentScan.class);
            var pIncluded = StringUtils.StringPresent(componentScan.include()) ? componentScan.include() : basePackage;
            var pExcluded = componentScan.exclude();

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(basePackage))
                    .filterInputsBy(new FilterBuilder().includePackage(pIncluded).excludePackage(pExcluded))
                    .setScanners(Scanners.TypesAnnotated)
            );

            Set<Class<?>> singletonClasses = reflections.get(
                    Scanners.TypesAnnotated.with(Singleton.class).asClass()
            );
            for (Class<?> singletonClass : singletonClasses) {
                    Singleton singleton = singletonClass.getAnnotation(Singleton.class);
                    String beanName = singletonClass.getSimpleName();
                    if (StringUtils.StringPresent(singleton.value())) {
                        beanName = singleton.value();
                    }
                    if (singletonBeans.containsKey(beanName))
                        throw new InstantiationException("[Bean %s]: Already instantiated".formatted(beanName));

                    singletonBeans.put(beanName, getConstructor(singletonClass).newInstance("Yaser", 20));

            }
        }
        return new BeanFactory();
    }

    public Object getBean(String name) {
        return singletonBeans.get(name);
    }

    public Object getBeans() {
        singletonBeans.forEach((k, v) -> System.out.println(k + ":" +v));
        return singletonBeans.keySet().stream().findFirst();
    }

    private static Constructor<?> getConstructor(Class<?> clazz) {
        return clazz.getDeclaredConstructors()[0];
    }


    private static class StringUtils {
        static boolean StringPresent(String str) {
            return Objects.nonNull(str) && !str.isBlank();
        }
    }

}
