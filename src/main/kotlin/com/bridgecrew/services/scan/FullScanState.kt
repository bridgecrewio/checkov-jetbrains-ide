package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

@Service
class FullScanStateService(val project: Project) {
    private var fullScanFinishedFrameworksNumber: Int = 0
        set(value) {
            field = value
            if (value == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN) {
                project.service<AnalyticsService>().fullScanFinished()
                displayNotificationForFullScanSummary()
            }

        }

    var frameworkScansFinishedWithErrors = mutableMapOf<String, ScanTaskResult>()
    var invalidFilesSize: Int = 0
    var frameworkScansFinishedWithNoVulnerabilities = mutableSetOf<String>()
    var unscannedFrameworks = mutableSetOf<String>()
    var totalPassedCheckovChecks: Int = 0
    var totalFailedCheckovChecks: Int = 0

    fun fullScanStarted() {
        fullScanFinishedFrameworksNumber = 0
    }

    fun frameworkScanFinishedAndDetectedIssues(framework: String, numberOfIssues: Int) {
        project.service<AnalyticsService>().fullScanFrameworkDetectedVulnerabilities(framework, numberOfIssues)

//        project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)
        fullScanFinishedFrameworksNumber++
//        if (fullScanFinishedFrameworksNumber == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN) {
////            val totalErrors = project.service<ResultsCacheService>().getAllCheckovResults().size
////            val message = "Checkov has detected $totalErrors configuration errors in your project. Check out the tool window to analyze your code"
////            CheckovNotificationBalloon.showNotification(project, message, NotificationType.INFORMATION)
//            project.service<AnalyticsService>().fullScanFinished()
//        }
    }

    fun frameworkFinishedWithNoErrors(framework: String) {
        frameworkScansFinishedWithNoVulnerabilities.add(framework)
        project.service<AnalyticsService>().fullScanFrameworkFinishedNoErrors(framework)
        fullScanFinishedFrameworksNumber++

    }

    fun frameworkFinishedWithErrors(framework: String, scanTaskResult: ScanTaskResult) {
        frameworkScansFinishedWithErrors[framework] = scanTaskResult
        project.service<AnalyticsService>().fullScanFrameworkError(framework)
        fullScanFinishedFrameworksNumber++
    }

    fun parsingErrorsFoundInFiles(framework: String, failedFilesSize: Int) {
        invalidFilesSize += failedFilesSize
        project.service<AnalyticsService>().fullScanParsingError(framework, failedFilesSize)
    }

    fun frameworkWasNotScanned(framework: String) {
        unscannedFrameworks.add(framework)
        fullScanFinishedFrameworksNumber++
    }

    fun displayNotificationForFullScanSummary() {
        val totalErrors = project.service<ResultsCacheService>().getAllCheckovResults().size
        var message = "Checkov has detected $totalErrors configuration errors in your project.\n" +
                "Check out the tool window to analyze your code.\n" +
                "${DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN} frameworks were scanned:\n" +
                "Scans for frameworks ${frameworkScansFinishedWithErrors.keys} were finished with errors.\n" +
                "Please check the log files in:\n" +
                "[${frameworkScansFinishedWithErrors.map { (framework, scanResults) -> "$framework:\n" +
                        "log file - ${scanResults.debugOutput.path}\n" +
                        "checkov result - ${scanResults.checkovResult.path}\n" }}]\n" +
                "${invalidFilesSize}} files were detected as invalid:\n" +
                "No errors have been detected for frameworks $frameworkScansFinishedWithNoVulnerabilities :)\n"

        if (unscannedFrameworks.isNotEmpty()) {
            message += "Frameworks $unscannedFrameworks were not scanned because they are probably not installed.\n"

        }

        CheckovNotificationBalloon.showNotification(project,
                message,
                NotificationType.INFORMATION)

    }
}