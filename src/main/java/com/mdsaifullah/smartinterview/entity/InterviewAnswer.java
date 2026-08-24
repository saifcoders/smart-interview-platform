package com.mdsaifullah.smartinterview.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "interview_answers")
public class InterviewAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    @JsonIgnore
    private InterviewSession session;

    @Column(name = "question_text", nullable = false, length = 1000)
    private String questionText;

    @Column(name = "user_answer", length = 2000)
    private String userAnswer;

    private Integer score;

    @Column(length = 2000)
    private String strengths;

    @Column(length = 2000)
    private String weaknesses;

    @Column(name = "missing_points", length = 2000)
    private String missingPoints;

    @Column(name = "improvement_suggestions", length = 2000)
    private String improvementSuggestions;

    @Column(name = "ideal_answer_summary", length = 2000)
    private String idealAnswerSummary;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public InterviewAnswer() {
    }

    public InterviewAnswer(Long id, InterviewSession session, String questionText, LocalDateTime createdAt) {
        this.id = id;
        this.session = session;
        this.questionText = questionText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InterviewSession getSession() {
        return session;
    }

    public void setSession(InterviewSession session) {
        this.session = session;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public String getStrengths() {
        return strengths;
    }

    public void setStrengths(String strengths) {
        this.strengths = strengths;
    }

    public String getWeaknesses() {
        return weaknesses;
    }

    public void setWeaknesses(String weaknesses) {
        this.weaknesses = weaknesses;
    }

    public String getMissingPoints() {
        return missingPoints;
    }

    public void setMissingPoints(String missingPoints) {
        this.missingPoints = missingPoints;
    }

    public String getImprovementSuggestions() {
        return improvementSuggestions;
    }

    public void setImprovementSuggestions(String improvementSuggestions) {
        this.improvementSuggestions = improvementSuggestions;
    }

    public String getIdealAnswerSummary() {
        return idealAnswerSummary;
    }

    public void setIdealAnswerSummary(String idealAnswerSummary) {
        this.idealAnswerSummary = idealAnswerSummary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
