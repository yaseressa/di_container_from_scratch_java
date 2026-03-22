package com.kq.core;

import com.kq.core.annotations.ComponentScan;
import com.kq.core.annotations.Prototype;
import com.kq.core.annotations.Singleton;
import com.kq.utils.StringUtils;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.util.HashSet;
import java.util.Set;

public class ComponentScanner {
    public Set<Class<?>> scan(Class<?> applicationClass) {
        String basePackage = applicationClass.getPackageName();
        if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScan = applicationClass.getAnnotation(ComponentScan.class);
            var pIncluded = StringUtils.stringPresent(componentScan.include()) ? componentScan.include()
                    : basePackage;
            var pExcluded = componentScan.exclude();

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setUrls(ClasspathHelper.forPackage(basePackage))
                    .filterInputsBy(new FilterBuilder().includePackage(pIncluded).excludePackage(pExcluded))
                    .setScanners(Scanners.TypesAnnotated));

            return reflections.get(
                    Scanners.TypesAnnotated.with(Singleton.class, Prototype.class).asClass());
        }
        return new HashSet<>();
    }
}