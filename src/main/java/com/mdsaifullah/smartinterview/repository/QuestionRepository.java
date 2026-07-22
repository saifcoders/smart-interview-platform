package com.mdsaifullah.smartinterview.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mdsaifullah.smartinterview.entity.Question;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

}