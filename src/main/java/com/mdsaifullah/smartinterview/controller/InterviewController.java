package com.mdsaifullah.smartinterview.controller;

import com.mdsaifullah.smartinterview.entity.InterviewAnswer;
import com.mdsaifullah.smartinterview.entity.InterviewSession;
import com.mdsaifullah.smartinterview.repository.UserRepository;
import com.mdsaifullah.smartinterview.service.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private UserRepository userRepository;

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

    private Long verifyUser() {
        Long userId = getAuthenticatedUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        return userId;
    }

    @PostMapping("/start")
    public InterviewSession startInterview(@RequestParam String category) {
        Long userId = verifyUser();
        return interviewService.startInterview(userId, category);
    }

    @PostMapping("/{sessionId}/submit")
    public InterviewAnswer submitAnswer(
            @PathVariable Long sessionId,
            @RequestBody Map<String, String> payload) {
        Long userId = verifyUser();
        String answer = payload != null ? payload.get("userAnswer") : null;
        return interviewService.submitAnswer(userId, sessionId, answer);
    }

    @PostMapping("/{sessionId}/next")
    public InterviewAnswer nextQuestion(@PathVariable Long sessionId) {
        Long userId = verifyUser();
        return interviewService.nextQuestion(userId, sessionId);
    }

    @PostMapping("/{sessionId}/finish")
    public InterviewSession finishInterview(@PathVariable Long sessionId) {
        Long userId = verifyUser();
        return interviewService.finishInterview(userId, sessionId);
    }

    @GetMapping("/history")
    public List<InterviewSession> getInterviewHistory() {
        Long userId = verifyUser();
        return interviewService.getInterviewHistory(userId);
    }

    @GetMapping("/{sessionId}/report")
    public InterviewSession getInterviewReport(@PathVariable Long sessionId) {
        Long userId = verifyUser();
        return interviewService.getInterviewReport(userId, sessionId);
    }
}
