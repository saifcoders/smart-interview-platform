# Viva / Technical Interview Preparation Guide

This document contains concise, highly structured technical explanations of the platform's architectural, security, and integration design decisions for viva examiners and interviewers.

---

### Q1: What problem does this project solve?
Traditional practice platforms offer static questions or non-interactive text inputs that lack personalized review. This platform bridges this gap by combining **authoritative, server-side graded quizzes** for theoretical checks with a **turn-by-turn AI Mock Interview agent** that simulates a live developer round and delivers detailed grading and gap analysis.

### Q2: Why Spring Boot?
Spring Boot provides a robust, production-grade ecosystem out-of-the-box. It simplifies configuration through sensible defaults, integrates seamlessly with Hibernate/JPA, provides declarative transaction management, and features a robust security module (Spring Security) to protect sensitive user profiles and REST endpoints.

### Q3: Why MySQL?
MySQL is a highly reliable, ACID-compliant relational database. It is ideal for storing structured user accounts, quiz-question mappings, performance histories, and interview transcripts where data integrity, foreign key relations, and ACID transaction guarantees are critical.

### Q4: How does JWT authentication work in this application?
1. The client sends user credentials to `/api/users/login`.
2. The server authenticates the user and generates a stateless, signed JSON Web Token (JWT) using an environment-based HS256 HMAC-SHA key.
3. The token contains claims: `userId`, `email`, `role`, and expiration constraints.
4. The client stores the JWT in `localStorage` and includes it in the `Authorization: Bearer <token>` header of subsequent API calls.
5. `JwtAuthenticationFilter` intercepts requests, validates the signature, parses claims, and configures the `SecurityContext` in Spring.

### Q5: How does role-based authorization work?
Authorized mappings are declared inside `SecurityConfig.java`. Endpoints under `/api/questions` (POST/PUT/DELETE) and `/api/quizzes` (POST/PUT/DELETE) are restricted using `.hasRole("ADMIN")`. The user directory endpoint `GET /api/users` is restricted to admins, while general endpoints (like taking quizzes or mock interviews) are allowed for any authenticated `USER` role.

### Q6: How does password security work?
We use Spring Security's `BCryptPasswordEncoder` to hash user passwords before storing them in the MySQL database. BCrypt uses an iterative hashing algorithm with a configurable work factor (salt) that secures passwords against rainbow tables and brute-force attacks. We also use Jackson annotations (`@JsonProperty(access = Access.WRITE_ONLY)`) on the entity to prevent password hashes from being serialized and returned in API responses.

### Q7: Why are credentials externalized via environment variables?
Hardcoding passwords, JWT secret keys, or AI developer tokens in application properties or git-tracked files poses a major security vulnerability. By externalizing configs (`jwt.secret=${JWT_SECRET:}` and `spring.datasource.password=${DB_PASSWORD:}`), we separate configuration from source code, enabling secure containerized deployments.

### Q8: Why was Spring Open-In-View disabled?
By default, Spring Boot keeps the database connection session open during the entire web request-response lifecycle (Open-In-View enabled). While this makes lazy-loading easy, it holds onto database connections too long, leading to connection pool starvation in production. Disabling it (`spring.jpa.open-in-view=false`) ensures connections are returned to the pool as soon as the transaction finishes, improving scaling performance.

### Q9: How is LazyInitializationException avoided with open-in-view disabled?
We enforce transaction boundaries at the Service layer (`@Transactional`). In service methods returning entities with lazy collections (such as a Quiz containing Questions, or an Interview containing Answers), we force Hibernate to initialize the collections *within the active transaction context* using `.size()`. When the transactional service method returns, the lists are fully populated in memory, allowing Jackson to serialize them to JSON without errors.

### Q10: How does the ManyToMany mapping between Quiz and Question work?
Quizzes and Questions have a classic Many-to-Many association. It is mapped in `Quiz.java` using JPA annotations:
```java
@ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
    name = "quiz_questions",
    joinColumns = @JoinColumn(name = "quiz_id"),
    inverseJoinColumns = @JoinColumn(name = "question_id")
)
private List<Question> questions;
```
This generates a join table `quiz_questions` holding foreign keys referencing primary keys of both tables, avoiding CSV string lists (`"1,2,3"`).

### Q11: How does server-side grading work?
We do not trust the client to compute quiz scores. When a quiz is submitted via `POST /api/results/submit`, the server retrieves the quiz questions from the database, compares user answers with the authoritative correct answers stored securely on the database, and persists the resulting score and total questions count to the `QuizResult` table.

### Q12: How does the Google Gemini integration work?
`AiService` leverages the native Java `java.net.http.HttpClient` to make secure POST handshakes with Google's Generative Language REST APIs targeting the `gemini-2.5-flash` model. 
* Prompts are configured with strict JSON schemas to force Gemini to reply in raw JSON formats.
* The JSON response is parsed into DTO classes (`AiFeedbackResponse`) using `ObjectMapper`.
* Fallback mocks automatically handle situations where the API key is missing or rate limits are hit.

### Q13: How are duplicate email registrations handled?
* Registration queries are normalized (emails are trimmed of whitespace and lowercase-converted).
* The database schema defines `email` as a unique column.
* `UserService` queries `userRepository.findByEmail(email)` during registration. If it is already present, it throws `409 Conflict`.

### Q14: How does unauthorized user profile access check work?
In `UserController.java`, when a request to `GET /api/users/{id}` is processed:
1. The server checks the authenticated principal's context.
2. It asserts that the request is made by an user with role `ADMIN` OR that the authenticated user's ID matches the path variable `{id}`.
3. If they mismatch, it immediately throws `403 Forbidden`.

### Q15: What testing strategy was used?
We designed integration tests using `@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)`. Using an HTTP Client, tests verify full request-response lifecycles, ensuring token authorization rules, input validation, role checks, and database updates function correctly in isolation.
