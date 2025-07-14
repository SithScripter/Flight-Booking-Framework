@Library('my-automation-library') _
pipeline {
    agent {
        dockerfile true  // 🐳 Use Dockerfile-based ephemeral container
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    stages {
        stage('Log Build Info') {
            steps {
                echo "================================================="
                echo "        BUILD & TEST METADATA (SMOKE)"
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
                bat "type Jenkinsfile-smoke"
            }
        }

        // ✅ Only start grid in 'enhancements' branch to avoid resource collision
        stage('Start Selenium Grid (Docker)') {
            when {
                branch 'enhancements'
            }
            steps {
                script {
                    retry(2) {
                        echo "🟡 Starting Docker Selenium Grid..."
                        bat 'docker-compose -f docker-compose-grid.yml up -d'
                        bat 'ping -n 20 127.0.0.1 > NUL'
                    }
                }
            }
        }

        stage('Run Smoke Tests') {
            when {
                branch 'enhancements'
            }
            steps {
                echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                bat """
                    mvn clean test ^
                    -P smoke ^
                    -Denv=${params.TARGET_ENVIRONMENT} ^
                    -Dtest.suite=smoke ^
                    -Dbrowser.headless=true
                """
            }
        }
    }

    post {
        always {
            echo '📦 Archiving and publishing reports...'
            archiveAndPublishReports()

            script {
                if (env.BRANCH_NAME == 'enhancements') {
                    echo "🧹 Cleaning up Grid, updating Qase, sending email..."
                    
                    stopDockerGrid()

                    updateQase(
                        projectCode: 'FB',
                        credentialsId: 'qase-api-token',
                        testCaseIds: '[2]' // adjust case IDs if needed
                    )

                    sendBuildSummaryEmail(
                        suiteName: 'smoke',
                        emailCredsId: 'recipient-email-list'
                    )
                } else {
                    echo "ℹ️ Skipping teardown/Qase/email for branch: ${env.BRANCH_NAME}"
                }
            }
        }

        failure {
            echo '⚠️ Build failed. Attempting to clean up...'
            script {
                try {
                    def result = bat(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
                    if (result) {
                        echo "🛑 Stopping containers:\n${result}"
                        stopDockerGrid()
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
