package com.kq;

import com.kq.annotations.Prototype;

import java.util.List;

class UserEntity {
    String name;
    int age;

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }


    UserEntity(String name, int age) {
        this.name = name;
        this.age = age;
    }
}

@Prototype
public class UserRepository {
    List<UserEntity> users = List.of(
            new UserEntity("Yaser", 22),
            new UserEntity("Abdi", 50),
            new UserEntity("Tarek", 9)
    );

    UserEntity getUser(String name) {
        return users.stream().filter(u -> u.getName().equals(name)).findFirst().orElse(null);
    }
}
