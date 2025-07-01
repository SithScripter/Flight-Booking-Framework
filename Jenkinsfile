pipeline {
    agent any

    // 1. DEFINE PARAMETERS
    // This block tells Jenkins to create a form on the "Build with Parameters" page.
    parameters {
        // We are creating a 'Choice' parameter (a dropdown menu)
        choice(
            name: 'TARGET_ENVIRONMENT', // The variable name for our parameter.
            choices: ['PRODUCTION', 'QA', 'STAGING'],// The options in the dropdown. 'PRODUCTION' is the default.
            description: 'Select the target environment to run the tests against.'
        )
    }

    // Tools section using your specific configured names
    tools {
        maven 'apache-maven-3.9.9'
        jdk 'JDK 21'
    }

    stages {
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning up the workspace...'
                cleanWs()
            }
        }

        stage('Checkout SCM') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build & Run Smoke Tests') {
            steps {
                echo "Running tests on environment selected by user: ${params.TARGET_ENVIRONMENT}"

                // 2. USE THE PARAMETER
                // Instead of a hardcoded "QA", we use the 'params' object to get the user's selection.
                bat "mvn clean test -P smoke -Denv=${params.TARGET_ENVIRONMENT}"
            }
        }
        
        stage('Archive Smoke Report') {
            steps {
                always {
                    echo 'Preparing and archiving the HTML test report...'
                    
                    // Step 1: Rename the generic report to a specific one.
                    // This is the correct and reliable way to handle renaming.
                    bat 'if exist reports\\extent-report.html (move reports\\extent-report.html reports\\smoke-report.html)'
                    
                    // Step 2: Archive the newly named file.
                    archiveArtifacts artifacts: 'reports/smoke-report.html', allowEmptyArchive: true
                }
            }
        }
    }
}