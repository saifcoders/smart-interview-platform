package com.mdsaifullah.smartinterview;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.mdsaifullah.smartinterview.entity.Question;
import com.mdsaifullah.smartinterview.entity.Quiz;
import com.mdsaifullah.smartinterview.entity.QuizResult;
import com.mdsaifullah.smartinterview.entity.QuizSubmission;
import com.mdsaifullah.smartinterview.entity.User;
import com.mdsaifullah.smartinterview.repository.QuestionRepository;
import com.mdsaifullah.smartinterview.repository.QuizRepository;
import com.mdsaifullah.smartinterview.repository.UserRepository;
import com.mdsaifullah.smartinterview.service.QuizService;
import com.mdsaifullah.smartinterview.service.QuizResultService;
import com.mdsaifullah.smartinterview.controller.QuizResultController;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
	"jwt.secret=mytestsigningsecretkeymustbelongenough12345",
	"spring.datasource.username=root",
	"spring.datasource.password=Saif@2005"
})
class SmartInterviewPlatformApplicationTests {

	@Autowired
	private org.springframework.transaction.PlatformTransactionManager transactionManager;

	@org.springframework.boot.test.web.server.LocalServerPort
	private int port;

	@Autowired
	private com.mdsaifullah.smartinterview.service.JwtService jwtService;

	@Autowired
	private QuizService quizService;

	@Autowired
	private QuizResultService quizResultService;

	@Autowired
	private QuizResultController quizResultController;

	@Autowired
	private QuestionRepository questionRepository;

	@Autowired
	private QuizRepository quizRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private com.mdsaifullah.smartinterview.repository.InterviewSessionRepository interviewSessionRepository;

	@Autowired
	private com.mdsaifullah.smartinterview.repository.InterviewAnswerRepository interviewAnswerRepository;

	@Autowired
	private com.mdsaifullah.smartinterview.migration.DatabaseMigrationRunner databaseMigrationRunner;

	private Long q1Id;
	private Long q2Id;
	private User testUser;

	@Autowired
	private jakarta.persistence.EntityManager entityManager;

	@BeforeEach
	void setUp() {
		org.springframework.transaction.support.TransactionTemplate template = 
			new org.springframework.transaction.support.TransactionTemplate(transactionManager);
		template.executeWithoutResult(status -> {
			entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
			entityManager.createNativeQuery("DROP TABLE IF EXISTS quiz_question_ids").executeUpdate();
			entityManager.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
		});

		// Clean tables to avoid constraint issues during test
		quizRepository.deleteAll();
		questionRepository.deleteAll();
		userRepository.deleteAll();

		// Set up sample questions
		Question q1 = new Question(null, "What is Java?", "OOP", "Functional", "Logic", "Procedural", "OOP");
		Question q2 = new Question(null, "What is Spring?", "Framework", "Library", "Database", "OS", "Framework");
		
		q1 = questionRepository.save(q1);
		q2 = questionRepository.save(q2);
		
		q1Id = q1.getId();
		q2Id = q2.getId();

		// Set up a test user
		testUser = new User(null, "Test User", "testuser@example.com", "password", "USER");
		testUser = userRepository.save(testUser);
	}

	@Test
	void contextLoads() {
		assertNotNull(quizService);
	}

