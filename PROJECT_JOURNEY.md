# Project Journey — Selenium CI/CD Demo

A complete build log of how this project was created, step by step — including every issue encountered and exactly how it was fixed. This is written as a reference document for revisiting concepts before interviews.

**Format used throughout:**
- 🔧 **Step** — what was done
- ⚠️ **Issue** — what went wrong
- ✅ **Solution** — how it was fixed and why

---

## Table of Contents

1. [Phase 1 — Project Setup](#phase-1--project-setup)
2. [Phase 2 — Local Test Debugging](#phase-2--local-test-debugging)
3. [Phase 3 — GitHub Actions CI](#phase-3--github-actions-ci)
4. [Phase 4 — Jenkins CI](#phase-4--jenkins-ci)
5. [Phase 5 — Allure Reporting](#phase-5--allure-reporting)
6. [Phase 6 — Shift-Left Testing (Concepts)](#phase-6--shift-left-testing-concepts)
7. [Phase 7 — BDD with Cucumber + Test Grouping](#phase-7--bdd-with-cucumber--test-grouping)
8. [Final Project Structure](#final-project-structure)
9. [Command Reference Sheet](#command-reference-sheet)

---

## Phase 1 — Project Setup

### 🔧 Step 1: Create Folder Structure

```bash
mkdir selenium-ci-demo
cd selenium-ci-demo
mkdir -p src/test/java/tests
```

**Why this structure:** Maven expects test code under `src/test/java` by convention — this is how `mvn test` knows where to look without extra configuration.

---

### 🔧 Step 2: Create `pom.xml`

The Project Object Model file — defines dependencies (Selenium, TestNG, WebDriverManager) and build plugins (Surefire, for running tests).

Key dependency choices and why:

| Dependency | Purpose |
|---|---|
| `selenium-java` | Core browser automation library |
| `testng` | Test runner — annotations like `@Test`, `@BeforeMethod` |
| `webdrivermanager` | Auto-downloads the correct ChromeDriver version — no manual driver management |

---

### 🔧 Step 3: Write the First Test

Initial test (`GoogleSearchTest.java`) automated a Google search using `driver.findElement(By.name("q"))`.

### 🔧 Step 4: Verify Locally

```bash
mvn clean test
```

This phase ended with the project compiling and Maven recognizing the test — but the test itself was not yet passing correctly (see Phase 2).

---

## Phase 2 — Local Test Debugging

This phase involved real trial-and-error to get a stable, CI-safe Selenium test.

---

### ⚠️ Issue 1: `ElementNotInteractable` Error

```
[ERROR] GoogleSearchTest.searchGoogle:33 » ElementNotInteractable element not interactable
```

**Root cause:** The test tried to interact with the search box immediately after `driver.get()`, before the page had fully rendered the element.

### ✅ Solution

Replaced the immediate `findElement` call with an **Explicit Wait**:

```java
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement searchBox = wait.until(
    ExpectedConditions.elementToBeClickable(By.name("q"))
);
```

**Why this is the correct fix (and not `Thread.sleep()`):** Explicit waits poll the DOM repeatedly until the condition is true or timeout is hit — faster and more reliable than a fixed sleep, and this is the answer interviewers expect.

---

### ⚠️ Issue 2: Same Error Persisted — "Browser runs faster than expected"

**Root cause:** Newer Chrome versions changed how `--headless` (the old flag) behaves — it no longer renders pages the same way, causing elements to be technically "present" but not interactable.

### ✅ Solution

Switched to the modern headless flag and added explicit window sizing (headless mode has no real viewport by default):

```java
options.addArguments("--headless=new");
options.addArguments("--window-size=1920,1080");
```

Also updated the locator, since Google had changed its search box from `<input>` to `<textarea>`:

```java
By.tagName("textarea")
```

---

### ⚠️ Issue 3: Incognito Requested — Browser Window Shows Black Screen and Closes

**Root cause:** `--headless=new` combined with certain flags was silently failing in a way that wasn't obvious from the error alone.

### ✅ Solution — Debug by Removing Headless Temporarily

Temporarily commented out `--headless=new` to run the browser visibly and confirm whether the issue was headless-specific or in the test logic itself:

```java
// options.addArguments("--headless=new");   // commented out to debug
```

This revealed the real root cause (see Issue 4 below).

---

### ⚠️ Issue 4: Google CAPTCHA / Bot Detection Page

Running the browser visibly showed Google's **"Our systems have detected unusual traffic"** reCAPTCHA page — Google was actively blocking the automated browser.

### ✅ Solution — Switch Test Target Entirely

Google is not a suitable target for Selenium practice or CI — it actively blocks bots. Switched to **[the-internet.herokuapp.com](https://the-internet.herokuapp.com)**, a site purpose-built for Selenium practice with stable, predictable locators.

New test: automate the `/login` page using a known valid username/password (`tomsmith` / `SuperSecretPassword!`) and assert the success message.

**Result:** Test passed locally, both with a visible browser and in headless mode.

---

### Key Lesson from Phase 2

> Not every website is automatable, and that's not a Selenium problem — it's a target selection problem. Sites with bot detection (Google, most production e-commerce sites) will actively fight automated browsers. Practice and CI demos should always use automation-friendly targets.

---

## Phase 3 — GitHub Actions CI

### 🔧 Step 1: Push Project to GitHub

```bash
git init
git add .
git commit -m "Initial Selenium CI demo project"
git remote add origin https://github.com/YOUR_USERNAME/selenium-ci-demo.git
git push -u origin main
```

---

### 🔧 Step 2: Create the Workflow File

```bash
mkdir -p .github/workflows
```

Created `.github/workflows/test-pipeline.yml` — a YAML pipeline that:
- Triggers on push/PR to `main`
- Checks out code, installs JDK 11 and Chrome
- Runs `mvn clean test`
- Uploads test results as an artifact

---

### ⚠️ Issue 1: Deprecated Action Version

```
Error: This request has been automatically failed because it uses a deprecated 
version of `actions/upload-artifact: v3`.
```

### ✅ Solution

```yaml
uses: actions/upload-artifact@v4    # v3 → v4
```

**Lesson:** GitHub Actions deprecates old action versions on a schedule. Pipelines need periodic maintenance even if the logic never changes.

---

### ⚠️ Issue 2: Node.js 20 Deprecation Warning

```
Node.js 20 is deprecated. The following actions target Node.js 20 but are 
being forced to run on Node.js 24: actions/checkout@v3, actions/setup-java@v3
```

### ✅ Solution

Bumped both actions to their latest major versions:

```yaml
uses: actions/checkout@v4
uses: actions/setup-java@v4
```

**Note:** This was a warning, not a failure — the pipeline was still green. Fixed proactively to avoid future breakage once GitHub completes its Node24 migration (planned for fall 2026).

---

### Result

A fully working GitHub Actions pipeline, triggering automatically on every push, running Selenium tests in headless Chrome on GitHub's Linux runners.

---

## Phase 4 — Jenkins CI

### 🔧 Step 1: Install and Run Jenkins Locally

```bash
java -jar jenkins.war --httpPort=8080
```

Completed first-time setup: unlocked with `initialAdminPassword`, installed suggested plugins, created admin user.

---

### ⚠️ Issue 1: Jenkins Stops When Terminal Closes

**Root cause:** Jenkins runs as a foreground Java process — closing the terminal kills it.

### ✅ Solution

Keep the terminal open for the session, or run it as a background process:

```bash
start /B java -jar jenkins.war --httpPort=8080
```

---

### 🔧 Step 2: Add `Jenkinsfile` and Configure Tools

Created a declarative `Jenkinsfile` with stages for Checkout, Build, Test, and Publish Results.

Configured **Manage Jenkins → Tools** with local JDK and Maven paths:

```
JAVA_HOME  = C:\Program Files\Java\jdk-25.0.3
MAVEN_HOME = C:\Program Files\apache-maven-3.9.14
```

---

### 🔧 Step 3: Create Pipeline Job

Job configured with:
- Definition: `Pipeline script from SCM`
- SCM: Git, pointing to the GitHub repo
- Script Path: `Jenkinsfile`

---

### ⚠️ Issue 1: Branch Not Found

```
fatal: couldn't find remote ref refs/heads/master
```

**Root cause:** Jenkins defaulted to looking for a `master` branch; the repo used `main`.

### ✅ Solution

```
Branch Specifier: */master → */main
```

---

### ⚠️ Issue 2: `sh` Command Not Recognized on Windows

```
'cmd' is not recognized as an internal or external command
script returned exit code 9009
```

**Root cause:** The initial `Jenkinsfile` used `sh 'mvn test'` — the Linux shell step. Jenkins was running on a Windows machine, which needs `bat` instead.

### ✅ Solution

```groovy
bat 'mvn test'    // sh → bat for Windows
```

---

### ⚠️ Issue 3: Maven Still Not Found After Switching to `bat`

**Root cause:** Jenkins' process didn't inherit the same system PATH as the terminal, so plain `mvn` wasn't resolvable even inside a Windows batch step.

### ✅ Solution — Explicit `environment` Block and Full Paths

```groovy
environment {
    MAVEN_HOME = 'C:\\Program Files\\apache-maven-3.9.14'
    JAVA_HOME  = 'C:\\Program Files\\Java\\jdk-25.0.3'
    PATH       = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;C:\\Windows\\System32"
}
...
bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" clean compile'
```

**Lesson:** Jenkins agents don't always inherit a user's shell environment. Explicit `environment` blocks and full binary paths are a common enterprise Jenkins pattern, especially on Windows agents.

---

### Result

A fully working local Jenkins pipeline with visible Console Output and stage-by-stage execution — mirroring how enterprise Jenkins setups (like at Deloitte GCCs) are typically configured.

---

## Phase 5 — Allure Reporting

### 🔧 Step 1: Add Allure to `pom.xml`

Added:
- `allure-testng` dependency — collects test result data
- `aspectjweaver` dependency — required by Allure to intercept test execution
- Updated **Surefire plugin** with the AspectJ Java agent:

```xml
<argLine>
    -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/1.9.21/aspectjweaver-1.9.21.jar"
</argLine>
```

- Added the **Allure Maven plugin** to generate the HTML report from raw results.

---

### ⚠️ Issue 1: Allure Results Written to Project Root Instead of `target/`

Running `mvn clean test` created an `allure-results/` folder at the project root instead of inside `target/`.

### ✅ Solution

Created `src/test/resources/allure.properties`:

```properties
allure.results.directory=target/allure-results
```

**Why this matters:** Keeping generated artifacts under `target/` (which is gitignored and cleaned by `mvn clean`) keeps the repo clean — raw result files shouldn't be committed to version control.

---

### 🔧 Step 2: Generate and View Report Locally

```bash
mvn allure:serve
```

This generates the HTML report and opens it in the browser automatically.

---

### 🔧 Step 3: Add Meaningful Annotations

Added Allure annotations to the test method to make the report business-readable:

```java
@Epic("Login Module")
@Story("Valid Login")
@Description("Verify that a valid user can log in successfully")
@Severity(SeverityLevel.CRITICAL)
```

| Annotation | Effect on Report |
|---|---|
| `@Epic` | Top-level grouping (e.g. module) |
| `@Story` | Grouping by user story/feature |
| `@Description` | Human-readable test purpose |
| `@Severity` | Priority tagging — CRITICAL/NORMAL/MINOR |

---

### 🔧 Step 4: Publish Report in GitHub Actions

Added two new steps to `test-pipeline.yml`:

```yaml
- name: Generate Allure Report
  if: always()
  run: mvn allure:report

- name: Upload Allure Report
  if: always()
  uses: actions/upload-artifact@v4
  with:
    name: allure-report
    path: target/site/allure-maven-plugin/
```

`if: always()` ensures the report generates and uploads even if tests fail — critical for actually debugging failures.

---

### ⚠️ Issue 1: Git Push Rejected — Non-Fast-Forward

```
! [rejected]        main -> main (non-fast-forward)
error: failed to push some refs
```

**Root cause:** The remote GitHub repo had a commit (a README edit made directly on GitHub) that didn't exist locally yet. Git refuses to push when histories have diverged, to avoid silently overwriting remote work.

### ✅ Solution

```bash
git pull origin main --allow-unrelated-histories
git push origin main
```

---

### ⚠️ Issue 2: Stuck in Vim During Merge Commit

The pull opened a Vim editor screen asking for a merge commit message, with no visible way to proceed.

### ✅ Solution

```
Esc      → exit insert/edit mode
:wq      → write and quit (accept default message)
Enter    → confirm
```

**Reference table for future Git lockups:**

| Situation | Command |
|---|---|
| Check current Git state | `git status` |
| Abort a stuck merge | `git merge --abort` |
| Abort a stuck rebase | `git rebase --abort` |
| Abort a stuck cherry-pick | `git cherry-pick --abort` |

---

### 🔧 Step 5: Publish Report in Jenkins

Installed the **Allure Jenkins Plugin** via *Manage Jenkins → Plugins*.

---

### ⚠️ Issue 1: No Allure CLI Installation Found

```
No Allure CLI installation found.
Please configure Allure CLI in Jenkins:
Manage Jenkins - Tools - Allure Commandline
```

### ✅ Solution

Configured under **Manage Jenkins → Tools → Allure Commandline**:
- Name: `allure`
- Install automatically: ✅ checked
- Version: latest available

Updated `Jenkinsfile` to add report generation and publishing stages using the `allure(...)` pipeline step.

---

### Result

Allure Reports now generate and publish automatically in **three places**: locally (`mvn allure:serve`), GitHub Actions (as a downloadable artifact), and Jenkins (as a linked report on the build page).

---

## Phase 6 — Shift-Left Testing (Concepts)

This phase was conceptual — no code changes — focused on QA Lead-level strategic thinking.

### Core Idea

Move QA involvement earlier in the SDLC (to the "left" of the timeline) — requirements review, test design during planning, not just execution after development finishes.

### Key Concepts Covered

- **Cost of Defects** — a bug caught at requirements stage costs roughly 100x less to fix than one caught in production.
- **BDD as a Shift-Left practice** — writing Gherkin scenarios during design, before development starts, so they double as acceptance criteria and test cases.
- **The Testing Pyramid** — unit tests (most, cheapest) → API tests (moderate) → UI tests (fewest, most expensive/flaky). QA Leads push automation investment toward the base.
- **Quality gates in CI** — e.g. code coverage thresholds, static analysis (SonarQube) blocking a PR before it merges.

This phase directly informed the design decisions in Phase 7 (BDD + tagged test grouping) — this project itself is a practical application of Shift-Left principles.

---

## Phase 7 — BDD with Cucumber + Test Grouping

### 🔧 Step 1: Add Cucumber Dependencies to `pom.xml`

```xml
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-java</artifactId>
    <version>7.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.cucumber</groupId>
    <artifactId>cucumber-testng</artifactId>
    <version>7.15.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>io.qameta.allure</groupId>
    <artifactId>allure-cucumber7-jvm</artifactId>
    <version>2.25.0</version>
    <scope>test</scope>
</dependency>
```

Updated the Surefire plugin to only pick up test **runner** classes:

```xml
<includes>
    <include>**/runners/*.java</include>
</includes>
```

**Why:** Cucumber doesn't run step definition classes directly — it runs "runner" classes that point Cucumber at feature files and step definitions. Without this include rule, Maven would try (and fail) to run step definition classes as if they were standalone tests.

---

### 🔧 Step 2: Write the Feature File

`src/test/resources/features/login.feature` — Gherkin scenarios tagged by test type:

```gherkin
Feature: Login Module

  @smoke
  Scenario: Valid login with correct credentials
    Given I am on the login page
    When I enter username "tomsmith" and password "SuperSecretPassword!"
    And I click the login button
    Then I should see the message "You logged into a secure area!"

  @regression
  Scenario: Invalid login with wrong password
    ...

  @edge-case
  Scenario: Login with empty credentials
    ...
```

**Why tags matter:** Tags (`@smoke`, `@regression`, `@edge-case`) let different runner classes execute different subsets of scenarios without duplicating any test logic.

---

### 🔧 Step 3: Write Step Definitions

`src/test/java/stepdefinitions/LoginSteps.java` maps each Gherkin line to actual Selenium code, using Cucumber's `@Given`, `@When`, `@And`, `@Then` annotations, plus `@Before`/`@After` for setup/teardown (equivalent to TestNG's `@BeforeMethod`/`@AfterMethod`).

---

### ⚠️ Issue 1: Compilation Error — "class, interface, or enum expected"

```
LoginSteps.java:[3,1] class, interface, or enum expected
LoginSteps.java:[5,1] class, interface, or enum expected
... (13 similar errors)
```

**Root cause:** The `package stepdefinitions;` declaration was missing or malformed at the top of the file — every import line after a broken package declaration throws this exact error, because the compiler no longer recognizes the file as a valid Java source file from that point on.

### ✅ Solution

Rewrote the file from scratch, double-checking the **very first line** was exactly:

```java
package stepdefinitions;
```

with no blank lines, BOM characters, or stray text before it.

**Lesson:** This exact error signature (`class, interface, or enum expected`, repeated across almost every import line) is a strong signal to check the package declaration first, before looking anywhere else.

---

### ⚠️ Issue 2: IDE Shows "Cannot resolve symbol 'cucumber'"

**Root cause:** The IDE's project model hadn't picked up the newly added Maven dependencies yet — this is an IDE indexing issue, not a real compilation problem.

### ✅ Solution

- IntelliJ: right-click `pom.xml` → **Maven → Reload Project**
- Eclipse: right-click project → **Maven → Update Project**
- Or force it from the terminal: `mvn clean install -U` (`-U` forces Maven to re-check for updated dependencies)

---

### 🔧 Step 4: Create Test Runners

Two runner classes, differentiated only by their `tags` filter:

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "stepdefinitions",
    tags = "@smoke",
    plugin = {"pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class SmokeTestRunner extends AbstractTestNGCucumberTests { }
```

```java
@CucumberOptions(
    features = "src/test/resources/features",
    glue = "stepdefinitions",
    tags = "@regression or @edge-case",
    plugin = {"pretty", "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"}
)
public class RegressionTestRunner extends AbstractTestNGCucumberTests { }
```

---

### ⚠️ Issue 3: Allure Report Shows Tests as Grey ("Unknown" Status) Instead of Green

**Root cause:** The Cucumber-to-Allure plugin wasn't correctly wired into both runners' `plugin` array — results were being generated but not classified with a proper pass/fail status.

### ✅ Solution

Ensured the exact plugin string was present in **both** runners:

```java
plugin = {
    "pretty",
    "html:target/cucumber-reports/regression-report.html",
    "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm"
}
```

Re-ran `mvn clean test -Dtest=RegressionTestRunner` followed by `mvn allure:serve` — statuses correctly showed green.

---

### 🔧 Step 5: Run Each Group Independently

```bash
mvn clean test -Dtest=SmokeTestRunner
mvn clean test -Dtest=RegressionTestRunner
```

Confirmed: Smoke runner executed only the 1 `@smoke`-tagged scenario; Regression runner executed only the 2 `@regression`/`@edge-case`-tagged scenarios.

---

### 🔧 Step 6: Update Both CI Pipelines for Grouped Execution

**GitHub Actions** — split into two sequential jobs:

```yaml
jobs:
  smoke-tests:
    steps:
      - run: mvn clean test -Dtest=SmokeTestRunner

  regression-tests:
    needs: smoke-tests    # only runs if smoke-tests succeeds
    steps:
      - run: mvn clean test -Dtest=RegressionTestRunner
```

**Jenkins** — added as separate sequential stages in the `Jenkinsfile`:

```groovy
stage('Smoke Tests') {
    steps {
        bat '"...\\mvn.cmd" clean test -Dtest=SmokeTestRunner'
    }
}
stage('Regression Tests') {
    steps {
        bat '"...\\mvn.cmd" test -Dtest=RegressionTestRunner'
    }
}
```

---

### ⚠️ Issue 4: Jenkins Stage View Not Showing After Pipeline Update

The job ran successfully (2 tests passed) but the horizontal stage-by-stage visual (previously visible) was missing.

### ✅ Solution

Installed the **Pipeline Stage View** plugin via *Manage Jenkins → Plugins → Available Plugins*, then restarted Jenkins:

```
http://localhost:8080/restart
```

After restart, the full stage view returned, showing all 7 stages (Checkout SCM → Checkout → Smoke Tests → Regression Tests → Generate Allure Report → Publish Results → Post Actions) each marked green.

---

### Result

A fully tagged, BDD-driven test suite where Smoke and Regression run as isolated, independently-triggerable groups — in both CI tools, with Allure reporting correctly reflecting pass/fail status for each.

---

## Final Project Structure

```
selenium-ci-demo/
├── .github/
│   └── workflows/
│       └── test-pipeline.yml
├── src/
│   └── test/
│       ├── java/
│       │   ├── stepdefinitions/
│       │   │   └── LoginSteps.java
│       │   └── runners/
│       │       ├── SmokeTestRunner.java
│       │       └── RegressionTestRunner.java
│       └── resources/
│           ├── features/
│           │   └── login.feature
│           └── allure.properties
├── Jenkinsfile
├── pom.xml
└── README.md
```

---

## Command Reference Sheet

### Git

| Command | Purpose |
|---|---|
| `git init` | Initialize a new repo |
| `git add .` | Stage all changes |
| `git commit -m "message"` | Commit staged changes |
| `git push -u origin main` | Push and set upstream tracking |
| `git pull origin main` | Fetch and merge remote changes |
| `git pull --rebase origin main` | Pull and replay local commits on top |
| `git status` | Check current repo state |
| `git merge --abort` | Cancel a stuck merge |

### Maven

| Command | Purpose |
|---|---|
| `mvn clean test` | Run all tests |
| `mvn clean test -Dtest=SmokeTestRunner` | Run only the smoke suite |
| `mvn clean test -Dtest=RegressionTestRunner` | Run only regression + edge cases |
| `mvn allure:serve` | Generate and open Allure report locally |
| `mvn allure:report` | Generate report files (used in CI) |
| `mvn clean install -U` | Force refresh of dependencies |

### Jenkins

| Command | Purpose |
|---|---|
| `java -jar jenkins.war --httpPort=8080` | Start Jenkins locally |
| `http://localhost:8080/restart` | Restart Jenkins after plugin install |

---

## Key Interview Talking Points From This Build

1. **CI/CD hands-on experience** — both cloud-native (GitHub Actions) and enterprise-style (Jenkins) pipelines, built and debugged personally.
2. **Real debugging, not just tutorials followed** — headless Chrome quirks, Windows vs Linux shell differences (`sh` vs `bat`), PATH configuration, Git merge conflicts.
3. **BDD framework design** — Cucumber + TestNG, with tag-based test grouping for Smoke/Regression/Edge-case, directly supporting a Fail-Fast pipeline strategy.
4. **Reporting strategy** — Allure integrated across all three execution contexts (local, GitHub Actions, Jenkins), including annotation-driven organization (`@Epic`, `@Story`, `@Severity`).
5. **Shift-Left thinking applied practically** — this project's structure (early smoke gating, BDD scenarios as living documentation) is a direct implementation of Shift-Left principles, not just a talking point.
