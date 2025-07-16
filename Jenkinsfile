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
            // Use a specific agent just for Docker Compose
            agent {
                docker {
                    image 'docker/compose:latest'
                    args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                }
            }
            steps {
                // This stage checks out the code, then starts the grid, which CREATES the network.
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
            // Use your all-in-one agent for the tests
            agent {
                docker {
                    image 'flight-booking-agent:latest'
                    // Now we can successfully connect to the network created in the previous stage
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
        always {
            stage('Stop Grid & Finalize Build') {
                // Use the docker/compose agent again to tear down the grid and handle reports
                agent {
                    docker {
                        image 'docker/compose:latest'
                        args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                    }
                }
                steps {
                    echo '🛑 Stopping Docker-based Selenium Grid...'
                    sh 'docker compose -f docker-compose-grid.yml down || echo "Grid already stopped."'
                    
//                    echo '📦 Archiving and publishing reports...'
//                    archiveAndPublishReports()

                    // Your custom script block for Qase and email notifications
//                    script {
//                        try {
//                            updateQase(
//                                projectCode: 'FB',
//                                credentialsId: 'qase-api-token',
//                                testCaseIds: '[2]'
//                            )
//        
//                            sendBuildSummaryEmail(
//                                suiteName: 'smoke',
//                                emailCredsId: 'recipient-email-list'
//                            )
//                        } catch (err) {
//                            echo "⚠️ Post-build actions failed: ${err.getMessage()}"
//                        }
//                    }
                }
            }
        }

        failure {
            // This block runs only if the build fails
            stage('Failure Cleanup') {
                // It needs its own agent to run docker commands
                agent {
                     docker {
                        image 'docker/compose:latest'
                        args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
                    }
                }
                steps {
                    echo '⚠️ Build failed. Attempting to clean up docker containers...'
                    script {
                        try {
                            def result = sh(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
                            if (result) {
                                echo "🛑 Stopping containers:\n${result}"
                                sh 'docker compose -f docker-compose-grid.yml down || echo "Grid already stopped"'
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
    }
}