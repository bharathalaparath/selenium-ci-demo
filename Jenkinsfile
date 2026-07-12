pipeline {

    agent any

    tools {
        maven 'Maven'        // We'll configure this name in Jenkins shortly
        jdk 'JDK11'          // We'll configure this name in Jenkins shortly
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                bat 'mvn clean compile'
            }
        }

        stage('Run Tests') {
            steps {
                bat 'mvn test'
            }
        }

        stage('Publish Results') {
            steps {
                junit 'target/surefire-reports/*.xml'
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
            echo 'Tests failed — check the report'
        }
    }
}