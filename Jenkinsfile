@Library('my-automation-library') _

pipeline {
    agent {
        docker {
            image 'docker/compose:latest'
            // Add --entrypoint='' to the args to fix the agent startup issue
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
        }
    }

    options {
        skipDefaultCheckout()
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    stages {
        stage('Checkout SCM') {
            steps {
                cleanWs()
                checkout scm
            }
        }

        stage('Log Build Info') {
            steps {
                echo "Job: ${env.JOB_NAME}"
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
            }
        }

        stage('Start Selenium Grid') {
            steps {
                sh 'docker-compose -f docker-compose-grid.yml up -d'
                sh 'sleep 20'
            }
        }

        stage('Run Tests') {
            steps {
                sh 'mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke -Dbrowser.headless=true'
            }
        }

        stage('Stop Selenium Grid') {
            steps {
                sh 'docker-compose -f docker-compose-grid.yml down'
            }
        }
    }

    post {
        always {
            echo '📦 Archiving and publishing reports...'
            archiveAndPublishReports()

//            script {
//                try {
//                    updateQase(
//                        projectCode: 'FB',
//                        credentialsId: 'qase-api-token',
//                        testCaseIds: '[2]'
//                    )
//
//                    sendBuildSummaryEmail(
//                        suiteName: 'smoke',
//                        emailCredsId: 'recipient-email-list'
//                    )
//                } catch (err) {
//                    echo "⚠️ Post-build actions failed: ${err.getMessage()}"
//                }
//            }
        }

        failure {
            echo '⚠️ Build failed. Attempting to clean up...'
            script {
                try {
                    def result = sh(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
                    if (result) {
                        echo "🛑 Stopping containers:\n${result}"
                        sh 'docker-compose -f docker-compose-grid.yml down || echo "Grid already stopped"'
                    } else {
                        echo "✅ No active Selenium containers to stop."
                    }
                } catch (e) {
                    echo "⚠️ Docker cleanup error: ${e.getMessage()}"
                }
            }
        }
    }
}