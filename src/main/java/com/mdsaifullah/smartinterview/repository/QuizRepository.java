package com.mdsaifullah.smartinterview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdsaifullah.smartinterview.entity.Quiz;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {

}