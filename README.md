This is a test project implementing Selenium for test automation integrated with CICD pipeline which can be scaled and used in real use case.

I built a Selenium + Maven + TestNG project integrated with two CI pipelines. GitHub Actions triggers automatically on every pull request to main, running tests in headless Chrome on a Linux runner.

I also set up a Jenkins pipeline locally with a Jenkinsfile checked into the repo — pipeline as code. I debugged real issues along the way including deprecated action versions, Windows vs Linux shell differences, and PATH configuration in Jenkins.

Project Accomplishments so far:-
-Selenium + Maven + TestNG project from scratch
-Real test on the-internet.herokuapp.com
-Headless + Incognito Chrome
-GitHub Actions pipeline — live & green
-Jenkins pipeline — live & green
-Allure Reports — local, GitHub & Jenkins

Project Structure so far:-

selenium-ci-demo/
├── .github/
│   └── workflows/
│       └── test-pipeline.yml    // GitHub Actions
├── src/
│   └── test/
│       ├── java/tests/
│       │   └── GoogleSearchTest.java   // Allure annotations
│       └── resources/
│           └── allure.properties       // Allure Results path configuration
├── Jenkinsfile                  // Jenkins pipeline
└── pom.xml                      // Allure + Surefire config, project dependencies management