# Demo Checklist

A step-by-step walkthrough to demonstrate the Smart Interview Preparation Platform's features during final project presentation or evaluation.

---

## 1. Startup & Environment Preparation
1. **Start MySQL database**: Ensure the local MySQL daemon is running.
2. **Set Environment Variables**:
   ```powershell
   $env:JWT_SECRET="demo_signing_secret_key_long_enough_256_bits_value"
   $env:DB_USER="root"
   $env:DB_PASSWORD="your_db_password"
   $env:GEMINI_API_KEY="your_gemini_api_key"
   ```
3. **Launch the JAR**:
   ```bash
   java -jar target/smart-interview-platform-0.0.1-SNAPSHOT.jar
   ```
4. **Access the Application**: Open a web browser and navigate to `http://localhost:8080`.

---

## 2. User Registration, Validation & Authentication Flow
5. **Open Registration Page**: Navigate to the registration route.
6. **Trigger Email Validation Error**:
   * Attempt to register with:
     * Name: `Student`
     * Email: `invalid-email-format`
     * Password: `password123`
   * Click **Submit** $\rightarrow$ Assert `HTTP 400 Bad Request` or frontend warning showing "Invalid email format".
7. **Trigger Password Length Validation Error**:
   * Attempt to register with:
     * Name: `Student`
     * Email: `valid@example.com`
     * Password: `short` (less than 8 characters)
   * Click **Submit** $\rightarrow$ Assert `HTTP 400 Bad Request` showing "Password must be at least 8 characters long".
8. **Register Valid User**:
   * Register with:
     * Name: `John Doe`
     * Email: `john@example.com`
     * Password: `securepassword123`
   * Click **Submit** $\rightarrow$ Assert successful registration (account created, redirected to login).
9. **Verify Email Normalization**:
   * Try registering again using: `JOHN@example.com` with a password.
   * Click **Submit** $\rightarrow$ Assert `HTTP 409 Conflict` (Email is already registered), confirming case-insensitive duplicate protection.
10. **Login**:
    * Authenticate using the credentials: `john@example.com` / `securepassword123`.
    * Assert landing on user dashboard.
    * Verify JWT token is generated and stored in client storage (`localStorage`).

---

## 3. Practice Quiz Flow (Student Profile)
11. **Browse Dashboard**: Observe stats (Quizzes attempted: 0, Average score: N/A, Mock interviews completed: 0).
12. **Select & Start Quiz**:
    * Click on an available quiz (e.g. Java basics).
    * Observe question navigation paging, options selection, and timer.
13. **Complete & Submit Quiz**:
    * Answer questions.
    * Click **Submit Quiz**.
14. **View Authoritative Grading**:
    * Assert redirection to the Quiz Result page.
    * View exact score count and percentage.
    * *Safety Check*: Confirm correct answers were not exposed in browser network response payloads before submission.
15. **Browse Performance History**:
    * Return to Dashboard.
    * Observe stats updated (Attempted: 1, Score calculated).
    * Observe result history table listing the attempted quiz details.

---

## 4. AI Mock Interview Practice Flow
16. **Launch Mock Interview**:
    * Go to **AI Practice** panel from navigation.
    * Select category: **Java** or **Behavioral**.
    * Click **Start Interview**.
17. **Answer Turn-by-Turn Questions**:
    * Read the first question prompted by the AI.
    * Type an answer of less than 10 characters $\rightarrow$ Click **Submit** $\rightarrow$ Observe error: "Answer is too short. Please write at least 10 characters."
    * Type a valid, complete answer (e.g. explaining JVM Memory Model, Garbage Collection, or OOP concepts).
    * Click **Submit Answer**.
    * Read the structured real-time AI feedback showing (AI Score, Strengths, Weaknesses, Missing Points).
18. **Continue to Completion**:
    * Click **Next Question** to proceed.
    * After answering all questions, click **Finish Interview**.
19. **Review Evaluation Report**:
    * Read the finalized overall performance feedback statement, overall score, and chronological chat transcript report.

---

## 5. Administration Control Flow
20. **Login as Admin**:
    * Log out from the student profile.
    * Register/Login as an admin (User with email containing `admin` or designated role mapping, or modify DB role directly to `ADMIN`).
21. **Access Admin Panel**:
    * Access `/admin.html` path.
    * Observe stats showing system-wide statistics (Total Users, Total Quizzes, Total Questions).
22. **Manage Questions**:
    * Click **Questions**.
    * Create a new question $\rightarrow$ Assert it appears in list.
    * Update the question $\rightarrow$ Verify update.
    * Delete the question $\rightarrow$ Assert safe disassociation and database deletion.
23. **Manage Quizzes**:
    * Click **Quizzes**.
    * Create a new quiz using the checklist selection grid to link questions.
    * Save $\rightarrow$ Verify quiz is created with correct Many-to-Many join relationships.
24. **Demonstrate Route Protection Rules**:
    * Log out from Admin and log in as normal student (`john@example.com`).
    * Attempt to browse to `http://localhost:8080/admin.html` $\rightarrow$ Assert redirect to login or blocked access.
    * Attempt to make a `GET` request to `/api/users` via Postman or browser console $\rightarrow$ Assert HTTP `403 Forbidden` response.
