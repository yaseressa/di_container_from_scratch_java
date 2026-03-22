package com.kq.examples;

import com.kq.core.annotations.Singleton;

@Singleton("controller")
public record UserController(UserService userService) {
    public User getUserByName(String name) {
        return userService.getUserByName(name);
    }
}
