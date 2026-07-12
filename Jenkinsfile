pipeline {

    agent any

    environment {
            MAVEN_HOME = 'C:\\Program Files\\apache-maven-3.9.14'
            JAVA_HOME  = 'C:\\Program Files\\Java\\jdk-25.0.3'
            PATH       = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;C:\\Windows\\System32"
        }

//     tools {
//         maven 'Maven'        // configure this name in Jenkins
//         jdk 'JDK11'          // configure this name in Jenkins
//     }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
//                 bat 'mvn clean compile'
                   bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" clean compile'
            }
        }

        stage('Run Tests') {
            steps {
//                 bat 'mvn test'
                bat '"C:\\Program Files\\apache-maven-3.9.14\\bin\\mvn.cmd" test'
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
            echo 'Tests failed — check the report'
        }
    }
}