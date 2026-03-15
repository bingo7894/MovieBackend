pipeline {
    agent any

    tools {
        maven 'Maven'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/bingo7894/MovieBackend.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_KEY')]) {
                    script {
                        withSonarQubeEnv('SonarQube') {
                            sh "mvn sonar:sonar -Dsonar.login=${SONAR_KEY}"
                        }
                    }
                }
            }
        }

        stage('Docker Build') {
            steps {
                sh 'docker build -t movie-jenkins:latest .'
            }
        }

         stage('Deploy to Railway') {
                             steps {
                                 withCredentials([string(credentialsId: 'railway-token', variable: 'RAILWAY_TOKEN')]) {
                                     sh 'curl -fsSL https://railway.app/install.sh | sh'

                                     sh "RAILWAY_TOKEN='${RAILWAY_TOKEN}' /usr/local/bin/railway up --detach --project 0b92a34d-a060-4173-927d-d09b2d7a87a5 --environment production --service MovieBackend"

                                 }
                             }
                         
    }
}