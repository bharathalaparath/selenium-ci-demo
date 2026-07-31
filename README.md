# Selenium CI/CD Demo — BDD Automation Framework

A production-style test automation framework built to demonstrate end-to-end CI/CD practices for QA — from local test execution to dual-pipeline automation (GitHub Actions + Jenkins), BDD-based test design, tagged test grouping, and rich reporting with Allure.

This project was built incrementally as a hands-on learning exercise and reflects real debugging and configuration challenges encountered along the way — not just a working end-state.

---

## Tech Stack

| Layer | Tool |
|---|---|
| Language | Java 11+ |
| Build Tool | Maven |
| UI Automation | Selenium WebDriver 4 |
| Test Framework | TestNG |
| BDD | Cucumber (cucumber-java, cucumber-testng) |
| Driver Management | WebDriverManager (auto ChromeDriver setup) |
| Reporting | Allure Reports |
| CI (Cloud) | GitHub Actions |
| CI (Enterprise-style) | Jenkins (local) |

---

## What This Project Demonstrates

- A real Selenium test automating login on [the-internet.herokuapp.com](https://the-internet.herokuapp.com/login)
- Headless + Incognito Chrome execution, CI-safe (`--no-sandbox`, `--disable-dev-shm-usage`)
- BDD test design using Gherkin feature files and Cucumber step definitions
- Tagged test grouping — `@smoke`, `@regression`, `@edge-case` — run independently
- Two parallel CI pipelines: GitHub Actions (cloud) and Jenkins (self-hosted)
- Fail-fast pipeline design — regression only runs if smoke passes
- Allure Reports integrated locally, in GitHub Actions, and in Jenkins
- Pipeline-as-code for both CI tools (`test-pipeline.yml` and `Jenkinsfile`)

---

## Project Structure

```
selenium-ci-demo/
├── .github/
│   └── workflows/
│       └── test-pipeline.yml        # GitHub Actions — Smoke + Regression jobs
├── src/
│   └── test/
│       ├── java/
│       │   ├── stepdefinitions/
│       │   │   └── LoginSteps.java          # Cucumber step definitions
│       │   └── runners/
│       │       ├── SmokeTestRunner.java     # Runs @smoke tagged scenarios
│       │       └── RegressionTestRunner.java# Runs @regression + @edge-case
│       └── resources/
│           ├── features/
│           │   └── login.feature            # Gherkin BDD scenarios
│           └── allure.properties            # Allure results directory config
├── Jenkinsfile                       # Jenkins declarative pipeline
├── pom.xml                           # Maven dependencies & plugins
└── README.md

```

## How Tests Are Organized

Scenarios are tagged in `login.feature` and picked up by dedicated runner classes:

| Tag | Runner | Purpose |
|---|---|---|
| `@smoke` | `SmokeTestRunner` | Fast, critical-path checks — run on every push/PR |
| `@regression` | `RegressionTestRunner` | Full functional coverage |
| `@edge-case` | `RegressionTestRunner` | Boundary and invalid-input scenarios |

This mirrors the **Fail Fast** principle — smoke tests must pass before regression runs, saving CI time on obviously broken builds.

---

## Running Tests Locally

**Run everything:**
```bash
mvn clean test
```

**Run only smoke tests:**
```bash
mvn clean test -Dtest=SmokeTestRunner
```

**Run only regression + edge cases:**
```bash
mvn clean test -Dtest=RegressionTestRunner
```

**Generate and view the Allure report:**
```bash
mvn allure:serve
```

---

## CI/CD Pipelines

### GitHub Actions
Triggered on every push/PR to `main`. Runs as two sequential jobs:
```
smoke-tests → regression-tests (only if smoke passes)
```
Both jobs generate and upload an Allure report as a downloadable artifact.

### Jenkins
A local Jenkins instance runs the same logic via a declarative `Jenkinsfile`:
```
Checkout → Smoke Tests → Regression Tests → Generate Allure Report → Publish Results
```
Results are published using the JUnit and Allure Jenkins plugins.

---

## Reporting

Every test run produces an **Allure Report** with:
- Pass/fail overview and trends
- Tests grouped by `@Epic` / `@Story` (e.g. Login Module → Valid Login)
- Severity tagging (`@Severity(CRITICAL)`)
- Step-by-step execution logs (`Allure.step(...)`)

---

## Why This Project Exists

Built as hands-on preparation for Senior SDET role, specifically to close two commonly identified gaps:
1. Real, working CI/CD pipeline experience (not just theoretical knowledge)
2. BDD framework design with proper test categorization for enterprise-scale suites

For the full step-by-step build log — including every issue hit and how it was resolved — see `PROJECT_JOURNEY.md`.