@Library('my-automation-library') _

pipeline {
    agent {
        docker {
            image 'flight-booking-agent:latest'
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
		
		stage('Build & Run Smoke Tests') {
			steps {
				echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
				// Use a script block to safely build the command
				script {
					// 1. Construct the command as a Groovy string.
					//    Groovy handles the variable interpolation perfectly.
					def mvnCommand = "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke -Dbrowser.headless=true"
					
					// 2. Execute the clean command string in the shell.
					sh mvnCommand
				}
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