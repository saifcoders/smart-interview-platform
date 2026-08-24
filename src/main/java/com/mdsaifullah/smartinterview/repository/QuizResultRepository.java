package com.mdsaifullah.smartinterview.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdsaifullah.smartinterview.entity.QuizResult;

@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findByUserId(Long userId);

    List<QuizResult> findByQuizId(Long quizId);
}