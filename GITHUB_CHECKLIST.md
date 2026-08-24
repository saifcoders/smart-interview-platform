# GitHub Repository Checklist

A checklist of best practices and repository guidelines before committing the code to public or private source control platforms (GitHub, GitLab, Bitbucket).

---

## 1. Project Naming & Settings
- [ ] **Repository Name**: Recommended `smart-interview-preparation-platform` or `ai-interview-prep`.
- [ ] **Description**: "AI-Powered Technical Practice & Mock Interview Web Platform built on Spring Boot, MySQL, and Thymeleaf/Tailwind."
- [ ] **Visibility**: Private during grading/evaluations, Public for portfolio demonstration.
- [ ] **Topics/Tags**: `java`, `spring-boot`, `spring-security`, `jwt`, `mysql`, `gemini-api`, `llm-integration`, `mock-interview`, `tailwind-css`, `thymeleaf`.

---

## 2. Pre-Commit Audits & Git Exclusions
- [ ] **Target Directory**: Ensure `target/` is completely ignored (check that `.\mvnw.cmd clean` has been executed).
- [ ] **IDE Metadata**: Ensure IntelliJ Idea `.idea/`, Eclipse `.project`/`.classpath`, and VS Code `.vscode/` files are excluded.
- [ ] **Secret Files Check**: Check that no `.env` or local configuration properties exist in the tracking index.
- [ ] **Command to check tracked files**:
  ```bash
  git status
  ```
  Ensure only `src/`, `pom.xml`, `mvnw`, `mvnw.cmd`, `.mvn/`, `.gitignore`, and documentation `.md` files are listed under untracked files.

---

## 3. Recommended Initial Commit Strategy
1. **Initialize Git**:
   ```bash
   git init -b main
   ```
2. **Stage files**:
   ```bash
   git add .
   ```
3. **Commit changes**:
   ```bash
   git commit -m "Initial release: Smart Interview Prep Platform with Spring Security, Many-to-Many Quiz relations, and Google Gemini AI mock interview integration"
   ```

---

## 4. Branching & Deployment Best Practices
* **Branch Policy**: Keep `main` locked for production-ready releases (where `mvnw clean package` passes 100%). Create feature branches (e.g. `feat/ai-resumes`) for enhancements.
* **CI/CD Integration**: Connect GitHub Actions to run the Maven test suite on every pull request to protect `main` from regression breaks.
