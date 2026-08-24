package com.mdsaifullah.smartinterview.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdsaifullah.smartinterview.entity.Quiz;
import com.mdsaifullah.smartinterview.repository.QuizRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuizService {

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private com.mdsaifullah.smartinterview.repository.QuestionRepository questionRepository;

    private void validateQuiz(Quiz quiz) {
        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Quiz title is required"
            );
        }
        quiz.setTitle(quiz.getTitle().trim());
    }

    private String validateAndNormalizeQuestionIds(String questionIds) {
        if (questionIds == null || questionIds.trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Question IDs cannot be empty"
            );
        }

        String[] parts = questionIds.split(",");
        java.util.HashSet<Long> seenIds = new java.util.HashSet<>();
        java.util.LinkedHashSet<Long> parsedIds = new java.util.LinkedHashSet<>();

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Question IDs contain empty values"
                );
            }
            try {
                Long qId = Long.parseLong(trimmed);
                if (!seenIds.add(qId)) {
                    throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST, "Duplicate question ID found: " + qId
                    );
                }
                parsedIds.add(qId);
            } catch (NumberFormatException e) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Malformed question ID: " + trimmed
                );
            }
        }

        if (parsedIds.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "No valid question IDs provided"
            );
        }

        for (Long qId : parsedIds) {
            if (!questionRepository.existsById(qId)) {
                throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST, "Question with ID " + qId + " does not exist"
                );
            }
        }

        return parsedIds.stream()
                .map(Object::toString)
                .collect(java.util.stream.Collectors.joining(","));
    }

    // Create Quiz
    public Quiz createQuiz(Quiz quiz) {
        validateQuiz(quiz);
        String normalizedIds = validateAndNormalizeQuestionIds(quiz.getQuestionIds());
        List<com.mdsaifullah.smartinterview.entity.Question> questionsList = new java.util.ArrayList<>();
        for (String part : normalizedIds.split(",")) {
            Long qId = Long.parseLong(part.trim());
            questionsList.add(questionRepository.findById(qId).get());
        }
        quiz.setQuestions(questionsList);
        return quizRepository.save(quiz);
    }

    // Get All Quizzes
    public List<Quiz> getAllQuizzes() {
        List<Quiz> quizzes = quizRepository.findAll();
        for (Quiz q : quizzes) {
            if (q.getQuestions() != null) {
                q.getQuestions().size();
            }
        }
        return quizzes;
    }

    // Get Quiz By ID
    public Quiz getQuizById(Long id) {
        Optional<Quiz> q = quizRepository.findById(id);
        if (q.isPresent()) {
            if (q.get().getQuestions() != null) {
                q.get().getQuestions().size();
            }
            return q.get();
        }
        return null;
    }

    // Update Quiz
    public Quiz updateQuiz(Long id, Quiz updatedQuiz) {
        validateQuiz(updatedQuiz);
        Optional<Quiz> optionalQuiz =
                quizRepository.findById(id);

        if (optionalQuiz.isPresent()) {

            Quiz quiz = optionalQuiz.get();

            quiz.setTitle(updatedQuiz.getTitle());
            String normalizedIds = validateAndNormalizeQuestionIds(updatedQuiz.getQuestionIds());

            List<com.mdsaifullah.smartinterview.entity.Question> questionsList = new java.util.ArrayList<>();
            for (String part : normalizedIds.split(",")) {
                Long qId = Long.parseLong(part.trim());
                questionsList.add(questionRepository.findById(qId).get());
            }
            quiz.setQuestions(questionsList);
            return quizRepository.save(quiz);
        }

        return null;
    }

    // Delete Quiz
    public String deleteQuiz(Long id) {

        if (quizRepository.existsById(id)) {

            quizRepository.deleteById(id);

            return "Quiz Deleted Successfully";
        }

        return "Quiz Not Found";
    }
}