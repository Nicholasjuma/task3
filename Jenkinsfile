pipeline {
    agent any
    environment {
        ANDROID_HOME = "/path/to/android/sdk"
        GRADLE_OPTS = "-Dorg.gradle.daemon=false"
    }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/your-repo.git'
            }
        }
        stage('Setup JDK') {
            steps {
                script {
                    def jdkTool = tool name: 'JDK11', type: 'jdk'
                    env.JAVA_HOME = "${jdkTool}"
                    sh 'java -version'
                }
            }
        }
        stage('Build') {
            steps {
                sh './gradlew assembleDebug'
            }
        }
        stage('Run Tests') {
            steps {
                sh './gradlew test'
            }
        }
        stage('Deploy') {
            steps {
                sh './gradlew appDistributionUploadDebug'
            }
        }
    }
}
