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
				echo "================================================="
				echo "         BUILD & TEST METADATA"
				echo "================================================="
				echo "Job: ${env.JOB_NAME}, Build: ${env.BUILD_NUMBER}, Branch: ${env.BRANCH_NAME}"
				echo "================================================="
				echo '📦 Starting Docker-based Selenium Grid...'
				sh 'docker-compose -f docker-compose-grid.yml up -d'
				sh 'sleep 20'
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
					sh 'docker-compose -f docker-compose-grid.yml down || echo "Grid already stopped."'

					echo '📦 Archiving and publishing reports...'

					script
					{
						def suiteName = "smoke"
						def summaryFile = "reports/${suiteName}-failure-summary.txt"
						def hasFailures = fileExists(summaryFile) && readFile(summaryFile).trim().toLowerCase().contains("failed")

						def failureSummary = hasFailures ? readFile(summaryFile).trim() : "✅ All tests passed."
						def failureHeader = hasFailures ? "❌ Failure Summary" : "✅ Test Result Summary"
						def failureBoxColor = hasFailures ? "#fff3f3" : "#f3fff3"
						def failureBorderColor = hasFailures ? "#f44336" : "#4CAF50"
						def failureTextColor = hasFailures ? "#c62828" : "#2e7d32"

						writeFile file: 'reports/index.html', text: """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <title>Smoke Test Dashboard</title>
                                <style>
                                    body { font-family: Arial; padding: 20px; background-color: #f7f7f7; }
                                    h1 { color: #222; }
                                    ul { list-style-type: none; padding-left: 0; }
                                    li { margin: 10px 0; }
                                    a { color: #1976D2; font-size: 16px; text-decoration: none; }
                                    a:hover { text-decoration: underline; }
                                    .summary-box {
                                        background-color: ${failureBoxColor};
                                        border-left: 6px solid ${failureBorderColor};
                                        padding: 10px;
                                        margin-top: 20px;
                                        white-space: pre-line;
                                        font-family: monospace;
                                        color: ${failureTextColor};
                                    }
                                </style>
                            </head>
                            <body>
                                <h1>📊 Smoke Test Dashboard</h1>
                                <ul>
                                    <li>🧪 <a href="chrome/index.html" target="_blank">Chrome Report</a></li>
                                    <li>🧪 <a href="firefox/index.html" target="_blank">Firefox Report</a></li>
                                </ul>
                                <p><strong>Build:</strong> #${env.BUILD_NUMBER}</p>
                                <h2>${failureHeader}</h2>
                                <div class="summary-box">${failureSummary}</div>
                            </body>
                            </html>
                        """
					}

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
	}
}
