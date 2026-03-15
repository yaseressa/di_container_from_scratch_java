package com.kq;

import com.kq.annotations.Lazy;
import com.kq.annotations.Singleton;

@Singleton
@Lazy
public class User {
    String name;
    Integer age;

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }

    public User(UserRepository repository) {
        UserEntity yaser = repository.getUser("Yaser");
        name = yaser.name;
        age = yaser.age;
    }
}
