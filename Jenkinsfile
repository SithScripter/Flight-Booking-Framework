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
        stage('Checkout SCM') {
            steps {
				checkout scm
                cleanWs()
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
		
		stages {
			stage('Start Selenium Grid') {
				// Use a specific agent just for Docker Compose
				agent {
					docker {
						image 'docker/compose:latest'
						args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
					}
				}
				steps {
					// This stage checks out the code to find the docker-compose file,
					// then starts the grid. This CREATES the network.
					cleanWs()
					checkout scm
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
		

//        stage('Start Selenium Grid') {
//            steps {
//				sh 'docker network inspect selenium_grid_network || docker network create selenium_grid_network'
//                sh 'docker compose -f docker-compose-grid.yml up -d'
//                sh 'sleep 20'
//            }
//        }
//		
//		stage('Build & Run Smoke Tests') {
//			steps {
//				echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
//				// Use a script block to safely build the command
//				script {
//					// 1. Construct the command as a Groovy string.
//					//    Groovy handles the variable interpolation perfectly.
//					def mvnCommand = "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke -Dbrowser.headless=true"
//					
//					// 2. Execute the clean command string in the shell.
//					sh mvnCommand
//				}
//			}
//		}

//        stage('Stop Selenium Grid') {
//            steps {
//                sh 'docker compose -f docker-compose-grid.yml down --remove-orphans'
//            }
//        }
//    }

    post {
			// 'always' will run after all stages are attempted
			always {
				stage('Stop Selenium Grid & Archive Reports') {
					// Use the docker/compose agent again to tear down the grid
					agent {
						docker {
							image 'docker/compose:latest'
							args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
						}
					}
					steps {
						echo '🛑 Stopping Docker-based Selenium Grid...'
						sh 'docker-compose -f docker-compose-grid.yml down || echo "Grid already stopped."'
						
						echo '📦 Archiving and publishing reports...'
						// Note: Post-build actions also need an agent context
						// This shared library call will execute within this agent
						archiveAndPublishReports()
					}
//            echo '📦 Archiving and publishing reports...'
//            archiveAndPublishReports()

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