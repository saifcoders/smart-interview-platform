package com.mdsaifullah.smartinterview.repository;

import com.mdsaifullah.smartinterview.entity.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    List<InterviewSession> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<InterviewSession> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime dateTime);
}
