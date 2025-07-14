@Library('my-automation-library') _

pipeline {
    agent {
        dockerfile {
            dir '.' // Look for Dockerfile in project root
        }
    }

    options {
        // ✅ Fix path issues on Windows when running Linux containers
        customWorkspace('/home/jenkins/agent/workspace/smoke-job')
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    stages {
        stage('Log Build Info') {
            steps {
                echo "================================================="
                echo "      BUILD & TEST METADATA (SMOKE - DOCKER)"
                echo "================================================="
                echo "Job: ${env.JOB_NAME}"
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Triggered by: ${currentBuild.getBuildCauses()[0].shortDescription}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                echo "================================================="
            }
        }

        stage('Clean Workspace') {
            steps {
                cleanWs()
            }
        }

        stage('Checkout SCM') {
            steps {
                checkout scm
            }
        }

        stage('Start Selenium Grid (Docker)') {
            steps {
                echo '📦 Starting Docker-based Selenium Grid...'
                bat 'docker-compose -f docker-compose-grid.yml up -d'
                // Wait for Grid to be ready
                bat 'ping -n 20 127.0.0.1 > NUL'
            }
        }

        stage('Build & Run Smoke Tests') {
            steps {
                echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                bat "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke -Dbrowser.headless=true"
            }
        }

        stage('Stop Selenium Grid') {
            steps {
                echo '🛑 Stopping Docker-based Selenium Grid...'
                bat 'docker-compose -f docker-compose-grid.yml down'
            }
        }
    }

    post {
        always {
            echo '📦 Archiving and publishing reports...'

            // ✅ Archive and publish using shared library
            archiveAndPublishReports()

            script {
                try {
                    // ✅ Qase.io Integration
                    updateQase(
                        projectCode: 'FB',
                        credentialsId: 'qase-api-token',
                        testCaseIds: '[2]'
                    )

                    // ✅ Email Notification
                    sendBuildSummaryEmail(
                        suiteName: 'smoke',
                        emailCredsId: 'recipient-email-list'
                    )
                } catch (err) {
                    echo "⚠️ Post-build step failed: ${err.getMessage()}"
                }
            }
        }

        failure {
            echo '⚠️ Build failed. Attempting to clean up Docker Grid...'
            script {
                try {
                    def result = bat(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
                    if (result) {
                        echo "🛑 Stopping containers:\n${result}"
                        bat 'docker-compose -f docker-compose-grid.yml down'
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
