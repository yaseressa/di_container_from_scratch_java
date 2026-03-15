package com.kq;


import com.kq.annotations.ComponentScan;
import javassist.bytecode.DuplicateMemberException;
import java.lang.reflect.InvocationTargetException;

@ComponentScan
public class App
{
    public static void main( String[] args ) throws InvocationTargetException, InstantiationException, IllegalAccessException, DuplicateMemberException {
        var beanFactory = OmniArchApplication.run(App.class);
        User user = (User)beanFactory.getComponent("User");
        System.out.println(user.getName());
        System.out.println(beanFactory.getComponents());

    }
}
