package com.kq;

import com.kq.annotations.ComponentScan;
import com.kq.annotations.Lazy;
import com.kq.annotations.Prototype;
import com.kq.annotations.Singleton;
import com.kq.utils.StringUtils;
import javassist.bytecode.DuplicateMemberException;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

public class OmniArchApplication {
    public static ApplicationContext run(Class<?> applicationClass) throws InstantiationException, InvocationTargetException, IllegalAccessException, DuplicateMemberException {

        final ApplicationContext context = new ApplicationContext();
        var scanner = new ComponentScanner();

        Set<Class<?>> singletonClasses = new HashSet<>(), prototypeClasses = new HashSet<>();

        if (scanner.scan(context, applicationClass, singletonClasses, prototypeClasses)) {
            new SingletonStrategy().register(singletonClasses, context);
            new PrototypalStrategy().register(prototypeClasses, context);
        }
        return context;
    }

    static class ComponentScanner {
        boolean scan(ApplicationContext context, Class<?> applicationClass, Set<Class<?>> singletonClasses, Set<Class<?>> prototypalClasses) {
            String basePackage = applicationClass.getPackageName();
            if (applicationClass.isAnnotationPresent(ComponentScan.class)) {
                ComponentScan componentScan = applicationClass.getAnnotation(ComponentScan.class);
                var pIncluded = StringUtils.stringPresent(componentScan.include()) ? componentScan.include() : basePackage;
                var pExcluded = componentScan.exclude();

                Reflections reflections = new Reflections(new ConfigurationBuilder()
                        .setUrls(ClasspathHelper.forPackage(basePackage))
                        .filterInputsBy(new FilterBuilder().includePackage(pIncluded).excludePackage(pExcluded))
                        .setScanners(Scanners.TypesAnnotated)
                );
                Set<Class<?>> singletonSet = reflections.get(
                        Scanners.TypesAnnotated.with(Singleton.class).asClass()
                );
                Set<Class<?>> prototypalSet = reflections.get(
                        Scanners.TypesAnnotated.with(Prototype.class).asClass()
                );
                singletonSet.forEach(s -> context.getSingletonDefinitions().put(s.getSimpleName(), s));

                singletonClasses.addAll(singletonSet);
                prototypalClasses.addAll(prototypalSet);

                return true;
            }
            return false;
        }
    }

    static class PrototypalStrategy implements ComponentRegistrationStrategy {
        @Override
        public void register(Set<Class<?>> classes, ApplicationContext context) throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
            for (Class<?> prototypeClass : classes) {

                Prototype prototypal = prototypeClass.getAnnotation(Prototype.class);
                String name = prototypeClass.getSimpleName();

                if (StringUtils.stringPresent(prototypal.value())) {
                    name = prototypal.value();
                }

                context.registerPrototype(prototypeClass, name);
            }
        }
    }

    static class SingletonStrategy implements ComponentRegistrationStrategy {
        @Override
        public void register(Set<Class<?>> classes, ApplicationContext context) throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException {
            for (Class<?> singletonClass : classes) {

                Singleton singleton = singletonClass.getAnnotation(Singleton.class);
                String name = singletonClass.getSimpleName();
                if (StringUtils.stringPresent(singleton.value())) {
                    name = singleton.value();
                }
                if (singletonClass.isAnnotationPresent(Lazy.class)) {
                    context.getLazyComponents().put(name, singletonClass);
                    continue;
                }
                if (context.getSingletonComponents().containsKey(name))
                    throw new InstantiationException("[Component %s]: Already instantiated".formatted(name));

                context.addSingleton(singletonClass, name);

            }
        }
    }


}


interface ComponentRegistrationStrategy {
    void register(Set<Class<?>> classes, ApplicationContext context) throws DuplicateMemberException, InvocationTargetException, InstantiationException, IllegalAccessException;
}

