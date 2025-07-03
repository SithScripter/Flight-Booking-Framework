post {
    always {
        echo 'Archiving and publishing the full HTML report folder...'

        // ✅ Corrected bat block with multiline Windows commands
        bat '''
        if exist target\\extent-report.html (
            mkdir reports
            copy target\\extent-report.html reports\\smoke-report.html
            copy target\\*.css reports\\
            copy target\\*.js reports\\
            copy target\\*.png reports\\
        )
        '''

        // ✅ Archive complete reports folder
        archiveArtifacts artifacts: 'reports/**', allowEmptyArchive: true

        // ✅ Publish report in Jenkins UI
        publishHTML(
            reportName: 'Smoke Test Report',
            reportDir: 'reports',
            reportFiles: 'smoke-report.html',
            keepAll: true,
            alwaysLinkToLastBuild: true,
            allowMissing: true
        )

        // ✅ Email with link and attachment
        script {
            def emailSubject
            def emailBody
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
                    attachmentsPattern: 'reports/smoke-report.html'
                )
            }
        }
    }
}
