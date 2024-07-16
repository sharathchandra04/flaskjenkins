pipeline {
    agent any

    environment {
        // Define Docker image name
        DOCKER_IMAGE = "sharathchandra04/sharathchandra04"
    }
    tools {
        python 'Python3' // Use the Python installation named 'Python3'
    }
    stages {
        stage('Checkout') {
            steps {
                git url: 'https://github.com/sharathchandra04/flaskjenkins.git', branch: 'dev'
            }
        }
        stage('Get Version and Timestamp') {
            steps {
                script {
                    env.VERSION = sh(script: 'python -c "import json; print(json.load(open(\'./version.json\'))[\'version\'])"', returnStdout: true).trim()
                    env.TIMESTAMP = sh(script: 'date +%Y_%m_%d_%H_%M_%S', returnStdout: true).trim()
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    def customTag = "${env.TIMESTAMP}_${env.VERSION}_dev"
                    sh "echo ${customTag}"
                    def dockerImage = docker.build("${env.DOCKER_IMAGE}:${customTag}")
                    sh "docker tag ${env.DOCKER_IMAGE}:${customTag} ${env.DOCKER_IMAGE}:latest"
                    docker.withRegistry('https://registry.hub.docker.com', 'dockercreds') {
                        dockerImage.push(customTag)
                        dockerImage.push('latest')
                    }
                }
            }
        }
    }

    post {
        always {
            script {
                // Example of reading emails from a JSON file
                def emails = sh(script: 'python -c "import json; print(\',\'.join(json.load(open(\'./jenkinsemails.json\'))[\'developers\']))"', returnStdout: true).trim()
                emailext (
                    to: emails,
                    subject: "Jenkins Build ${env.JOB_NAME} #${env.BUILD_NUMBER}",
                    body: """<p>Build ${env.BUILD_NUMBER} of project ${env.JOB_NAME}:</p>
                    <p>Build status: ${currentBuild.currentResult}</p>
                    <p>Docker Image Tag: ${env.DOCKER_IMAGE}:${env.TIMESTAMP}-${env.VERSION}</p>
                    <p>Check the console output at <a href="${env.BUILD_URL}">${env.BUILD_URL}</a> to view the results.</p>"""
                )
            }
        }
        success {
            echo 'Build and Docker image creation successful!'
        }
        failure {
            echo 'Build or Docker image creation failed!'
        }
    }
}

