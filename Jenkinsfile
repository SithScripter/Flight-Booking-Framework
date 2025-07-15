@Library('my-automation-library') _

pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile'
        }
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    // Removed tools block: handled by Docker image

    options {
        skipDefaultCheckout(true)
    }

    stages {
        stage('Start Selenium Grid (on host)') {
            agent any
            steps {
                echo '📦 Starting Docker-based Selenium Grid on host...'
                sh 'docker-compose -f docker-compose-grid.yml up -d'
                sh 'sleep 20'
            }
        }

        stage('Run Smoke Tests (in Docker Agent)') {
            steps {
                checkout scm

                echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                sh """
                    mvn clean test \\
                    -P smoke \\
                    -Denv=${params.TARGET_ENVIRONMENT} \\
                    -Dtest.suite=smoke \\
                    -Dbrowser.headless=true
                """
            }
        }

        stage('Stop Selenium Grid (on host)') {
            agent any
            steps {
                echo '🛑 Stopping Docker-based Selenium Grid on host...'
                sh 'docker-compose -f docker-compose-grid.yml down'
            }
        }
    }

    post {
        always {
            echo '📦 Archiving and publishing reports...'
            archiveAndPublishReports()

            script {
                try {
                    updateQase(
                        projectCode: 'FB',
                        credentialsId: 'qase-api-token',
                        testCaseIds: '[2]'
                    )

                    sendBuildSummaryEmail(
                        suiteName: 'smoke',
                        emailCredsId: 'recipient-email-list'
                    )
                } catch (err) {
                    echo "⚠️ Post-build actions failed: ${err.getMessage()}"
                }
            }
        }

        failure {
            echo '⚠️ Build failed. Attempting to clean up grid...'
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
