@Library('my-automation-library') _

pipeline
{
	agent none

	options
	{
		skipDefaultCheckout()
	}

	parameters
	{
		choice(name: 'TARGET_ENVIRONMENT', choices: ['PRODUCTION', 'STAGING', 'QA'], description: 'Select environment')
		string(name: 'QASE_TEST_CASE_IDS', defaultValue: '[2]', description: 'Comma-separated Qase Test Case IDs')
	}

	stages
	{
		stage('Initialize & Start Grid')
		{
			agent
			{
				docker
				{
					image 'flight-booking-agent:latest'
					args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""'
				}
			}
			steps
			{
				cleanWs()
				checkout scm
				printBuildMetadata('smoke')
				echo '📦 Starting Docker-based Selenium Grid...'
				// Using your retry logic with the shared library function
				retry(2)
				{
					startDockerGrid('docker-compose-grid.yml', 20)
				}
			}
		}

		stage('Build & Run Smoke Tests')
		{
			agent
			{
				docker
				{
					image 'flight-booking-agent:latest'
					args '-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint="" --network=selenium_grid_network'
				}
			}
			steps
			{
				echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
				script
				{
					parallel(
							Chrome:
							{
								echo '🧪 Running Smoke tests on Chrome...'
								sh """
                                mvn clean test \
                                -P smoke \
                                -Denv=${params.TARGET_ENVIRONMENT} \
                                -Dtest.suite=smoke \
                                -Dbrowser=CHROME \
                                -Dreport.dir=chrome \
                                -Dbrowser.headless=true \
                                -Dmaven.repo.local=.m2-chrome
                            """
							},
							Firefox:
							{
								echo '🧪 Running Smoke tests on Firefox...'
								sh """
                                mvn clean test \
                                -P smoke \
                                -Denv=${params.TARGET_ENVIRONMENT} \
                                -Dtest.suite=smoke \
                                -Dbrowser=FIREFOX \
                                -Dreport.dir=firefox \
                                -Dbrowser.headless=true \
                                -Dmaven.repo.local=.m2-firefox
                            """
							}
							)
				}
			}
		}
	}

	post
	{
		always
		{
			script
			{
				docker.image('flight-booking-agent:latest').inside('-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""')
				{
					echo '🛑 Stopping Docker-based Selenium Grid...'
					//					sh 'docker-compose -f docker-compose-grid.yml down || echo "Grid already stopped."'
					stopDockerGrid('docker-compose-grid.yml')

					echo '📦 Archiving and publishing reports...'
					generateDashboard("smoke", "${env.BUILD_NUMBER}")

					archiveAndPublishReports()

					try
					{
						updateQase(
								projectCode: 'FB',
								credentialsId: 'qase-api-token',
								testCaseIds: params.QASE_TEST_CASE_IDS
								)
						sendBuildSummaryEmail(
								suiteName: 'smoke',
								emailCredsId: 'recipient-email-list'
								)
					} catch (err)
					{
						echo "⚠️ Post-build notification actions failed: ${err.getMessage()}"
					}
				}
			}
		}

		failure
		{
			script
			{
				docker.image('flight-booking-agent:latest').inside('-u root -v /var/run/docker.sock:/var/run/docker.sock --entrypoint=""')
				{
					echo '⚠️ Build failed. Checking for running Selenium containers...'
					try
					{
						def result = sh(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
						if (result)
						{
							echo "🛑 Stopping containers:\n${result}"
							stopDockerGrid('docker-compose-grid.yml')
						} else
						{
							echo "✅ No active Selenium containers to stop."
						}
					} catch (e)
					{
						echo "⚠️ Docker cleanup error: ${e.getMessage()}"
					}
				}
			}
		}
	}
}
