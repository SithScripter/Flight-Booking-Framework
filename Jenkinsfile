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
        
        // Stage 4: Archives the test report so it can be viewed from the Jenkins UI.
        stage('Archive Smoke Report') {
            steps {
                // The 'always' block ensures these steps run regardless of the build's outcome (success or failure).
                // This is critical so we can always access the report, especially for failed runs.
                always {
                    echo 'Preparing and archiving the HTML test report with a unique name...'
                    
                    // Step A: Rename the generic report to a specific one. This is the correct, reliable way to handle renaming.
                    bat 'if exist reports\\extent-report.html (move reports\\extent-report.html reports\\smoke-report.html)'
                    
                    // Step B: Archive the newly named file. The path is relative to the workspace root.
                    archiveArtifacts artifacts: 'reports/smoke-report.html', allowEmptyArchive: true
                }
            }
        }
    } // The 'stages' block ends here.

    // The 'post' block defines actions that run after all the main stages are complete.
    post {
        // The 'always' condition ensures a notification is sent for any build outcome.
        always {
            // A 'script' block is used here to allow for more complex Groovy logic, like defining variables and using if/else statements.
            script {
                def emailSubject
                def emailBody

                // We check the build's result to create a dynamic subject and body for the email.
                if (currentBuild.currentResult == 'SUCCESS') {
                    emailSubject = "✅ SUCCESS: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
					<p>Congratulations, the build was successful!</p>
					<p>Check the build details here: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>"""
                } else {
                    emailSubject = "❌ FAILURE: Build #${env.BUILD_NUMBER} for ${env.JOB_NAME}"
                    emailBody = """
					<p><b>WARNING: The build has failed.</b></p>
					<p>Immediate attention may be required.</p>
					<p>Check the build failure details here: <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
					"""
                }

                // This is the step provided by the Email Extension plugin to send a rich HTML email.
                emailext(
                    subject: emailSubject,
                    body: emailBody,
                    to: 'your.email@example.com', // IMPORTANT: Change this to your actual email address.
                    mimeType: 'text/html'         // We specify the body contains HTML to render links correctly.
                )
            }
        }
    }
}