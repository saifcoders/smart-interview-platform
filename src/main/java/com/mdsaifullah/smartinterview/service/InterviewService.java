package com.mdsaifullah.smartinterview.service;

import com.mdsaifullah.smartinterview.dto.AiFeedbackResponse;
import com.mdsaifullah.smartinterview.entity.InterviewAnswer;
import com.mdsaifullah.smartinterview.entity.InterviewSession;
import com.mdsaifullah.smartinterview.repository.InterviewAnswerRepository;
import com.mdsaifullah.smartinterview.repository.InterviewSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InterviewService {

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private InterviewAnswerRepository answerRepository;

    @Autowired
    private AiService aiService;

    private static final List<String> VALID_CATEGORIES = Arrays.asList(
            "Java", "Python", "C++", "Data Structures & Algorithms",
            "Database / SQL", "Computer Networks", "Operating Systems", "HR / Behavioral"
    );

    /**
     * Starts a new mock interview session and generates the first question.
     */
    public InterviewSession startInterview(Long userId, String category) {
        if (!VALID_CATEGORIES.contains(category)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid interview category selected.");
        }

        // Rate Limit Check: Max 3 sessions per user per day
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        long dailySessions = sessionRepository.countByUserIdAndCreatedAtAfter(userId, todayStart);
        if (dailySessions >= 3) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily mock interview session limit reached (maximum 3 sessions/day).");
        }

        // Initialize a new session
        InterviewSession session = new InterviewSession(null, userId, category, "STARTED", LocalDateTime.now());
        session = sessionRepository.save(session);

        // Generate the first question text
        String firstQuestionText = aiService.generateQuestion(category, new ArrayList<>());

        // Persist the first blank answer card
        InterviewAnswer firstAnswer = new InterviewAnswer(null, session, firstQuestionText, LocalDateTime.now());
        answerRepository.save(firstAnswer);

        // Force session load of the answer list
        session.getAnswers().add(firstAnswer);

        return session;
    }

    /**
     * Submits the user's response to the current interview question.
     */
    public InterviewAnswer submitAnswer(Long userId, Long sessionId, String userAnswerText) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found."));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this session.");
        }

        if (!"STARTED".equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This interview session is already completed.");
        }

        // Locate the active unanswered question
        InterviewAnswer currentAnswer = session.getAnswers().stream()
                .filter(a -> a.getUserAnswer() == null)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active question found to answer in this session."));

        // Validate text input boundaries
        if (userAnswerText == null || userAnswerText.trim().length() < 10) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer is too short. Please write at least 10 characters.");
        }
        if (userAnswerText.length() > 1000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Answer exceeds 1000 characters limit.");
        }

        // Request AI evaluation
        AiFeedbackResponse feedback = aiService.evaluateAnswer(
                session.getCategory(),
                currentAnswer.getQuestionText(),
                userAnswerText
        );

        // Update answer details
        currentAnswer.setUserAnswer(userAnswerText);
        currentAnswer.setScore(feedback.getScore());
        currentAnswer.setStrengths(feedback.getStrengths());
        currentAnswer.setWeaknesses(feedback.getWeaknesses());
        currentAnswer.setMissingPoints(feedback.getMissingPoints());
        currentAnswer.setImprovementSuggestions(feedback.getImprovementSuggestions());
        currentAnswer.setIdealAnswerSummary(feedback.getIdealAnswerSummary());

        return answerRepository.save(currentAnswer);
    }

    /**
     * Generates and returns the next interview question card.
     */
    public InterviewAnswer nextQuestion(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found."));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this session.");
        }

        if (!"STARTED".equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This interview session is already completed.");
        }

        // Verify previous question was answered
        boolean hasUnanswered = session.getAnswers().stream().anyMatch(a -> a.getUserAnswer() == null);
        if (hasUnanswered) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please submit an answer to the current question first.");
        }

        if (session.getAnswers().size() >= 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum question limit (5) reached. Please finish the session.");
        }

        // Format history context
        List<String> previousQuestions = session.getAnswers().stream()
                .map(InterviewAnswer::getQuestionText)
                .collect(Collectors.toList());

        String nextQuestionText = aiService.generateQuestion(session.getCategory(), previousQuestions);

        InterviewAnswer nextAnswer = new InterviewAnswer(null, session, nextQuestionText, LocalDateTime.now());
        nextAnswer = answerRepository.save(nextAnswer);

        session.getAnswers().add(nextAnswer);

        return nextAnswer;
    }

    /**
     * Finishes the interview, calculates final average scores, and drafts overall feedback.
     */
    public InterviewSession finishInterview(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found."));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this session.");
        }

        if (!"STARTED".equals(session.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This interview session is already completed.");
        }

        List<InterviewAnswer> answers = session.getAnswers();
        long answeredCount = answers.stream().filter(a -> a.getUserAnswer() != null).count();
        if (answeredCount == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot finish an interview session with zero answered questions.");
        }

        // Calculate average score
        double avgScore = answers.stream()
                .filter(a -> a.getScore() != null)
                .mapToInt(InterviewAnswer::getScore)
                .average()
                .orElse(0.0);

        session.setAverageScore(Math.round(avgScore * 10.0) / 10.0);

        // Generate overall text feedback
        String feedback;
        if (avgScore >= 8.0) {
            feedback = "Outstanding performance! You showed exceptional technical accuracy, structure, and comprehensive depth. Focus on refining minor implementation nuances.";
        } else if (avgScore >= 6.0) {
            feedback = "Strong technical knowledge. Your answers cover the major concepts, but could benefit from explaining lower-level memory architecture, time complexities, or edge cases.";
        } else {
            feedback = "Keep practicing! Some answers have technical gaps, incorrect terms, or lack structural completeness. Review the suggested improvements and ideal summaries to build a stronger foundation.";
        }

        session.setOverallFeedback(feedback);
        session.setStatus("COMPLETED");
        session.setCompletedAt(LocalDateTime.now());

        // Remove any trailing unanswered question if the user finished early
        answers.removeIf(a -> a.getUserAnswer() == null);

        InterviewSession savedSession = sessionRepository.save(session);
        if (savedSession.getAnswers() != null) {
            savedSession.getAnswers().size();
        }
        return savedSession;
    }

    /**
     * Retrieves all completed interview reports for the user.
     */
    public List<InterviewSession> getInterviewHistory(Long userId) {
        List<InterviewSession> sessions = sessionRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, "COMPLETED");
        for (InterviewSession s : sessions) {
            if (s.getAnswers() != null) {
                s.getAnswers().size();
            }
        }
        return sessions;
    }

    /**
     * Retrieves the report detail for a specific interview session.
     */
    public InterviewSession getInterviewReport(Long userId, Long sessionId) {
        InterviewSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Interview session not found."));

        if (!session.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied to this session.");
        }

        // Initialize lists in lazy loading session
        session.getAnswers().size();

        return session;
    }
}
