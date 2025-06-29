// A minimal Jenkinsfile for local execution

pipeline {
    // 1. Agent: 'agent any' tells Jenkins to run on the main machine (your local machine in this case).
    agent any

    // We will skip the 'tools' section for now and assume Jenkins can find mvn on its own.

    // 2. Stages: The sequence of steps to execute.
    stages {
        stage('Run Local Smoke Tests') {
            steps {
                echo 'Attempting to run Maven command...'
                // This command executes directly in your project's workspace folder on Jenkins.
                // We use 'bat' for Windows. For Linux or macOS, you would use 'sh'.
                bat 'mvn clean test -P smoke'
            }
        }
    }
}