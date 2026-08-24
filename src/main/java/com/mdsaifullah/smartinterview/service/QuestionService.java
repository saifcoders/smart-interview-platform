package com.mdsaifullah.smartinterview.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mdsaifullah.smartinterview.entity.Question;
import com.mdsaifullah.smartinterview.entity.Quiz;
import com.mdsaifullah.smartinterview.repository.QuestionRepository;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QuestionService {

    @Autowired
    private QuestionRepository questionRepository;

    private void validateQuestion(Question question) {
        if (question.getTitle() == null || question.getTitle().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Question title is required"
            );
        }
        if (question.getOption1() == null || question.getOption1().trim().isEmpty() ||
            question.getOption2() == null || question.getOption2().trim().isEmpty() ||
            question.getOption3() == null || question.getOption3().trim().isEmpty() ||
            question.getOption4() == null || question.getOption4().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "All four options are required and must not be empty"
            );
        }
        if (question.getCorrectAnswer() == null || question.getCorrectAnswer().trim().isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Correct answer is required"
            );
        }

        // Trim values
        question.setTitle(question.getTitle().trim());
        question.setOption1(question.getOption1().trim());
        question.setOption2(question.getOption2().trim());
        question.setOption3(question.getOption3().trim());
        question.setOption4(question.getOption4().trim());
        question.setCorrectAnswer(question.getCorrectAnswer().trim());

        // Must match options
        String ca = question.getCorrectAnswer();
        if (!ca.equals(question.getOption1()) &&
            !ca.equals(question.getOption2()) &&
            !ca.equals(question.getOption3()) &&
            !ca.equals(question.getOption4())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.BAD_REQUEST, "Correct answer must match one of the options"
            );
        }
    }

    // Add Question
    public Question addQuestion(Question question) {
        if (question != null) {
            validateQuestion(question);
        }
        return questionRepository.save(question);
    }

    // Get All Questions
    public List<Question> getAllQuestions() {
        return questionRepository.findAll();
    }

    // Get Question By ID
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    // Update Question
    public Question updateQuestion(Long id, Question updatedQuestion) {
        if (updatedQuestion != null) {
            validateQuestion(updatedQuestion);
        }

        Optional<Question> optionalQuestion = questionRepository.findById(id);

        if (optionalQuestion.isPresent()) {

            Question question = optionalQuestion.get();

            question.setTitle(updatedQuestion.getTitle());
            question.setOption1(updatedQuestion.getOption1());
            question.setOption2(updatedQuestion.getOption2());
            question.setOption3(updatedQuestion.getOption3());
            question.setOption4(updatedQuestion.getOption4());
            question.setCorrectAnswer(updatedQuestion.getCorrectAnswer());

            return questionRepository.save(question);
        }

        return null;
    }

    // Delete Question
    public String deleteQuestion(Long id) {
        Optional<Question> opt = questionRepository.findById(id);
        if (opt.isPresent()) {
            Question q = opt.get();
            // Disassociate cleanly from any quizzes to synchronize state
            if (q.getQuizzes() != null) {
                for (Quiz quiz : q.getQuizzes()) {
                    quiz.getQuestions().remove(q);
                }
            }
            questionRepository.delete(q);
            return "Question Deleted Successfully";
        }

        return "Question Not Found";
    }
}