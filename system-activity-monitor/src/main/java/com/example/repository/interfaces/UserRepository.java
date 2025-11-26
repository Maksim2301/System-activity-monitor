package com.example.repository.interfaces;

import com.example.model.User;
import java.util.Optional;

public interface UserRepository {

    void save(User user);
    Optional<User> findByUsername(String username);
    Optional<User> findById(Integer id);
    void updatePassword(Integer userId, String newPasswordHash);
    void deleteById(Integer id);
}
