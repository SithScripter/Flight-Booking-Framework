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

    // The 'post' block defines actions that run after all the main stages are complete.
    post {
        // The 'always' condition ensures a notification is sent for any build outcome.
        always {
			 // ---- ACTION 1: Archive and Process Reports ----
            echo 'Archiving reports and publishing HTML report...'

            // Step 1: Rename the report to give it a unique name.
            bat 'if exist reports\\extent-report.html (move reports\\extent-report.html reports\\smoke-report.html)'

            // Step 2: Archive the artifact. This keeps a raw copy of the report with the build.
            archiveArtifacts artifacts: 'reports/smoke-report.html', allowEmptyArchive: true
            
            // Step 3: Publish the HTML report. This creates a user-friendly link in the Jenkins UI.
            publishHTML(
                reportName: 'Smoke Test Report', // The name of the link that will appear on the job page.
                reportDir: 'reports',           // The directory (relative to workspace) where the report is located.
                reportFiles: 'smoke-report.html', // The specific HTML file to display.
                keepAll: true,                  // Keep reports for all builds, not just the last one.
                alwaysLinkToLastBuild: true,    // Ensure the main job page links to the latest report.
                allowMissing: true              // Don't fail the build if the report is missing for some reason.
            )
			
			// ---- ACTION 2: Send Email Notification ----
            // A 'script' block is used here to allow for more complex Groovy logic, like defining variables and using if/else statements.
            script {
                def emailSubject
                def emailBody

                // We check the build's result to create a dynamic subject and body for the email.
                if (currentBuild.currentResult == 'SUCCESS') {
                    emailSubject = "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
					<p>Build was successful. See the attached report and test results.</p>
					<p>Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
					"""
                } else {
                    emailSubject = "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """<p><b>WARNING: The build has failed.</b></p>
					<p>See the attached report and test results for details.</p>
					<p>Build URL: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
					"""
                }
                
                 // This is the step provided to send a rich HTML email
                // Use withCredentials to securely access the email address
                withCredentials([string(credentialsId: 'recipient-email-list', variable: 'RECIPIENT_EMAILS')]) {
                    emailext(
                        subject: emailSubject,
                        body: emailBody,
                        to: RECIPIENT_EMAILS, // Use the variable injected by withCredentials
                        mimeType: 'text/html'
                    )
            	}
            }
        }
    }
}