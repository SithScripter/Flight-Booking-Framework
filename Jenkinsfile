@Library('my-automation-library') _

pipeline {
    agent {
        docker {
            image 'docker/compose:latest'
            args '-u root -v /var/run/docker.sock:/var/run/docker.sock'
        }
    }

    // This options block is the key to the fix
    options {
        // Prevents Jenkins from doing a checkout on the controller before the agent starts
        skipDefaultCheckout()
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    stages {
        // This stage must now be first to get the code into our agent
        stage('Checkout SCM') {
            steps {
                // We manually check out the code now that we are in the correct agent
                cleanWs() // Clean workspace before checkout
                checkout scm
            }
        }

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
            // The post actions will now run in the agent's context
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