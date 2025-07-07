pipeline {
    agent any

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
                echo "          BUILD & TEST METADATA (SMOKE)"
                echo "================================================="
                echo "Job: ${env.JOB_NAME}"
                echo "Build Number: ${env.BUILD_NUMBER}"
                echo "Triggered by: ${currentBuild.getBuildCauses()[0].shortDescription}"
                echo "Branch: ${env.BRANCH_NAME}"
                echo "Commit: ${env.GIT_COMMIT}"
                echo "================================================="
            }
        }

        stage('Clean Workspace') { steps { cleanWs() } }

        stage('Checkout SCM') { steps { checkout scm } }

        stage('Start Selenium Grid (Docker)') {
            steps {
                echo '📦 Starting Docker-based Selenium Grid...'
                bat 'docker-compose -f docker-compose.yml up -d'
				// ✅ Add wait to allow nodes to register with the hub
				bat 'ping -n 20 127.0.0.1 > NUL'
            }
        }

        stage('Build & Run Smoke Tests') {
            steps {
                echo "🧪 Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                bat "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke"
            }
        }

        stage('Stop Selenium Grid') {
            steps {
                echo '🛑 Stopping Docker-based Selenium Grid...'
                bat 'docker-compose -f docker-compose.yml down'
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true

            publishHTML(reportName: 'Smoke Test Report',
                        reportDir: 'reports',
                        reportFiles: 'index.html',
                        keepAll: true,
                        alwaysLinkToLastBuild: true,
                        allowMissing: true)

            script {
                // 🔗 Qase.io Integration
                try {
                    echo '--- Starting Qase.io Integration ---'
                    def runId

                    withCredentials([string(credentialsId: 'qase-api-token', variable: 'QASE_TOKEN')]) {
                        echo '1. Creating new run on Qase...'
                        bat """
                            curl -s -X POST "https://api.qase.io/v1/run/FB" ^
                            -H "accept: application/json" ^
                            -H "Content-Type: application/json" ^
                            -H "Token: %QASE_TOKEN%" ^
                            -d "{\\"title\\":\\"${env.JOB_NAME} - Build ${env.BUILD_NUMBER}\\", \\"cases\\":[2]}" ^
                            -o response.json
                        """
                        def responseJson = readJSON file: 'response.json'

                        if (responseJson.status) {
                            runId = responseJson.result.id
                            echo "✅ Qase Run ID created: ${runId}"

                            echo '2. Uploading test results to Qase...'
                            bat """
                                curl -s -X PATCH "https://api.qase.io/v1/result/FB/${runId}/testng" ^
                                -H "accept: application/json" ^
                                -H "Content-Type: multipart/form-data" ^
                                -H "Token: %QASE_TOKEN%" ^
                                -F "file=@target/surefire-reports/testng-results.xml"
                            """

                            echo '3. Marking Qase Run as Complete...'
                            bat """
                                curl -s -X POST "https://api.qase.io/v1/run/FB/${runId}/complete" ^
                                -H "accept: application/json" ^
                                -H "Token: %QASE_TOKEN%"
                            """
                        } else {
                            echo "⚠️ Qase API error: ${responseJson}"
                        }
                    }
                } catch (Exception err) {
                    echo "⚠️ Qase integration failed: ${err.getMessage()}"
                }

                // 📧 Email Notification
                def suiteName = "smoke"
                def reportToAttach = "reports/${suiteName}-report.html"
                def summaryFile = "reports/${suiteName}-failure-summary.txt"
                def failureSummary = fileExists(summaryFile) ? readFile(summaryFile).trim() : ""
                def reportURL = "${env.BUILD_URL}Smoke-Test-Report/"

                def emailSubject
                def emailBody

                if (currentBuild.currentResult == 'SUCCESS') {
                    emailSubject = "✅ SUCCESS: Smoke Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
                        <p>Smoke build was successful.</p>
                        <p><b><a href='${reportURL}'>📄 View Smoke Report</a></b></p>
                    """
                } else {
                    emailSubject = "❌ FAILURE: Smoke Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
                        <p><b>Smoke build failed.</b></p>
                        <p><b>Failure Summary:</b></p>
                        <pre style="background-color:#F5F5F5; border:1px solid #E0E0E0; padding:10px; font-family:monospace;">${failureSummary}</pre>
                        <p><b><a href='${reportURL}'>📄 View Full Smoke Report</a></b></p>
                    """
                }

                withCredentials([string(credentialsId: 'recipient-email-list', variable: 'RECIPIENT_EMAILS')]) {
                    emailext(
                        subject: emailSubject,
                        body: emailBody,
                        to: RECIPIENT_EMAILS,
                        mimeType: 'text/html',
                        attachmentsPattern: reportToAttach
                    )
                }
            }
        }

        failure {
            // 🧹 Stop Grid if failure before shutdown stage
            echo '⚠️ Build failed. Attempting to clean up Docker Grid...'
            script {
                try {
                    def result = bat(script: 'docker ps -a --filter "name=selenium" --format "{{.Names}}"', returnStdout: true).trim()
                    if (result) {
                        echo "🛑 Stopping containers:\n${result}"
                        bat 'docker-compose -f docker-compose.yml down || echo "Grid already stopped"'
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
