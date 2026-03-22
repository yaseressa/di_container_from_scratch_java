package com.kq;

import com.kq.core.annotations.ComponentScan;

@ComponentScan
public class App {
    public static void main(String[] args)
            throws Exception {
        var componentFactory = OmniArchApplication.run(App.class);

        // Singleton
        System.out.println(componentFactory.getComponent("user"));
        System.out.println(componentFactory.getComponent("user"));

        // Prototype
        System.out.println(componentFactory.getComponent("userRepository"));
        System.out.println(componentFactory.getComponent("userRepository"));
        System.out.println(componentFactory.getComponent("userRepository"));


    }
}
