pipeline {
    agent any

    tools {
        maven 'Maven3'   // Name configured in Manage Jenkins > Tools
        jdk   'JDK17'    // Name configured in Manage Jenkins > Tools
    }

    triggers {
        // SCM polling every minute (alternative: GitHub webhook)
        pollSCM('* * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // CI enhancement 1: automated unit tests run BEFORE the build
        stage('Test') {
            steps {
                sh 'mvn -B clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build') {
            steps {
                sh 'mvn -B package -DskipTests'
            }
        }

        // CI enhancement 2: version the artifact by build number + git commit SHA
        stage('Version Artifact') {
            steps {
                script {
                    env.SHORT_SHA = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                }
                sh 'cp target/todo-api.jar target/todo-api-${BUILD_NUMBER}-${SHORT_SHA}.jar'
            }
        }

        stage('Archive') {
            steps {
                archiveArtifacts artifacts: 'target/todo-api-*.jar', fingerprint: true
            }
        }
    }

    post {
        success { echo "Build #${env.BUILD_NUMBER} (${env.SHORT_SHA}) succeeded" }
        failure { echo 'Build failed' }
    }
}
