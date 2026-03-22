package com.kq.core;

import java.util.Set;


public class OmniArchApplication {
    public static ApplicationContext run(Class<?> applicationClass)
            throws Exception {

        final var context = new ApplicationContext();
        var scanner = new ComponentScanner();
        var componentRegistrar = new ComponentRegistrar();

        Set<Class<?>> definitionsClasses = scanner.scan(applicationClass);

        context.setDefinitions(componentRegistrar.register(definitionsClasses));
        context.populateContext();


        return context;
    }


}
