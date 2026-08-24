# Resume & LinkedIn Project Description

This document provides standardized copy-paste formats for adding the Smart Interview Preparation Platform to your resume, LinkedIn profile, or portfolio site.

---

## 1. Resume Bullet Points (Copy & Paste)

* **Smart Interview Preparation Platform | Spring Boot, JPA, MySQL, Thymeleaf, Google Gemini API**
  * Architected and implemented a secure technical practice web application with Spring Boot, JPA, and MySQL, achieving a 100% success rate across 40 integration tests.
  * Designed stateless JWT authentication and role-based access control (RBAC), securing REST endpoints and restricting administrative operations to authorized roles.
  * Refactored legacy CSV data mappings into a proper JPA `@ManyToMany` database join table schema, ensuring data consistency and query integrity.
  * Disabled Spring Open-In-View (OSIV) to prevent database connection pool starvation, utilizing `@Transactional` service boundaries and manual fetch initializations to resolve lazy loading.
  * Integrated Google Gemini LLM API (`gemini-2.5-flash`) with strict JSON schema outputs to construct a turn-based AI Mock Interview panel, delivering real-time candidate evaluations, gap analyses, and scores.

---

## 2. Short LinkedIn / Portfolio Description (Copy & Paste)

🚀 **Smart Interview Preparation Platform** is a web platform designed to help technical candidates prepare for coding and system design interviews. 

Built using **Spring Boot**, **Spring Security**, and **MySQL**, the application provides structured technical quizzes alongside an **AI Mock Interview practice module** integrated with **Google Gemini**. Candidates receive turn-by-turn evaluation feedback, scoring diagnostics, strengths/weaknesses breakdowns, and optimal reference answers to speed up learning.

**Technical Highlights:**
* Secure, stateless JWT token authentication & role checks (Admin vs Student).
* Relational schema mapping (JPA Many-to-Many join tables).
* Production-ready database connection pool tuning (Disabled Open-in-View to protect connections pool).
* Direct integration with LLM APIs using Java HTTP Client & Jackson ObjectMapper.
* Clean, responsive frontend styled with Thymeleaf and Tailwind CSS.
* Full integration test coverage (40 tests).

---

## 3. Technical Skills Demonstrated
* **Backend Development**: Java (21), Spring Boot, Spring Security (JWT), Thymeleaf, Jackson
* **Database Management**: MySQL 8.0, Hibernate, Spring Data JPA, Relational Schema Normalization
* **AI & Integration**: RESTful API Integration, Prompt Engineering, Google Gemini API
* **Security & Hardening**: BCrypt Password Hashing, Environment Configuration Management, Route Protection
* **Testing & Tools**: JUnit 5, Integration Testing, Maven, Git
