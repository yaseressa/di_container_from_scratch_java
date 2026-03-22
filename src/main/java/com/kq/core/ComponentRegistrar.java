package com.kq.core;

import com.kq.core.annotations.Lazy;
import com.kq.core.annotations.Prototype;
import com.kq.core.annotations.Singleton;
import com.kq.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.kq.core.enums.Scope.PROTOTYPE;
import static com.kq.core.enums.Scope.SINGLETON;

public class ComponentRegistrar {
    public  List<ComponentDefinition> register(Set<Class<?>> classes) {
        List<ComponentDefinition>  definitions = new ArrayList<>();
        for (Class<?> clazz : classes) {
            var isSingleton = clazz.isAnnotationPresent(Singleton.class);
            var name = StringUtils.camelCaseConvertor(clazz.getSimpleName());
            if (!isSingleton) {
                Prototype prototypal = clazz.getAnnotation(Prototype.class);
                if (StringUtils.stringPresent(prototypal.value())) {
                    name = prototypal.value();
                }
            } else {
                Singleton singleton = clazz.getAnnotation(Singleton.class);
                if (StringUtils.stringPresent(singleton.value())) {
                    name = singleton.value();
                }
            }
            ComponentDefinition componentDefinition = ComponentDefinition.builder().name(name).lazy(clazz.isAnnotationPresent(Lazy.class))
                    .type(clazz).scope(isSingleton ? SINGLETON : PROTOTYPE).build();
            definitions.add(componentDefinition);
        }
        return definitions;
    }
}
