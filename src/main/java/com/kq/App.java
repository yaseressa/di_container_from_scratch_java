package com.kq;


import com.kq.annotations.ComponentScan;

import java.lang.reflect.InvocationTargetException;
@ComponentScan
public class App
{
    public static void main( String[] args ) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        var beanFactory = BeanFactory.run(App.class);
        User user = (User)beanFactory.getBean("User");
        System.out.println(user.name());

    }
}
