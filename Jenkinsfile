pipeline {
    // 'agent any' specifies that this pipeline can run on any available Jenkins agent machine.
    // For a local setup, this means it will run on the main Jenkins instance.
    agent any

    // The 'parameters' block defines interactive inputs that create a form on the "Build with Parameters" page.
    parameters {
        // This 'choice' parameter creates a dropdown menu for environment selection.
        choice(
            name: 'TARGET_ENVIRONMENT', // The name of the variable that will hold the user's selected value.
            choices: ['PRODUCTION', 'STAGING', 'QA'], // The list of options. 'QA' will be the default as it's first.
            description: 'Select the target environment to run the tests against.'
        )
    }

    // The 'tools' block specifies which pre-configured tools from Jenkins Global Tool Configuration are required for this pipeline.
    // Jenkins will ensure these tools are available in the system's PATH.
    tools {
        maven 'apache-maven-3.9.9' // This name MUST exactly match the name you gave your Maven installation in Jenkins.
        jdk 'JDK 21'               // This name MUST exactly match the name you gave your JDK installation in Jenkins.
    }

    // The 'stages' block contains the main sequence of work for our pipeline. Each stage is a logical unit of work.
    stages {
        // Stage 1: A standard best practice to ensure the build starts in a clean environment.
        stage('Clean Workspace') {
            steps {
                // 'cleanWs()' is a built-in Jenkins step that deletes all files from the workspace from any previous builds.
                cleanWs()
            }
        }

        // Stage 2: Clones or pulls the latest source code from the repository configured in the Jenkins job UI.
        stage('Checkout SCM') {
            steps {
                // 'checkout scm' is a built-in step that uses the Source Control Management configuration from the job's UI page.
                checkout scm
            }
        }

        // Stage 3: Compiles the code and executes our test suite using Maven.
        stage('Build & Run Smoke Tests') {
            steps {
                // 'echo' prints a message to the Jenkins console log for better traceability.
                echo "Running tests on environment selected by user: ${params.TARGET_ENVIRONMENT}"

                // 'bat' executes a Windows batch command. For Linux/macOS agents, you would use 'sh'.
                // We dynamically insert the user's selected parameter value into the Maven command.
                bat "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT}"
            }
        }
    } // The 'stages' block ends here.

    // The 'post' block with the final, correct logic for archiving, publishing, and notifications.
    post {
        always {
            // ---ACTION 1: Archive and Publish the Full Report Folder---
            echo 'Archiving and publishing the full HTML report folder...'

            // First, rename the report file itself so we can find it easily.
            bat 'if exist reports\\extent-report.html (move reports\\extent-report.html reports\\smoke-report.html)'

            // If report is in target folder, copy it and its assets to a consistent reports folder.
            bat """
                if exist target\\extent-report.html (
                    mkdir reports
                    copy target\\extent-report.html reports\\smoke-report.html
                    copy target\\*.css reports\\
                    copy target\\*.js reports\\
                    copy target\\*.png reports\\
                )
            """

            // Now, archive the ENTIRE 'reports' directory to preserve CSS/JS assets.
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true

            // Publish the specific HTML file from within the archived directory.
            publishHTML(
                reportName: 'Smoke Test Report',
                reportDir: 'reports',
                reportFiles: 'smoke-report.html',
                keepAll: true,
                alwaysLinkToLastBuild: true,
                allowMissing: true
            )

            // --- ACTION 2: Send Email Notification with a Link AND an Attachment ---
            script {
                def emailSubject
                def emailBody

                // This is the direct link to the report artifact that Jenkins stores.
                def reportURL = "${env.BUILD_URL}artifact/reports/smoke-report.html"

                if (currentBuild.currentResult == 'SUCCESS') {
                    emailSubject = "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
                        <p>Build was successful.</p>
                        <p><b><a href='${env.BUILD_URL}'>View Build in Jenkins</a></b></p>
                        <p><b><a href='${reportURL}'>📄 View Smoke Test Report</a></b></p>
                    """
                } else {
                    emailSubject = "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
                        <p><b>WARNING: The build has failed.</b></p>
                        <p><b><a href='${env.BUILD_URL}'>View Build in Jenkins</a></b></p>
                        <p><b><a href='${reportURL}'>📄 View Smoke Test Report</a></b></p>
                    """
                }

                withCredentials([string(credentialsId: 'recipient-email-list', variable: 'RECIPIENT_EMAILS')]) {
                    emailext(
                        subject: emailSubject,
                        body: emailBody,
                        to: RECIPIENT_EMAILS,
                        mimeType: 'text/html',
                        // We can also still attach the file for offline viewing.
                        attachmentsPattern: 'reports/smoke-report.html'
                    )
                }
            }
        }
    }
}
