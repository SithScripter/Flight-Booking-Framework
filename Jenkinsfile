// Jenkinsfile (Declarative Pipeline for SCM-based CI Smoke Job)

pipeline {
    // 1. Agent: Specifies that this pipeline can run on any available Jenkins agent.
    agent any

    // 2. Tools: Defines tools that must be available on the agent.
    // These names must match what you have configured in Jenkins > Manage Jenkins > Tools.
    tools {
        maven 'apache-maven-3.9.9' // Use the name you configured for your Maven installation
        jdk 'JDK 21'              // Use the name you configured for your JDK installation
    }

    // 3. Stages: The sequence of steps our pipeline will execute.
    stages {
        // Stage 1: A best practice to start with a clean environment for the build.
        stage('Clean Workspace') {
            steps {
                echo 'Cleaning up the workspace...'
                cleanWs() // This is a built-in Jenkins step.
            }
        }

        // Stage 2: This stage checks out the code from the GitHub repository linked to the job.
        stage('Checkout SCM') {
            steps {
                echo 'Checking out code from Source Control (GitHub)...'
                checkout scm
            }
        }

        // Stage 3: This stage runs our tests.
        stage('Build & Run Smoke Tests') {
            steps {
                echo 'Building the project and running the smoke test suite...'
                // We run the default command. Our pom.xml is configured to run the 'smoke'
                // profile by default. We can also add parameters like -Denv=QA.
                bat 'mvn clean test -Denv=QA'
            }
        }

        // Stage 4: This stage saves our test report.
        stage('Archive Test Report') {
            steps {
                // The 'always()' block ensures this step runs even if the 'Test' stage fails.
                // This is crucial so we can see the report for failed test runs.
                always {
                    echo 'Archiving the HTML test report...'
                    archiveArtifacts artifacts: 'reports/extent-report.html', allowEmptyArchive: true
                }
            }
        }
    }
}