	// 1. Valid quiz creation
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testValidQuizCreation() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + ", " + q2Id);
		Quiz savedQuiz = quizService.createQuiz(quiz);
		assertNotNull(savedQuiz.getId());
		assertEquals(q1Id + "," + q2Id, savedQuiz.getQuestionIds()); // normalized without spaces
	}

	// 2. Quiz creation with non-existent question ID
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testQuizCreationWithNonExistentQuestionId() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + ",999999");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			quizService.createQuiz(quiz);
		});
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	// 3. Quiz update with non-existent question ID
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testQuizUpdateWithNonExistentQuestionId() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id.toString());
		Quiz savedQuiz = quizService.createQuiz(quiz);

		Quiz updated = new Quiz(savedQuiz.getId(), "Java basics updated", q1Id + ",999999");
		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			quizService.updateQuiz(savedQuiz.getId(), updated);
		});
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	// 4. Valid quiz submission & 8. Server calculates score & 9. Server calculates totalQuestions
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testValidQuizSubmissionAndScoring() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + "," + q2Id);
		quiz = quizService.createQuiz(quiz);

		QuizSubmission submission = new QuizSubmission();
		submission.setUserId(testUser.getId());
		submission.setQuizId(quiz.getId());
		
		Map<Long, String> answers = new HashMap<>();
		answers.put(q1Id, "OOP");
		answers.put(q2Id, "Framework");
		submission.setAnswers(answers);

		QuizResult result = quizResultService.calculateResult(submission);
		assertNotNull(result.getId());
		assertEquals(2, result.getScore());
		assertEquals(2, result.getTotalQuestions());
	}

	// 5. Submission with incomplete answers
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testSubmissionWithIncompleteAnswers() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + "," + q2Id);
		quiz = quizService.createQuiz(quiz);

		QuizSubmission submission = new QuizSubmission();
		submission.setUserId(testUser.getId());
		submission.setQuizId(quiz.getId());

		Map<Long, String> answers = new HashMap<>();
		answers.put(q1Id, "OOP"); // Left Q2 unanswered
		submission.setAnswers(answers);

		QuizResult result = quizResultService.calculateResult(submission);
		assertEquals(1, result.getScore());
		assertEquals(2, result.getTotalQuestions()); // totalQuestions is still 2
	}

	// 6. Submission with invalid question ID (does not belong to quiz)
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testSubmissionWithInvalidQuestionId() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id.toString());
		quiz = quizService.createQuiz(quiz);

		QuizSubmission submission = new QuizSubmission();
		submission.setUserId(testUser.getId());
		submission.setQuizId(quiz.getId());

		Map<Long, String> answers = new HashMap<>();
		answers.put(q1Id, "OOP");
		answers.put(q2Id, "Framework"); // Q2 does not belong to this quiz
		submission.setAnswers(answers);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			quizResultService.calculateResult(submission);
		});
		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
	}

	// 7. Submission with non-existent quiz ID
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testSubmissionWithNonExistentQuizId() {
		QuizSubmission submission = new QuizSubmission();
		submission.setUserId(testUser.getId());
		submission.setQuizId(999999L); // non-existent quiz ID

		Map<Long, String> answers = new HashMap<>();
		answers.put(q1Id, "OOP");
		submission.setAnswers(answers);

		ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
			quizResultService.calculateResult(submission);
		});
		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	// 10. User cannot submit result for another user (Controller level security)
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testUserCannotSubmitResultForAnotherUser() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id.toString());
		quiz = quizService.createQuiz(quiz);

		// Mock authentication context for testUser
		org.springframework.security.core.Authentication auth = 
			new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
				testUser.getEmail(), null, 
				Collections.singletonList(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER"))
			);
		org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

		QuizSubmission submission = new QuizSubmission();
		submission.setUserId(999999L); // Attempting to spoof user ID
		submission.setQuizId(quiz.getId());

		Map<Long, String> answers = new HashMap<>();
		answers.put(q1Id, "OOP");
		submission.setAnswers(answers);

		// Call controller
		QuizResult result = quizResultController.submitQuiz(submission);
		
		// Assert that the userId was overridden with the authenticated user ID (testUser.getId())
		assertEquals(testUser.getId(), result.getUserId());
		assertNotEquals(999999L, result.getUserId());
	}

	// Removing a question from a quiz
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testRemoveQuestionFromQuiz() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + "," + q2Id);
		quiz = quizService.createQuiz(quiz);
		assertEquals(2, quiz.getQuestions().size());

		// Remove Q2
		quiz.getQuestions().removeIf(q -> q.getId().equals(q2Id));
		quiz = quizRepository.save(quiz);

		// Assert relationship updated, but Q2 question itself still exists in DB
		Quiz loaded = quizService.getQuizById(quiz.getId());
		assertEquals(1, loaded.getQuestions().size());
		assertTrue(questionRepository.existsById(q2Id));
	}

	// Deleting a quiz without deleting questions
	@Test
	@org.springframework.transaction.annotation.Transactional
	void testDeleteQuizWithoutDeletingQuestions() {
		Quiz quiz = new Quiz(null, "Java basics", q1Id + "," + q2Id);
		quiz = quizService.createQuiz(quiz);

		Long quizId = quiz.getId();
		quizService.deleteQuiz(quizId);

		assertNull(quizService.getQuizById(quizId));
		assertTrue(questionRepository.existsById(q1Id));
		assertTrue(questionRepository.existsById(q2Id));
	}

	// Existing quiz data migration & duplicate migration prevention & invalid question ID & QuizResult preservation
	@Test
	void testMigrationRunnerIdempotentAndValidations() throws Exception {
		org.springframework.transaction.support.TransactionTemplate template = 
			new org.springframework.transaction.support.TransactionTemplate(transactionManager);
		template.executeWithoutResult(status -> {
			try {
				// Create a raw quiz using native SQL to simulate pre-existing database state
				entityManager.createNativeQuery(
					"INSERT INTO quiz (title, question_ids) VALUES (?, ?)")
					.setParameter(1, "Pre-existing Quiz")
					.setParameter(2, q1Id + ",999999," + q2Id) // q1 is valid, 999999 is invalid, q2 is valid
					.executeUpdate();

				// Get the inserted quiz ID
				Number rawQuizId = (Number) entityManager.createNativeQuery(
					"SELECT id FROM quiz WHERE title = 'Pre-existing Quiz'")
					.getSingleResult();
				Long quizId = rawQuizId.longValue();

				// Save a QuizResult to assert it remains intact
				com.mdsaifullah.smartinterview.entity.QuizResult qr = new com.mdsaifullah.smartinterview.entity.QuizResult();
				qr.setUserId(testUser.getId());
				qr.setQuizId(quizId);
				qr.setScore(1);
				qr.setTotalQuestions(2);
				qr = quizResultService.saveResult(qr);
				Long qrId = qr.getId();

				// Clear join table to isolate migration test
				entityManager.createNativeQuery("DELETE FROM quiz_questions").executeUpdate();

				// Run migration runner
				databaseMigrationRunner.run();

				// Assert relationships populated for valid questions (q1 and q2)
				List<?> joins = entityManager.createNativeQuery(
					"SELECT question_id FROM quiz_questions WHERE quiz_id = ?")
					.setParameter(1, quizId)
					.getResultList();
				assertEquals(2, joins.size());

				// Assert idempotency: running again does not add duplicate records
				databaseMigrationRunner.run();
				List<?> joinsAfter = entityManager.createNativeQuery(
					"SELECT question_id FROM quiz_questions WHERE quiz_id = ?")
					.setParameter(1, quizId)
					.getResultList();
				assertEquals(2, joinsAfter.size());

				// Assert QuizResult remains intact
				assertNotNull(quizResultService.getResultById(qrId));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	private final java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();

	private String getUrl(String path) {
		return "http://localhost:" + port + path;
	}

	private java.net.http.HttpResponse<String> sendRequest(String method, String path, String token, String payload) throws Exception {
		java.net.http.HttpRequest.Builder builder = java.net.http.HttpRequest.newBuilder()
			.uri(java.net.URI.create(getUrl(path)));

		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}

		if (payload != null) {
			builder.header("Content-Type", "application/json")
				.method(method, java.net.http.HttpRequest.BodyPublishers.ofString(payload));
		} else {
			builder.method(method, java.net.http.HttpRequest.BodyPublishers.noBody());
		}

		return httpClient.send(builder.build(), java.net.http.HttpResponse.BodyHandlers.ofString());
	}

	// 1. Admin can create question
	@Test
	void testAdminCanCreateQuestion() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"HTTP Question\",\"option1\":\"A\",\"option2\":\"B\",\"option3\":\"C\",\"option4\":\"D\",\"correctAnswer\":\"A\"}";

		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/questions", adminToken, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"id\":"));
	}

	// 2. Normal user cannot create question (403 Forbidden)
	@Test
	void testUserCannotCreateQuestion() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"title\":\"HTTP Question\",\"option1\":\"A\",\"option2\":\"B\",\"option3\":\"C\",\"option4\":\"D\",\"correctAnswer\":\"A\"}";

		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/questions", userToken, payload);
		assertEquals(403, response.statusCode());
	}

	// 3. Admin can update question
	@Test
	void testAdminCanUpdateQuestion() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"Updated Title\",\"option1\":\"A\",\"option2\":\"B\",\"option3\":\"C\",\"option4\":\"D\",\"correctAnswer\":\"B\"}";

		java.net.http.HttpResponse<String> response = sendRequest("PUT", "/api/questions/" + q1Id, adminToken, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"Updated Title\""));
	}

	// 4. Normal user cannot update question
	@Test
	void testUserCannotUpdateQuestion() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"title\":\"Updated Title\",\"option1\":\"A\",\"option2\":\"B\",\"option3\":\"C\",\"option4\":\"D\",\"correctAnswer\":\"B\"}";

		java.net.http.HttpResponse<String> response = sendRequest("PUT", "/api/questions/" + q1Id, userToken, payload);
		assertEquals(403, response.statusCode());
	}

	// 5. Admin can delete question
	@Test
	void testAdminCanDeleteQuestion() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");

		java.net.http.HttpResponse<String> response = sendRequest("DELETE", "/api/questions/" + q1Id, adminToken, null);
		assertEquals(200, response.statusCode());
		assertFalse(questionRepository.existsById(q1Id));
	}

	// 6. Admin can create quiz
	@Test
	void testAdminCanCreateQuiz() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"HTTP Quiz\",\"questionIds\":\"" + q1Id + "," + q2Id + "\"}";

		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/quizzes", adminToken, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"id\":"));
	}

	// 7. Quiz rejects non-existent question ID
	@Test
	void testQuizRejectsNonExistentQuestionId() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"HTTP Quiz\",\"questionIds\":\"" + q1Id + ",999999\"}";

		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/quizzes", adminToken, payload);
		assertEquals(400, response.statusCode());
	}

	// 8. Quiz rejects duplicate question IDs
	@Test
	void testQuizRejectsDuplicateQuestionIds() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"HTTP Quiz\",\"questionIds\":\"" + q1Id + "," + q1Id + "\"}";

		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/quizzes", adminToken, payload);
		assertEquals(400, response.statusCode());
	}

	// 9. Admin can update quiz
	@Test
	void testAdminCanUpdateQuiz() throws Exception {
		Quiz quiz = new Quiz(null, "Old Title", q1Id.toString());
		quiz = quizService.createQuiz(quiz);

		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		String payload = "{\"title\":\"HTTP Updated Quiz\",\"questionIds\":\"" + q1Id + "," + q2Id + "\"}";

		java.net.http.HttpResponse<String> response = sendRequest("PUT", "/api/quizzes/" + quiz.getId(), adminToken, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"title\":\"HTTP Updated Quiz\""));
	}

	// 10. Admin can delete quiz
	@Test
	void testAdminCanDeleteQuiz() throws Exception {
		Quiz quiz = new Quiz(null, "Java basics", q1Id.toString());
		quiz = quizService.createQuiz(quiz);

		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");

		java.net.http.HttpResponse<String> response = sendRequest("DELETE", "/api/quizzes/" + quiz.getId(), adminToken, null);
		assertEquals(200, response.statusCode());
		assertNull(quizService.getQuizById(quiz.getId()));
	}

	// 13. Unauthorized API access returns appropriate status (403 or 401)
	@Test
	void testUnauthorizedApiAccess() throws Exception {
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/questions", null, "{}");
		assertTrue(response.statusCode() == 403 || response.statusCode() == 401);
	}

	// 1. Authenticated user can start interview
	@Test
	void testAuthenticatedUserCanStartInterview() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/interviews/start?category=Java", userToken, null);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"status\":\"STARTED\""));
		assertTrue(response.body().contains("\"category\":\"Java\""));
	}

	// 2. Unauthenticated user cannot access interview APIs
	@Test
	void testUnauthenticatedUserCannotAccessInterviews() throws Exception {
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/interviews/start?category=Java", null, null);
		assertTrue(response.statusCode() == 403 || response.statusCode() == 401);
	}

	// 3. User cannot access another user's session
	@Test
	void testUserCannotAccessOtherUserSession() throws Exception {
		com.mdsaifullah.smartinterview.entity.User otherUser = new com.mdsaifullah.smartinterview.entity.User(null, "Other User", "other@example.com", "password", "USER");
		otherUser = userRepository.save(otherUser);

		com.mdsaifullah.smartinterview.entity.InterviewSession session = new com.mdsaifullah.smartinterview.entity.InterviewSession(null, otherUser.getId(), "Java", "STARTED", java.time.LocalDateTime.now());
		session = interviewSessionRepository.save(session);

		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"userAnswer\":\"This is a valid long answer for testing Java HashMap properties.\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/interviews/" + session.getId() + "/submit", userToken, payload);
		
		assertEquals(403, response.statusCode());
	}

	// 4. Empty answer rejected (returns 400)
	@Test
	void testEmptyAnswerRejected() throws Exception {
		com.mdsaifullah.smartinterview.entity.InterviewSession session = new com.mdsaifullah.smartinterview.entity.InterviewSession(null, testUser.getId(), "Java", "STARTED", java.time.LocalDateTime.now());
		session = interviewSessionRepository.save(session);
		com.mdsaifullah.smartinterview.entity.InterviewAnswer ans = new com.mdsaifullah.smartinterview.entity.InterviewAnswer(null, session, "Question?", java.time.LocalDateTime.now());
		interviewAnswerRepository.save(ans);

		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"userAnswer\":\"Short\"}"; // < 10 chars
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/interviews/" + session.getId() + "/submit", userToken, payload);
		
		assertEquals(400, response.statusCode());
	}

	// 5. Excessively long answer rejected
	@Test
	void testOverlyLongAnswerRejected() throws Exception {
		com.mdsaifullah.smartinterview.entity.InterviewSession session = new com.mdsaifullah.smartinterview.entity.InterviewSession(null, testUser.getId(), "Java", "STARTED", java.time.LocalDateTime.now());
		session = interviewSessionRepository.save(session);
		com.mdsaifullah.smartinterview.entity.InterviewAnswer ans = new com.mdsaifullah.smartinterview.entity.InterviewAnswer(null, session, "Question?", java.time.LocalDateTime.now());
		interviewAnswerRepository.save(ans);

		char[] chars = new char[1005];
		java.util.Arrays.fill(chars, 'a');
		String longAnswer = new String(chars);

		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"userAnswer\":\"" + longAnswer + "\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/interviews/" + session.getId() + "/submit", userToken, payload);
		
		assertEquals(400, response.statusCode());
	}

	// 9. Feedback persisted correctly & 10. Interview history works
	@Test
	void testFeedbackPersistedAndHistoryWorks() throws Exception {
		com.mdsaifullah.smartinterview.entity.InterviewSession session = new com.mdsaifullah.smartinterview.entity.InterviewSession(null, testUser.getId(), "Java", "STARTED", java.time.LocalDateTime.now());
		session = interviewSessionRepository.save(session);
		com.mdsaifullah.smartinterview.entity.InterviewAnswer ans = new com.mdsaifullah.smartinterview.entity.InterviewAnswer(null, session, "Question?", java.time.LocalDateTime.now());
		interviewAnswerRepository.save(ans);

		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"userAnswer\":\"This is a valid long answer explaining garbage collection in JVM memory model.\"}";
		
		// Submit answer
		java.net.http.HttpResponse<String> responseSubmit = sendRequest("POST", "/api/interviews/" + session.getId() + "/submit", userToken, payload);
		assertEquals(200, responseSubmit.statusCode());
		
		// Finish interview
		java.net.http.HttpResponse<String> responseFinish = sendRequest("POST", "/api/interviews/" + session.getId() + "/finish", userToken, null);
		assertEquals(200, responseFinish.statusCode());
		
		// Check history
		java.net.http.HttpResponse<String> responseHistory = sendRequest("GET", "/api/interviews/history", userToken, null);
		assertEquals(200, responseHistory.statusCode());
		assertTrue(responseHistory.body().contains("\"category\":\"Java\""));
		assertTrue(responseHistory.body().contains("\"status\":\"COMPLETED\""));
	}

	// 14. ADMIN -> GET /api/users -> 200
	@Test
	void testAdminCanGetUsersList() throws Exception {
		String adminToken = jwtService.generateToken(999L, "admin@example.com", "ADMIN");
		java.net.http.HttpResponse<String> response = sendRequest("GET", "/api/users", adminToken, null);
		assertEquals(200, response.statusCode());
	}

	// 15. USER -> GET /api/users -> 403
	@Test
	void testUserCannotGetUsersList() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		java.net.http.HttpResponse<String> response = sendRequest("GET", "/api/users", userToken, null);
		assertEquals(403, response.statusCode());
	}

	// 16. USER -> GET /api/users/{ownId} -> allowed
	@Test
	void testUserCanGetOwnProfile() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		java.net.http.HttpResponse<String> response = sendRequest("GET", "/api/users/" + testUser.getId(), userToken, null);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains(testUser.getEmail()));
	}

	// 17. USER -> GET /api/users/{otherId} -> forbidden
	@Test
	void testUserCannotGetOtherUserProfile() throws Exception {
		com.mdsaifullah.smartinterview.entity.User otherUser = new com.mdsaifullah.smartinterview.entity.User(null, "Other Guy", "otherguy@example.com", "password", "USER");
		otherUser = userRepository.save(otherUser);

		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		java.net.http.HttpResponse<String> response = sendRequest("GET", "/api/users/" + otherUser.getId(), userToken, null);
		assertTrue(response.statusCode() == 403 || response.statusCode() == 401);
		
		// Clean up
		userRepository.deleteById(otherUser.getId());
	}

	// 18. Valid email registration
	@Test
	void testValidEmailRegistration() throws Exception {
		String payload = "{\"name\":\"New User\",\"email\":\"newuser@example.com\",\"password\":\"strongpass123\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/users/register", null, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"email\":\"newuser@example.com\""));
	}

	// 19. Invalid email registration -> HTTP 400
	@Test
	void testInvalidEmailRegistration() throws Exception {
		String payload = "{\"name\":\"New User\",\"email\":\"invalid-email-format\",\"password\":\"strongpass123\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/users/register", null, payload);
		assertEquals(400, response.statusCode());
	}

	// 20. Password shorter than 8 characters -> HTTP 400
	@Test
	void testPasswordShorterThan8Characters() throws Exception {
		String payload = "{\"name\":\"New User\",\"email\":\"validpwd@example.com\",\"password\":\"short\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/users/register", null, payload);
		assertEquals(400, response.statusCode());
	}

	// 21. Valid 8+ character password -> accepted
	@Test
	void testValid8CharacterPasswordAccepted() throws Exception {
		String payload = "{\"name\":\"New User\",\"email\":\"validpwd@example.com\",\"password\":\"pwd12345\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/users/register", null, payload);
		assertEquals(200, response.statusCode());
	}

	// 22. Existing duplicate email protection still works
	@Test
	void testDuplicateEmailRegistrationFails() throws Exception {
		String payload = "{\"name\":\"New User\",\"email\":\"testuser@example.com\",\"password\":\"strongpass123\"}";
		java.net.http.HttpResponse<String> response = sendRequest("POST", "/api/users/register", null, payload);
		assertEquals(409, response.statusCode());
	}

	// 23. Profile update with valid email
	@Test
	void testProfileUpdateWithValidEmail() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"name\":\"Updated Name\",\"email\":\"updatedemail@example.com\"}";
		java.net.http.HttpResponse<String> response = sendRequest("PUT", "/api/users/" + testUser.getId(), userToken, payload);
		assertEquals(200, response.statusCode());
		assertTrue(response.body().contains("\"email\":\"updatedemail@example.com\""));
	}

	// 24. Profile update with invalid email -> HTTP 400
	@Test
	void testProfileUpdateWithInvalidEmail() throws Exception {
		String userToken = jwtService.generateToken(testUser.getId(), testUser.getEmail(), "USER");
		String payload = "{\"name\":\"Updated Name\",\"email\":\"invalid-format-update\"}";
		java.net.http.HttpResponse<String> response = sendRequest("PUT", "/api/users/" + testUser.getId(), userToken, payload);
		assertEquals(400, response.statusCode());
	}
}
