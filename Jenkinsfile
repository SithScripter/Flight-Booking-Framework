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
			
			// Stage 1: for providing LOg Build Info-good for debugging
		    stage('Log Build Info') {
        	steps {
            	// This 'echo' step prints a clear header to the console log for easy reading.
           		echo "================================================="
            	echo "          BUILD & TEST METADATA"
            	echo "================================================="
            	// Jenkins provides environment variables that give us context about the build.
            	echo "Job: ${env.JOB_NAME}"
            	echo "Build Number: ${env.BUILD_NUMBER}"
            	echo "Workspace: ${env.WORKSPACE}"
            	// The 'currentBuild' global variable gives us more detailed information about the trigger.
            	echo "Triggered by: ${currentBuild.getBuildCauses()[0].shortDescription}"
            	// For Git-based projects, Jenkins provides specific SCM variables.
            	echo "Branch: ${env.BRANCH_NAME}"
            	echo "Commit: ${env.GIT_COMMIT}"
            	echo "================================================="
        	}
    	}
		
		
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
            echo 'Archiving reports and emailing results for SMOKE suite...'
            
            // This archives the entire reports directory, including both smoke-report.html and index.html
            archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true
            
            // This publishes the index.html file for a perfect view in the Jenkins UI
            publishHTML(
				reportName: 'Smoke Test Report',
				reportDir: 'reports',
				reportFiles: 'index.html',// Correctly pointing to index.html
				keepAll: true,
				alwaysLinkToLastBuild: true,
				allowMissing: true
				)
            
            // All logic involving variables MUST be inside a script block.
            script {
				// Defines the files we will use in this script
				def reportToAttach = 'reports/smoke-report.html'
                def summaryFile = 'reports/smoke-failure-summary.txt'

                def failureSummary = fileExists(summaryFile) ? readFile(summaryFile).trim() : "Check Jenkins console for details."
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
                        attachmentsPattern: reportToAttach
                    )
                }
            }
        }
    }
}