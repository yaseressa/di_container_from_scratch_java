package com.kq.examples;

import com.kq.core.annotations.Prototype;

import java.util.List;



@Prototype("repository")
public class UserRepository {
    List<User> users = List.of(
            new User("Yaser", 22),
            new User("Abdi", 50),
            new User("Tarek", 9)
    );

    User getUser(String name) {
        return users.stream().filter(u -> u.getName().equals(name)).findFirst().orElse(null);
    }


}
