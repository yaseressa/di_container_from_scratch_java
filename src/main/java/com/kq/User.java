package com.kq;

import com.kq.annotations.Singleton;

@Singleton
public record User(String name, Integer age) {
    public User {
        if (name == null) name = "Yaser";
        if (age == null) age = 0;
    }
}
