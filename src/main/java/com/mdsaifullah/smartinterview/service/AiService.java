package com.mdsaifullah.smartinterview.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mdsaifullah.smartinterview.dto.AiFeedbackResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Generates a new interview question for the given category.
     */
    public String generateQuestion(String category, List<String> previousQuestions) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock_key_for_tests".equals(apiKey)) {
            return getFallbackQuestion(category, previousQuestions);
        }

        try {
            String historyContext = String.join("\n- ", previousQuestions);
            String promptText = "You are an expert technical interviewer. Generate a challenging and relevant interview question for the category: " + category + ".\n"
                    + "Do not repeat or generate anything similar to the following previous questions:\n- " + historyContext + "\n\n"
                    + "Return the response in this exact JSON structure:\n"
                    + "{\n"
                    + "  \"question\": \"(string)\"\n"
                    + "}";

            String jsonPayload = createGeminiRequestBody(promptText);
            String rawResponse = postToGemini(jsonPayload);

            JsonNode root = objectMapper.readTree(rawResponse);
            String textResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            // Extract JSON block if Gemini wrapped it in markdown code blocks
            textResponse = cleanJsonMarkdown(textResponse);

            JsonNode parsedQuestion = objectMapper.readTree(textResponse);
            return parsedQuestion.path("question").asText("Describe the main concepts of " + category + ".");

        } catch (Exception e) {
            logger.error("Gemini API Question Generation Failed: {}", e.getMessage());
            return getFallbackQuestion(category, previousQuestions);
        }
    }

    /**
     * Evaluates a candidate's answer and returns structured feedback.
     */
    public AiFeedbackResponse evaluateAnswer(String category, String question, String userAnswer) {
        if (apiKey == null || apiKey.trim().isEmpty() || "mock_key_for_tests".equals(apiKey)) {
            return getFallbackFeedback(category, question, userAnswer);
        }

        try {
            String promptText = "Evaluate the candidate's answer to the following interview question.\n"
                    + "Category: " + category + "\n"
                    + "Question: " + question + "\n"
                    + "Candidate's Answer: " + userAnswer + "\n\n"
                    + "Rate the answer on a scale from 0 to 10 based on relevance, technical accuracy, completeness, and clarity.\n"
                    + "Return the response in this exact JSON structure (all keys are required, do not change casing):\n"
                    + "{\n"
                    + "  \"score\": (integer 0-10),\n"
                    + "  \"strengths\": \"(concise list of what the candidate answered well)\",\n"
                    + "  \"weaknesses\": \"(concise list of gaps or technical inaccuracies in candidate's response)\",\n"
                    + "  \"missing_points\": \"(important points or technical terms that were omitted)\",\n"
                    + "  \"improvement_suggestions\": \"(actionable steps to make the response better)\",\n"
                    + "  \"ideal_answer_summary\": \"(a comprehensive, high-quality reference answer in 2-3 sentences)\"\n"
                    + "}";

            String jsonPayload = createGeminiRequestBody(promptText);
            String rawResponse = postToGemini(jsonPayload);

            JsonNode root = objectMapper.readTree(rawResponse);
            String textResponse = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();

            textResponse = cleanJsonMarkdown(textResponse);

            return objectMapper.readValue(textResponse, AiFeedbackResponse.class);

        } catch (Exception e) {
            logger.error("Gemini API Evaluation Failed: {}", e.getMessage());
            return getFallbackFeedback(category, question, userAnswer);
        }
    }

    private String createGeminiRequestBody(String prompt) throws Exception {
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> contentNode = Map.of("parts", List.of(textPart));
        Map<String, Object> configNode = Map.of("responseMimeType", "application/json");

        Map<String, Object> payload = Map.of(
                "contents", List.of(contentNode),
                "generationConfig", configNode
        );

        return objectMapper.writeValueAsString(payload);
    }

    private String postToGemini(String jsonPayload) throws Exception {
        String url = apiUrl + "?key=" + apiKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(15))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini returned HTTP status " + response.statusCode() + ": " + response.body());
        }

        return response.body();
    }

    private String cleanJsonMarkdown(String raw) {
        String cleaned = raw.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private String getFallbackQuestion(String category, List<String> previousQuestions) {
        int index = previousQuestions.size();
        if ("Java".equalsIgnoreCase(category)) {
            String[] questions = {
                "What is the difference between HashMap and Hashtable in Java?",
                "Explain the Java garbage collection mechanism and Heap memory organization.",
                "What are default methods in interfaces, and why were they introduced in Java 8?",
                "What is the difference between checked and unchecked exceptions in Java?",
                "Explain ThreadLocal class and its typical use cases in concurrent applications."
            };
            return questions[index % questions.length];
        } else if ("Database / SQL".equalsIgnoreCase(category)) {
            String[] questions = {
                "What is database normalization, and explain 1NF, 2NF, and 3NF.",
                "What are database indexes, and how does a B-Tree index speed up queries?",
                "Explain ACID properties in transactional databases with real-world examples.",
                "What is the difference between inner join, left join, and outer join?",
                "Explain database locking strategies: Pessimistic vs Optimistic Locking."
            };
            return questions[index % questions.length];
        } else if ("Data Structures & Algorithms".equalsIgnoreCase(category)) {
            String[] questions = {
                "Explain how a Hash Map resolves collisions using chaining vs open addressing.",
                "What is the difference between DFS and BFS traversal, and their time complexities?",
                "Explain the concept of dynamic programming and how it differs from recursion.",
                "How does the Quick Sort algorithm work, and what is its worst-case complexity?",
                "Explain Binary Search Tree (BST) operations and the self-balancing AVL trees."
            };
            return questions[index % questions.length];
        } else {
            // General HR / Behavioral fallback questions
            String[] questions = {
                "Tell me about a challenging project you worked on and how you resolved technical difficulties.",
                "How do you handle conflict in a technical team environment?",
                "What is your approach to learning a new programming language or framework quickly?",
                "Describe a situation where you had to debug a complex production issue under pressure.",
                "Where do you see yourself in five years as a software engineer?"
            };
            return questions[index % questions.length];
        }
    }

    private AiFeedbackResponse getFallbackFeedback(String category, String question, String userAnswer) {
        int score = 7;
        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            score = 1;
        } else if (userAnswer.trim().length() > 50) {
            score = 8;
        }

        return new AiFeedbackResponse(
                score,
                "The candidate provided a structured answer addressing core terminology related to the question.",
                "Could expand more on internal execution steps or detailed architectural details.",
                "Mention key runtime details, edge cases, and performance/memory implications.",
                "Include concrete code patterns or real-world project usage examples to strengthen the explanation.",
                "A strong answer should define the core concept, explain how it operates under the hood, compare it to common alternatives, and state its time/space complexity."
        );
    }
}
