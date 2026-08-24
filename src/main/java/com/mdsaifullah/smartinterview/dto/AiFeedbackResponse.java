package com.mdsaifullah.smartinterview.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiFeedbackResponse {

    private Integer score;
    private String strengths;
    private String weaknesses;

    @JsonProperty("missing_points")
    private String missingPoints;

    @JsonProperty("improvement_suggestions")
    private String improvementSuggestions;

    @JsonProperty("ideal_answer_summary")
    private String idealAnswerSummary;

    public AiFeedbackResponse() {
    }

    public AiFeedbackResponse(Integer score, String strengths, String weaknesses, String missingPoints, String improvementSuggestions, String idealAnswerSummary) {
        this.score = score;
        this.strengths = strengths;
        this.weaknesses = weaknesses;
        this.missingPoints = missingPoints;
        this.improvementSuggestions = improvementSuggestions;
        this.idealAnswerSummary = idealAnswerSummary;
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
}
