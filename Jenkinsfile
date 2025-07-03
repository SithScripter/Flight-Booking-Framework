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
        jdk 'JDK 21'             // This name MUST exactly match the name you gave your JDK installation in Jenkins.
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
        echo 'Archiving reports and publishing results...'
		
		// --- ACTION 1: Archive and Publish ---

        // ✅ Archive everything under reports (screenshots etc.)
        archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true

        // ✅ Publish the self-contained offline HTML report
        publishHTML(
            reportName: 'Test Execution Report',
            reportDir: 'reports',
            reportFiles: 'extent-report.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: true
        )
		
		// --- ACTION 2: Send Email with Correct Offline Report ---

        // ✅ Send email with correct offline HTML report attached
        script {
            def reportURL = "${env.BUILD_URL}Test-Execution-Report/"
            def emailSubject = currentBuild.currentResult == 'SUCCESS' ?
                "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}" :
                "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"

            def emailBody = """
                <p>${currentBuild.currentResult == 'SUCCESS' ? "Build was successful." : "<b>WARNING:</b> The build has failed."}</p>
                <p><b><a href='${reportURL}'>📄 View Interactive Report in Jenkins</a></b></p>
            """

            withCredentials([string(credentialsId: 'recipient-email-list', variable: 'RECIPIENT_EMAILS')]) {
                emailext(
                    subject: emailSubject,
                    body: emailBody,
                    to: RECIPIENT_EMAILS,
                    mimeType: 'text/html',
                    attachmentsPattern: 'reports/extent-report.html' // ✅ Self-contained report
                    )
                }
            }
        }
    }
}