package com.mdsaifullah.smartinterview.repository;

import com.mdsaifullah.smartinterview.entity.InterviewAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewAnswerRepository extends JpaRepository<InterviewAnswer, Long> {
}
