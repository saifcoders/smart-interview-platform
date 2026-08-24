package com.mdsaifullah.smartinterview.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mdsaifullah.smartinterview.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
}