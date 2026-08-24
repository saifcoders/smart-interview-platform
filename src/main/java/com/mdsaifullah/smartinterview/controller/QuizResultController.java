package com.mdsaifullah.smartinterview.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mdsaifullah.smartinterview.entity.QuizResult;
import com.mdsaifullah.smartinterview.entity.QuizSubmission;
import com.mdsaifullah.smartinterview.service.QuizResultService;

@RestController
@RequestMapping("/api/results")
public class QuizResultController {

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private com.mdsaifullah.smartinterview.repository.UserRepository userRepository;

    private Long getAuthenticatedUserId() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            String email = (String) authentication.getPrincipal();
            return userRepository.findByEmail(email)
                    .map(com.mdsaifullah.smartinterview.entity.User::getId)
                    .orElse(null);
        }
        return null;
    }

    // Save Quiz Result
    @PostMapping
    public QuizResult saveResult(@RequestBody QuizResult result) {
        Long authUserId = getAuthenticatedUserId();
        if (authUserId == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        result.setUserId(authUserId);
        return quizResultService.saveResult(result);
    }

    // Submit Quiz and Calculate Score Automatically
    @PostMapping("/submit")
    public QuizResult submitQuiz(@RequestBody QuizSubmission submission) {
        Long authUserId = getAuthenticatedUserId();
        if (authUserId == null) {
            throw new org.springframework.security.access.AccessDeniedException("User not authenticated");
        }
        submission.setUserId(authUserId);
        return quizResultService.calculateResult(submission);
    }

    // Get Result By ID
    @GetMapping("/{id}")
    public QuizResult getResultById(@PathVariable Long id) {
        return quizResultService.getResultById(id);
    }

    // Get All Results
    @GetMapping
    public List<QuizResult> getAllResults() {
        return quizResultService.getAllResults();
    }

    // Get Results By User
    @GetMapping("/user/{userId}")
    public List<QuizResult> getResultsByUser(@PathVariable Long userId) {
        Long authUserId = getAuthenticatedUserId();
        boolean isAdmin = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin && !userId.equals(authUserId)) {
            throw new org.springframework.security.access.AccessDeniedException("You are not authorized to view this user's results");
        }
        return quizResultService.getResultsByUser(userId);
    }

    // Get Results By Quiz
    @GetMapping("/quiz/{quizId}")
    public List<QuizResult> getResultsByQuiz(@PathVariable Long quizId) {
        return quizResultService.getResultsByQuiz(quizId);
    }

    // Delete Result
    @DeleteMapping("/{id}")
    public String deleteResult(@PathVariable Long id) {
        return quizResultService.deleteResult(id);
    }
}