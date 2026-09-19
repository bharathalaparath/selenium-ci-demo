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
| Driver Management | WebDriverManager — config-driven Chrome/Firefox/Edge via `DriverFactory` |
| Reporting | Allure Reports + ExtentReports (with failure screenshots embedded) |
| Logging | SLF4J + Logback |
| CI (Cloud) | GitHub Actions |
| CI (Enterprise-style) | Jenkins (local) |

---

## What This Project Demonstrates

- A real Selenium test automating login on [the-internet.herokuapp.com](https://the-internet.herokuapp.com/login)
- Page Object Model (`BasePage` / `LoginPage`) — step definitions call page methods, never raw locators
- `DriverFactory` — config-driven browser selection (Chrome/Firefox/Edge) behind a `ThreadLocal<WebDriver>`, so the suite is safe to parallelize later
- Headless + Incognito Chrome execution, CI-safe (`--no-sandbox`, `--disable-dev-shm-usage`)
- BDD test design using Gherkin feature files and Cucumber step definitions
- Tagged test grouping — `@smoke`, `@regression`, `@edge-case` — run independently
- Two parallel CI pipelines: GitHub Actions (cloud) and Jenkins (self-hosted)
- Fail-fast pipeline design — regression only runs if smoke passes
- Allure + ExtentReports integrated locally, in GitHub Actions, and in Jenkins, with screenshots auto-attached on failure
- Automatic retry for flaky UI tests via a TestNG `IRetryAnalyzer`, wired in through an `IAnnotationTransformer`
- Pipeline-as-code for both CI tools (`test-pipeline.yaml` and `Jenkinsfile`)

---

## Project Structure

```
selenium-ci-demo/
├── .github/
│   └── workflows/
│       └── test-pipeline.yaml        # GitHub Actions — Smoke + Regression jobs
├── src/
│   └── test/
│       ├── java/
│       │   ├── pages/
│       │   │   ├── BasePage.java             # Shared wait helpers for all page objects
│       │   │   └── LoginPage.java            # Login page locators + actions
│       │   ├── factory/
│       │   │   └── DriverFactory.java        # Config-driven WebDriver creation (ThreadLocal)
│       │   ├── stepdefinitions/
│       │   │   ├── Hooks.java                # Cucumber @Before/@After — driver lifecycle
│       │   │   └── LoginSteps.java           # Cucumber step definitions
│       │   ├── listeners/
│       │   │   ├── ExtentTestNGListener.java # Builds the ExtentReports report
│       │   │   ├── RetryAnalyzer.java        # Retries a failed scenario up to 2x
│       │   │   └── AnnotationTransformer.java# Wires RetryAnalyzer onto every scenario
│       │   ├── utils/
│       │   │   ├── ConfigReader.java         # Reads config.properties (-D overrides supported)
│       │   │   ├── WaitUtils.java            # Centralized WebDriverWait construction
│       │   │   ├── ScreenshotUtils.java      # Base64 screenshot capture
│       │   │   └── ExtentManager.java        # Per-runner ExtentReports instances
│       │   └── runners/
│       │       ├── SmokeTestRunner.java     # Runs @smoke tagged scenarios
│       │       └── RegressionTestRunner.java# Runs @regression + @edge-case
│       └── resources/
│           ├── features/
│           │   └── login.feature            # Gherkin BDD scenarios
│           ├── META-INF/services/
│           │   └── org.testng.ITestNGListener # SPI registration for AnnotationTransformer
│           ├── config.properties             # browser / base.url / wait timeout
│           ├── logback.xml                   # Console logging format
│           └── allure.properties             # Allure results directory config
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

**Run against a different browser (no file changes needed):**
```bash
mvn clean test -Dtest=SmokeTestRunner -Dbrowser=firefox
```

**Generate and view the Allure report:**
```bash
mvn allure:serve
```

**View the ExtentReports report:**
```
target/extent-reports/SmokeTestRunner.html
target/extent-reports/RegressionTestRunner.html
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

Every test run produces both an **Allure Report** and an **ExtentReports** report:
- Pass/fail overview and trends
- Tests grouped by `@Epic` / `@Story` (e.g. Login Module → Valid Login)
- Severity tagging (`@Severity(CRITICAL)`)
- Step-by-step execution logs (`Allure.step(...)`)
- Screenshots automatically attached to both reports on any scenario failure
- Smoke and Regression each get their own separate ExtentReports HTML file — they never overwrite each other

---

## Why This Project Exists

Built as hands-on preparation for Senior SDET role, specifically to close two commonly identified gaps:
1. Real, working CI/CD pipeline experience (not just theoretical knowledge)
2. BDD framework design with proper test categorization for enterprise-scale suites

For the full step-by-step build log — including every issue hit and how it was resolved — see `PROJECT_JOURNEY.md`.