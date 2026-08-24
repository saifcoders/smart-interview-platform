package com.mdsaifullah.smartinterview.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.mdsaifullah.smartinterview.entity.User;
import com.mdsaifullah.smartinterview.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    // Register User
    public User registerUser(User user) {

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Name is required"
            );
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Email is required"
            );
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Password is required"
            );
        }

        // Normalize email
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        user.setEmail(normalizedEmail);

        // Validate format
        if (!normalizedEmail.matches(EMAIL_REGEX)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid email format"
            );
        }

        // Validate password length
        if (user.getPassword().length() < 8) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long"
            );
        }

        // Check if email already registered
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.CONFLICT, "Email is already registered"
            );
        }

        // Public registration must always create USER role
        user.setRole("USER");

        // Encrypt password
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        return userRepository.save(user);
    }

    // Login User
    public User loginUser(String email, String password) {

        Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {

            boolean passwordMatched =
                    passwordEncoder.matches(
                            password,
                            user.get().getPassword()
                    );

            if (passwordMatched) {
                return user.get();
            }
        }

        return null;
    }

    // Get All Users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get User By ID
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    // Update User
    public User updateUser(Long id, User updatedUser) {

        Optional<User> optionalUser =
                userRepository.findById(id);

        if (optionalUser.isPresent()) {

            User user = optionalUser.get();

            if (updatedUser.getName() == null || updatedUser.getName().trim().isEmpty()) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Name is required"
                );
            }
            if (updatedUser.getEmail() == null || updatedUser.getEmail().trim().isEmpty()) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Email is required"
                );
            }

            // Normalize and validate email
            String normalizedEmail = updatedUser.getEmail().trim().toLowerCase();
            if (!normalizedEmail.matches(EMAIL_REGEX)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Invalid email format"
                );
            }

            // Verify email uniqueness (ignoring current user)
            Optional<User> existing = userRepository.findByEmail(normalizedEmail);
            if (existing.isPresent() && !existing.get().getId().equals(id)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.CONFLICT, "Email is already registered"
                );
            }

            user.setName(updatedUser.getName().trim());
            user.setEmail(normalizedEmail);

            // Update password only if provided
            if (updatedUser.getPassword() != null
                    && !updatedUser.getPassword().isEmpty()) {

                if (updatedUser.getPassword().length() < 8) {
                    throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Password must be at least 8 characters long"
                    );
                }

                user.setPassword(
                        passwordEncoder.encode(
                                updatedUser.getPassword()
                        )
                );
            }

            return userRepository.save(user);
        }

        return null;
    }

    // Delete User
    public String deleteUser(Long id) {

        if (userRepository.existsById(id)) {

            userRepository.deleteById(id);

            return "User Deleted Successfully";
        }

        return "User Not Found";
    }
}