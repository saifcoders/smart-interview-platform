package com.mdsaifullah.smartinterview.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdsaifullah.smartinterview.entity.Question;
import com.mdsaifullah.smartinterview.repository.QuestionRepository;

@Service
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    // Add Question
    public Question addQuestion(Question question) {
        return questionRepository.save(question);
    }

    // Get All Questions
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }
}