package com.mdsaifullah.smartinterview.migration;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import com.mdsaifullah.smartinterview.repository.QuestionRepository;

import java.util.List;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        System.out.println("MIGRATION: Checking database for quiz-question data migration...");

        // 1. Verify that 'quiz' table exists and contains the 'question_ids' column
        // If the table doesn't exist (e.g. fresh database setup in tests), skip.
        try {
            entityManager.createNativeQuery("SELECT 1 FROM quiz LIMIT 1").getResultList();
        } catch (Exception e) {
            System.out.println("MIGRATION: Quiz table does not exist or schema not loaded yet. Skipping migration.");
            return;
        }

        // 2. Read existing questionIds values from the database BEFORE column could be removed/deprecated.
        // We query this natively using native SQL query:
        List<Object[]> quizzesData;
        try {
            quizzesData = entityManager.createNativeQuery("SELECT id, question_ids FROM quiz").getResultList();
        } catch (Exception e) {
            System.out.println("MIGRATION: Failed to query question_ids column (may not exist in schema). Skipping migration.");
            return;
        }

        System.out.println("MIGRATION: Found " + quizzesData.size() + " quizzes to check/migrate.");

        for (Object[] row : quizzesData) {
            Long quizId = ((Number) row[0]).longValue();
            String questionIdsStr = (String) row[1];

            if (questionIdsStr != null && !questionIdsStr.trim().isEmpty()) {
                System.out.println("MIGRATION: Migrating relationships for Quiz ID: " + quizId);

                String[] parts = questionIdsStr.split(",");
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (!trimmed.isEmpty()) {
                        try {
                            Long qId = Long.parseLong(trimmed);

                            // Validate non-existent question IDs (Safety Rule 2: DO NOT silently filter/discard without logging)
                            if (!questionRepository.existsById(qId)) {
                                System.err.println("MIGRATION WARNING: Quiz ID " + quizId + " contains non-existent Question ID: " + qId);
                                continue;
                            }

                            // Idempotency: Check if relationship already exists
                            List<?> existing = entityManager.createNativeQuery(
                                "SELECT 1 FROM quiz_questions WHERE quiz_id = ? AND question_id = ?")
                                .setParameter(1, quizId)
                                .setParameter(2, qId)
                                .getResultList();

                            if (existing.isEmpty()) {
                                System.out.println("MIGRATION: Inserting relation quiz_id=" + quizId + ", question_id=" + qId);
                                entityManager.createNativeQuery(
                                    "INSERT INTO quiz_questions (quiz_id, question_id) VALUES (?, ?)")
                                    .setParameter(1, quizId)
                                    .setParameter(2, qId)
                                    .executeUpdate();
                            } else {
                                System.out.println("MIGRATION: Relation quiz_id=" + quizId + ", question_id=" + qId + " already exists. Skipping.");
                            }

                        } catch (NumberFormatException e) {
                            System.err.println("MIGRATION ERROR: Malformed question ID: '" + trimmed + "' in Quiz ID: " + quizId);
                        }
                    }
                }
            }
        }
        System.out.println("MIGRATION: Process completed successfully.");
    }
}
