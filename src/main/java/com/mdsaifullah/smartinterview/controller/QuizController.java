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

import com.mdsaifullah.smartinterview.entity.Quiz;
import com.mdsaifullah.smartinterview.service.QuizService;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @Autowired
    private QuizService quizService;

    // Create Quiz
    @PostMapping
    public Quiz createQuiz(@RequestBody Quiz quiz) {
        return quizService.createQuiz(quiz);
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

    // Get All Quizzes
    @GetMapping
    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        if (!isAdmin()) {
            return quizzes.stream().map(quiz -> {
                Quiz safeQuiz = new Quiz();
                safeQuiz.setId(quiz.getId());
                safeQuiz.setTitle(quiz.getTitle());
                if (quiz.getQuestions() != null) {
                    List<com.mdsaifullah.smartinterview.entity.Question> safeQuestions = quiz.getQuestions().stream().map(q -> {
                        com.mdsaifullah.smartinterview.entity.Question safeQ = new com.mdsaifullah.smartinterview.entity.Question();
                        safeQ.setId(q.getId());
                        safeQ.setTitle(q.getTitle());
                        safeQ.setOption1(q.getOption1());
                        safeQ.setOption2(q.getOption2());
                        safeQ.setOption3(q.getOption3());
                        safeQ.setOption4(q.getOption4());
                        return safeQ;
                    }).toList();
                    safeQuiz.setQuestions(safeQuestions);
                }
                return safeQuiz;
            }).toList();
        }
        return quizzes;
    }

    // Get Quiz By ID
    @GetMapping("/{id}")
    public Quiz getQuizById(@PathVariable Long id) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz != null && !isAdmin()) {
            Quiz safeQuiz = new Quiz();
            safeQuiz.setId(quiz.getId());
            safeQuiz.setTitle(quiz.getTitle());
            if (quiz.getQuestions() != null) {
                List<com.mdsaifullah.smartinterview.entity.Question> safeQuestions = quiz.getQuestions().stream().map(q -> {
                    com.mdsaifullah.smartinterview.entity.Question safeQ = new com.mdsaifullah.smartinterview.entity.Question();
                    safeQ.setId(q.getId());
                    safeQ.setTitle(q.getTitle());
                    safeQ.setOption1(q.getOption1());
                    safeQ.setOption2(q.getOption2());
                    safeQ.setOption3(q.getOption3());
                    safeQ.setOption4(q.getOption4());
                    return safeQ;
                }).toList();
                safeQuiz.setQuestions(safeQuestions);
            }
            return safeQuiz;
        }
        return quiz;
    }

    // Update Quiz
    @PutMapping("/{id}")
    public Quiz updateQuiz(
            @PathVariable Long id,
            @RequestBody Quiz quiz) {

        return quizService.updateQuiz(id, quiz);
    }

    // Delete Quiz
    @DeleteMapping("/{id}")
    public String deleteQuiz(@PathVariable Long id) {
        return quizService.deleteQuiz(id);
    }
}