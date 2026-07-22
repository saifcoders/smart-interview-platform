package com.mdsaifullah.smartinterview.service;

import java.util.List;
import java.util.Optional;

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

    // Get Question By ID
    public Question getQuestionById(Long id) {
        return questionRepository.findById(id).orElse(null);
    }

    // Update Question
    public Question updateQuestion(Long id, Question updatedQuestion) {

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

        if (questionRepository.existsById(id)) {
            questionRepository.deleteById(id);
            return "Question Deleted Successfully";
        }

        return "Question Not Found";
    }
}