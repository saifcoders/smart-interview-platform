package com.mdsaifullah.smartinterview.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdsaifullah.smartinterview.entity.Question;
import com.mdsaifullah.smartinterview.service.QuestionService;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    // Add Question API
    @PostMapping
    public Question addQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    // Get All Questions API
    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }
}