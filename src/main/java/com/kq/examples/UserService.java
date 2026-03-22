package com.kq.examples;

import com.kq.core.annotations.Singleton;

@Singleton("service")
public record UserService(UserRepository userRepository) {
    public User getUserByName(String name){
        return userRepository.getUser(name);
    }
}
