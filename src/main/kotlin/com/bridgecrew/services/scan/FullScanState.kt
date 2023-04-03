package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.utils.DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
import com.bridgecrew.utils.FULL_SCAN_STATE_FILE
import com.bridgecrew.utils.createCheckovTempFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.json.JSONArray
import java.io.File

@Service
class FullScanStateService(val project: Project) {
    private var fullScanFinishedFrameworksNumber: Int = 0
        set(value) {
            field = value
            if (value == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN) {
                handleFullScanFinished()

            }
        }

    var frameworkScansFinishedWithErrors = mutableMapOf<String, ScanTaskResult>()
    var invalidFilesSize: Int = 0
    var frameworkScansFinishedWithNoVulnerabilities = mutableSetOf<String>()
    var unscannedFrameworks = mutableSetOf<String>()
    var totalPassedCheckovChecks: Int = 0
    var totalFailedCheckovChecks: Int = 0

    private var stateFile: File? = null
    var onCancel: Boolean = false
    var previousState = if (project.service<ResultsCacheService>().checkovResults.size > 0) State.SUCCESSFUL_SCAN else State.FIRST_TIME_SCAN

    private val gson = Gson()
    private val LOG = logger<FullScanStateService>()

    private fun handleFullScanFinished() {
        if(!onCancel) {
            if (wereAllFrameworksFinishedWithErrors()) {
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).fullScanFailed()
                previousState = State.FAILED_SCAN
                CheckovScanAction.resetActionDynamically(true)
            } else {
                displayNotificationForFullScanSummary()
                previousState = State.SUCCESSFUL_SCAN
            }
        } else {
            returnToPreviousState()
        }

        deletePreviousState()
        project.service<AnalyticsService>().fullScanFinished()
    }

    fun fullScanStarted() {
        fullScanFinishedFrameworksNumber = 0
        frameworkScansFinishedWithErrors.clear()
        invalidFilesSize = 0
        frameworkScansFinishedWithNoVulnerabilities.clear()
        unscannedFrameworks.clear()
        totalPassedCheckovChecks = 0
        totalFailedCheckovChecks = 0
        onCancel = false
    }

    fun saveCurrentState() {
        val currentResults: List<BaseCheckovResult> = project.service<ResultsCacheService>().getAllCheckovResults()
        stateFile = createCheckovTempFile(FULL_SCAN_STATE_FILE, ".json")

        val resultsAsJson = JSONArray(currentResults)
        stateFile!!.writeText(resultsAsJson.toString())
    }

    fun returnToPreviousState() {
        try {
            val stateContent = stateFile!!.readText()
            val resultsListType = object : TypeToken<List<BaseCheckovResult>>() {}.type
            val checkovResultsList: MutableList<BaseCheckovResult> = gson.fromJson(stateContent, resultsListType)
            project.service<ResultsCacheService>().checkovResults = checkovResultsList
        } catch (e: Exception) {
            LOG.warn("Could not restore previous state from file, clearing the list", e)
        }
    }

    private fun deletePreviousState() {
        try {
            stateFile!!.delete()
        } catch (e: Exception) {
            LOG.warn("Could not delete previous state file in $stateFile")
        }
    }

    fun frameworkWasCancelled() {
        fullScanFinishedFrameworksNumber++
    }

    fun frameworkScanFinishedAndDetectedIssues(framework: String, numberOfIssues: Int) {
        project.service<AnalyticsService>().fullScanFrameworkDetectedVulnerabilities(framework, numberOfIssues)

        fullScanFinishedFrameworksNumber++
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

    fun wereAllFrameworksFinished(): Boolean {
        return fullScanFinishedFrameworksNumber == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
    }

    fun wereAllFrameworksFinishedWithErrors(): Boolean {
        return frameworkScansFinishedWithErrors.size == DESIRED_NUMBER_OF_FRAMEWORK_FOR_FULL_SCAN
    }

    enum class State() {
        FIRST_TIME_SCAN,
        SUCCESSFUL_SCAN,
        FAILED_SCAN
    }
}