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
        stage('Build & Run Smoke Tests') {
            steps {
                echo "Running smoke tests on: ${params.TARGET_ENVIRONMENT}"
                bat "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT} -Dtest.suite=smoke"
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
            publishHTML(reportName: 'Smoke Test Report', reportDir: 'reports', reportFiles: 'index.html', keepAll: true, alwaysLinkToLastBuild: true, allowMissing: true)
            
            script {
                // --- Qase.io Integration ---
                try {
                    // ... your existing Qase logic ...
                } catch (Exception err) {
                    echo "⚠️ Warning: Qase.io integration failed: ${err.getMessage()}"
                }

                // --- Email Notification Logic ---
                def reportToAttach = 'reports/smoke-report.html'
                def summaryFile = 'reports/smoke-failure-summary.txt'
                def failureSummary = fileExists(summaryFile) ? readFile(summaryFile).trim() : ""
                def reportURL = "${env.BUILD_URL}Smoke-Test-Report/"

                def emailSubject
                def emailBody

                if (currentBuild.currentResult == 'SUCCESS') {
                    emailSubject = "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """<p>Build was successful.</p><p><b><a href='${reportURL}'>📄 View Test Report in Jenkins</a></b></p>"""
                } else {
                    emailSubject = "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
                        <p><b>WARNING: The build has failed.</b></p>
                        <p><b>Failure Summary:</b></p>
                        <pre style="background-color:#F5F5F5; border:1px solid #E0E0E0; padding:10px; font-family:monospace;">${failureSummary}</pre>
                        <p><b><a href='${reportURL}'>📄 View Full Report in Jenkins</a></b></p>
                    """
                }

                withCredentials([string(credentialsId: 'recipient-email-list', variable: 'RECIPIENT_EMAILS')]) {
                    emailext(
                        subject: emailSubject,
                        body: emailBody,
                        to: RECIPIENT_EMAILS,
                        mimeType: 'text/html',
                    )
                }
            }
        }
    }
}