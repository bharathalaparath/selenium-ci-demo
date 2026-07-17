pipeline {

    agent any

    environment {
            MAVEN_HOME = 'C:\\Program Files\\apache-maven-3.9.14'
            JAVA_HOME  = 'C:\\Program Files\\Java\\jdk-25.0.3'
            PATH       = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;C:\\Windows\\System32"
        }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Smoke Tests') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" clean test -Dtest=SmokeTestRunner'
            }
        }

        stage('Regression Tests') {
            steps {
                bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" clean test -Dtest=RegressionTestRunner'
            }
        }

        stage('Generate Allure Report') {
             steps {
                        bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" allure:report'
             }
        }

        stage('Publish Results') {
            steps {
                junit 'target/surefire-reports/*.xml'
                allure([
                    includeProperties: false,
                    jdk: '',
                    results: [[path: 'target/allure-results']]
                ])
            }
        }
    }

    post {
        always {
            echo 'Pipeline complete'
        }
        success {
            echo 'All tests passed!'
        }
        failure {
            echo 'Smoke Tests failed — Regression skipped, check the report'
        }
    }
}