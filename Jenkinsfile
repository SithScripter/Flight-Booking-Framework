@Library('my-automation-library') _

pipeline {
    // We define no top-level agent. Each stage will define its own.
    agent none 

    options {
        skipDefaultCheckout()
    }

    parameters {
        choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
    }

    stages {
        stage('Initialize & Start Grid') {
            agent {
                docker {
                    image 'docker/compose:latest'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                }
            }
            steps {
                cleanWs()
                checkout scm
                echo "================================================="
                echo "         BUILD & TEST METADATA (SMOKE)"
                echo "================================================="
                echo "Job: ${env.JOB_NAME}, Build: ${env.BUILD_NUMBER}, Branch: ${env.BRANCH_NAME}"
                echo "================================================="
                echo '📦 Starting Docker-based Selenium Grid...'
                sh 'docker compose -f docker-compose-grid.yml up -d'
                sh 'sleep 20' // Give the grid a moment to stabilize
            }
        }

        stage('Build & Run Smoke Tests') {
            agent {
                docker {
                    image 'flight-booking-agent:latest'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint="" --network=selenium_grid_network'
                }
            }
            steps {
                echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                script {
                    def mvnCommand = "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke -Dbrowser.headless=true"
                    sh mvnCommand
                }
            }
        }
    }

    post {
        // This block runs regardless of build success or failure
        always {
            // We must define an agent here for the cleanup and reporting steps
            agent {
                docker {
                    image 'docker/compose:latest'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                }
            }
            steps {
                echo '🛑 Stopping Docker-based Selenium Grid...'
                sh 'docker compose -f docker-compose-grid.yml down || echo "Grid already stopped."'
                
//                echo '📦 Archiving and publishing reports...'
//                archiveAndPublishReports()
//
//                script {
//                    try {
//                        updateQase(
//                            projectCode: 'FB',
//                            credentialsId: 'qase-api-token',
//                            testCaseIds: '[2]'
//                        )
//                        sendBuildSummaryEmail(
//                            suiteName: 'smoke',
//                            emailCredsId: 'recipient-email-list'
//                        )
//                    } catch (err) {
//                        echo "⚠️ Post-build notification actions failed: ${err.getMessage()}"
//                    }
//                }
            }
        }
    }
}