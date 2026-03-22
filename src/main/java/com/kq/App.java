package com.kq;

import com.kq.core.OmniArchApplication;
import com.kq.core.annotations.ComponentScan;
import com.kq.examples.UserController;

@ComponentScan(include = "com.kq.examples")
public class App {
    public static void main(String[] args)
            throws Exception {
        var componentFactory = OmniArchApplication.run(App.class);

        System.out.println(((UserController)componentFactory.getComponent("controller")).getUserByName("Yaser").getName());




    }
}
