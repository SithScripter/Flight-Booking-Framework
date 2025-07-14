// At the very top of the file, we import the Shared Library configured in Jenkins.
@Library('my-automation-library') _

// This pipeline now focuses on "what" to do, not "how" to do it.
pipeline {
    agent {
        dockerfile {
            filename 'Dockerfile' // This assumes the Dockerfile is in your repo root
        }
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    tools {
        maven 'apache-maven-3.9.9'
        jdk 'JDK 21'
    }

    stages {
        stage('Log Build Info') {
            steps {
                echo "================================================="
                echo "         BUILD & TEST METADATA (SMOKE)"
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
                sh 'docker-compose -f docker-compose-grid.yml up -d'
                sh 'sleep 20'
            }
        }

        stage('Build & Run Smoke Tests') {
            steps {
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

        stage('Stop Selenium Grid') {
            steps {
                echo '🛑 Stopping Docker-based Selenium Grid...'
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
