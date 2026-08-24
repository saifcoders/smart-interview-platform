package com.mdsaifullah.smartinterview.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Transient
    private String questionIds;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "quiz_questions",
        joinColumns = @JoinColumn(name = "quiz_id", referencedColumnName = "id"),
        inverseJoinColumns = @JoinColumn(name = "question_id", referencedColumnName = "id")
    )
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    private List<Question> questions = new ArrayList<>();

    // Default Constructor
    public Quiz() {
    }

    // Parameterized Constructor
    public Quiz(Long id, String title, String questionIds) {
        this.id = id;
        this.title = title;
        this.questionIds = questionIds;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getQuestionIds() {
        if (questions != null && !questions.isEmpty()) {
            return questions.stream()
                    .map(q -> q.getId().toString())
                    .collect(java.util.stream.Collectors.joining(","));
        }
        return questionIds;
    }

    public void setQuestionIds(String questionIds) {
        this.questionIds = questionIds;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}