package com.mdsaifullah.smartinterview.entity;

import java.util.Map;

public class QuizSubmission {

    private Long userId;

    private Long quizId;

    private Map<Long, String> answers;

    public QuizSubmission() {
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Map<Long, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<Long, String> answers) {
        this.answers = answers;
    }
}