package com.mdsaifullah.smartinterview.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mdsaifullah.smartinterview.entity.User;
import com.mdsaifullah.smartinterview.service.JwtService;
import com.mdsaifullah.smartinterview.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private com.mdsaifullah.smartinterview.repository.UserRepository userRepository;

    private Long getAuthenticatedUserId() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String email = (String) authentication.getPrincipal();
            return userRepository.findByEmail(email)
                    .map(com.mdsaifullah.smartinterview.entity.User::getId)
                    .orElse(null);
        }
        return null;
    }

    private boolean isAdmin() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    // Register User
    @PostMapping("/register")
    public User registerUser(@RequestBody User user) {
        return userService.registerUser(user);
    }

    // Login User + Generate JWT Token
    @PostMapping("/login")
    public String loginUser(
            @RequestParam String email,
            @RequestParam String password) {

        User user = userService.loginUser(email, password);

        if (user != null) {

            return jwtService.generateToken(
                    user.getId(),        // Added User ID
                    user.getEmail(),
                    user.getRole()
            );
        }

        return "Invalid Email or Password";
    }

    // Get All Users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // Get User By ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        Long authUserId = getAuthenticatedUserId();
        if (!isAdmin() && !id.equals(authUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this profile");
        }
        return userService.getUserById(id);
    }

    // Update User
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {

        Long authUserId = getAuthenticatedUserId();
        if (!isAdmin() && !id.equals(authUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to update this profile");
        }
        return userService.updateUser(id, user);
    }

    // Delete User
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        Long authUserId = getAuthenticatedUserId();
        if (!isAdmin() && !id.equals(authUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to delete this user");
        }
        return userService.deleteUser(id);
    }
}