package com.mdsaifullah.smartinterview.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdsaifullah.smartinterview.entity.Question;
import com.mdsaifullah.smartinterview.entity.QuizResult;
import com.mdsaifullah.smartinterview.entity.QuizSubmission;
import com.mdsaifullah.smartinterview.repository.QuestionRepository;
import com.mdsaifullah.smartinterview.repository.QuizResultRepository;

@Service
public class QuizResultService {

    @Autowired
    private QuizResultRepository quizResultRepository;

    @Autowired
    private QuestionRepository questionRepository;

    // Save Quiz Result
    public QuizResult saveResult(QuizResult result) {
        return quizResultRepository.save(result);
    }

    @Autowired
    private com.mdsaifullah.smartinterview.repository.QuizRepository quizRepository;

    // Calculate Score Automatically
    public QuizResult calculateResult(QuizSubmission submission) {
        if (submission.getQuizId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Quiz ID cannot be null"
            );
        }

        com.mdsaifullah.smartinterview.entity.Quiz quiz = quizRepository.findById(submission.getQuizId())
            .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.NOT_FOUND, "Quiz with ID " + submission.getQuizId() + " not found"
            ));

        List<Long> quizQuestionIds = new java.util.ArrayList<>();
        if (quiz.getQuestions() != null) {
            for (Question q : quiz.getQuestions()) {
                quizQuestionIds.add(q.getId());
            }
        }

        Map<Long, String> answers = submission.getAnswers();
        if (answers == null) {
            answers = new java.util.HashMap<>();
        }

        // Validate that submitted questions belong to this quiz
        for (Long questionId : answers.keySet()) {
            if (!quizQuestionIds.contains(questionId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Question ID " + questionId + " does not belong to this quiz"
                );
            }
        }

        int score = 0;
        int totalQuestions = quizQuestionIds.size();

        for (Long questionId : quizQuestionIds) {
            String userAnswer = answers.get(questionId);
            if (userAnswer != null && !userAnswer.trim().isEmpty()) {
                Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR, "Configured question with ID " + questionId + " not found"
                    ));

                if (question.getCorrectAnswer() != null 
                    && question.getCorrectAnswer().trim().equalsIgnoreCase(userAnswer.trim())) {
                    score++;
                }
            }
        }

        QuizResult result = new QuizResult();
        result.setUserId(submission.getUserId());
        result.setQuizId(submission.getQuizId());
        result.setScore(score);
        result.setTotalQuestions(totalQuestions);

        return quizResultRepository.save(result);
    }

    // Get Result By ID
    public QuizResult getResultById(Long id) {
        return quizResultRepository.findById(id).orElse(null);
    }

    // Get All Results
    public List<QuizResult> getAllResults() {
        return quizResultRepository.findAll();
    }

    // Get Results By User
    public List<QuizResult> getResultsByUser(Long userId) {
        return quizResultRepository.findByUserId(userId);
    }

    // Get Results By Quiz
    public List<QuizResult> getResultsByQuiz(Long quizId) {
        return quizResultRepository.findByQuizId(quizId);
    }

    // Delete Result
    public String deleteResult(Long id) {

        if (quizResultRepository.existsById(id)) {

            quizResultRepository.deleteById(id);

            return "Result Deleted Successfully";
        }

        return "Result Not Found";
    }
}