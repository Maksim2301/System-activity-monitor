package com.example.modules.user.service;

import com.example.dto.UserLoginDto;
import com.example.dto.UserPasswordChangeDto;
import com.example.dto.UserRegisterDto;
import com.example.model.User;
import com.example.repository.interfaces.UserRepository;
import com.example.util.SimplePasswordEncoder;

import java.util.Optional;


public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(UserRegisterDto dto) {

        if (dto.getUsername() == null || dto.getUsername().isBlank())
            throw new IllegalArgumentException("Username cannot be empty.");

        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new IllegalArgumentException("The password cannot be empty.");

        String hash = SimplePasswordEncoder.hash(dto.getPassword());

        User user = new User(dto.getUsername(), hash, dto.getEmail());
        userRepository.save(user);

        System.out.println("User '" + dto.getUsername() + "' registered.");
        return user;
    }

    public Optional<User> login(UserLoginDto dto) {

        if (dto.getUsername() == null || dto.getUsername().isBlank())
            return Optional.empty();

        if (dto.getPassword() == null || dto.getPassword().isBlank())
            return Optional.empty();

        Optional<User> found = userRepository.findByUsername(dto.getUsername());

        if (found.isPresent()) {
            User user = found.get();

            String hashedInput = SimplePasswordEncoder.hash(dto.getPassword());

            if (hashedInput.equals(user.getPasswordHash())) {
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    public void changePassword(Integer userId, UserPasswordChangeDto dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String oldHash = SimplePasswordEncoder.hash(dto.getOldPassword());

        if (!oldHash.equals(user.getPasswordHash()))
            throw new SecurityException("Incorrect old password.");

        String newHash = SimplePasswordEncoder.hash(dto.getNewPassword());

        userRepository.updatePassword(userId, newHash);

        user.setPasswordHash(newHash);

        System.out.println("Password updated.");
    }

    public void deleteUser(Integer id) {
        if (id == null)
            throw new IllegalArgumentException("ID cannot be null.");

        userRepository.deleteById(id);
        System.out.println("User deleted: ID=" + id);
    }

    private void validateUser(User user) {
        if (user == null || user.getId() == null)
            throw new IllegalArgumentException("User not saved or null.");
    }
}
