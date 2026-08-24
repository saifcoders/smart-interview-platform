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

    private boolean isAdmin() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }

    // Get All Questions API
    @GetMapping
    public List<Question> getAllQuestions() {
        List<Question> questions = questionService.getAllQuestions();
        if (!isAdmin()) {
            return questions.stream().map(q -> {
                Question safeQuestion = new Question();
                safeQuestion.setId(q.getId());
                safeQuestion.setTitle(q.getTitle());
                safeQuestion.setOption1(q.getOption1());
                safeQuestion.setOption2(q.getOption2());
                safeQuestion.setOption3(q.getOption3());
                safeQuestion.setOption4(q.getOption4());
                return safeQuestion;
            }).toList();
        }
        return questions;
    }

    // Get Question By ID API
    @GetMapping("/{id}")
    public Question getQuestionById(@PathVariable Long id) {
        Question question = questionService.getQuestionById(id);
        if (question == null) {
            return null;
        }
        if (!isAdmin()) {
            Question safeQuestion = new Question();
            safeQuestion.setId(question.getId());
            safeQuestion.setTitle(question.getTitle());
            safeQuestion.setOption1(question.getOption1());
            safeQuestion.setOption2(question.getOption2());
            safeQuestion.setOption3(question.getOption3());
            safeQuestion.setOption4(question.getOption4());
            return safeQuestion;
        }
        return question;
    }

    // Update Question API
    @PutMapping("/{id}")
    public Question updateQuestion(@PathVariable Long id,
                                   @RequestBody Question question) {
        return questionService.updateQuestion(id, question);
    }

    // Delete Question API
    @DeleteMapping("/{id}")
    public String deleteQuestion(@PathVariable Long id) {
        return questionService.deleteQuestion(id);
    }
}