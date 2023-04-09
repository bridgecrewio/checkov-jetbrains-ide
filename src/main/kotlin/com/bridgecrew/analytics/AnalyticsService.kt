package com.bridgecrew.analytics

import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.services.scan.ScanTaskResult
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class AnalyticsService(val project: Project) {

    private val LOG = logger<AnalyticsService>()

    var fullScanData: FullScanAnalyticsData? = null
    private var fullScanNumber = 0

    fun fullScanButtonWasPressed() {
        val fullScanButtonWasPressedDate = Date()
        fullScanNumber += 1
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan button was pressed")
        fullScanData = FullScanAnalyticsData(fullScanNumber)
        fullScanData!!.buttonPressedTime = fullScanButtonWasPressedDate
    }

    fun fullScanStarted() {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan started")
        fullScanData!!.scanStartedTime = Date()
    }

    fun fullScanByFrameworkStarted(framework: String) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan started for framework $framework")
        fullScanData!!.frameworksScanTime[framework] = FullScanFrameworkScanTimeData(Date())
    }

    fun fullScanByFrameworkFinished(framework: String) {
        fullScanData!!.frameworksScanTime[framework]!!.endTime = Date()
        fullScanData!!.frameworksScanTime[framework]!!.totalTimeMinutes = fullScanData!!.frameworksScanTime[framework]!!.endTime.time - fullScanData!!.frameworksScanTime[framework]!!.startTime.time
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan finished for framework $framework and took ${fullScanData!!.frameworksScanTime[framework]!!.totalTimeMinutes} ms")
    }

    fun fullScanFinished() {
        fullScanData!!.scanFinishedTime = Date()
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan finished")
    }

    fun fullScanFrameworkFinishedNoErrors(framework: String) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - framework $framework finished with no errors")
    }

    fun fullScanResultsWereFullyDisplayed() {
        if (fullScanData!!.isFullScanFinished()) {
            fullScanData!!.resultsWereFullyDisplayedTime = Date()
            LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan results are fully displayed")
            logFullScanAnalytics()
        }
    }

    fun fullScanFrameworkError(framework: String) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - error while scanning framework $framework")
    }

    fun fullScanFrameworkDetectedVulnerabilities(framework: String, numberOfVulnerabilities: Int) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - $numberOfVulnerabilities security issues were detected while scanning framework $framework")
    }

    fun fullScanParsingError(framework: String, failedFilesSize: Int) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - parsing error while scanning framework $framework in $failedFilesSize files}")
    }

    private fun logFullScanAnalytics() {
        var maximumScanFramework = 0L
        var minimumScanFramework = 0L
        var maximumFramework = ""
        var minimumFramework = ""
        fullScanData!!.frameworksScanTime.forEach { framework ->
            if (framework.value.totalTimeMinutes >= maximumScanFramework) {
                maximumScanFramework = framework.value.totalTimeMinutes
                maximumFramework = framework.key
                if (minimumFramework.isEmpty()) {
                    minimumScanFramework = framework.value.totalTimeMinutes
                    minimumFramework = framework.key
                }
            }

            if (framework.value.totalTimeMinutes <= minimumScanFramework) {
                minimumScanFramework = framework.value.totalTimeMinutes
                minimumFramework = framework.key
            }
        }

        val dateFormatter = SimpleDateFormat("dd/M/yyyy hh:mm:ss")

        val frameworkScansFinishedWithErrors: MutableMap<String, ScanTaskResult> = project.service<FullScanStateService>().frameworkScansFinishedWithErrors

        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan analytics:\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.buttonPressedTime, fullScanData!!.resultsWereFullyDisplayedTime)} minutes from pressing on the scan button to fully display the results\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.scanStartedTime, fullScanData!!.scanFinishedTime)} minutes from starting checkov scans and finishing checkov scans for all frameworks\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.buttonPressedTime, fullScanData!!.scanStartedTime)} minutes from pressing on the scan button to starting checkov scan\n" +
                "full scan took ${formatTimeAsString(fullScanData!!.scanFinishedTime, fullScanData!!.resultsWereFullyDisplayedTime)} minutes from finishing checkov scans for all frameworks to fully display the results\n" +
                "framework scan $maximumFramework took the most - ${formatTimeAsString(fullScanData!!.frameworksScanTime[maximumFramework]!!.startTime, fullScanData!!.frameworksScanTime[maximumFramework]!!.endTime)} minutes\n" +
                "framework scan $minimumFramework took the least - ${formatTimeAsString(fullScanData!!.frameworksScanTime[minimumFramework]!!.startTime, fullScanData!!.frameworksScanTime[minimumFramework]!!.endTime)} minutes\n" +
                "${frameworkScansFinishedWithErrors.size} frameworks was finished with errors: ${frameworkScansFinishedWithErrors.keys}\n" +
                "frameworks scans:\n" +
                "${fullScanData!!.frameworksScanTime.map { (framework, scanResults) ->
                        "framework $framework took ${formatTimeAsString(scanResults.startTime, scanResults.endTime)} minutes to be scanned\n" }
                }\n" +
                "full scan button pressed on ${dateFormatter.format(fullScanData!!.buttonPressedTime)}\n" +
                "full scan started on ${dateFormatter.format(fullScanData!!.scanStartedTime)}\n" +
                "full scan finished on ${dateFormatter.format(fullScanData!!.scanFinishedTime)}\n" +
                "full scan results displayed on ${dateFormatter.format(fullScanData!!.resultsWereFullyDisplayedTime)}\n"
        )
    }

    private fun formatTimeAsString(startTime: Date, endTime: Date): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(endTime.time - startTime.time)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(endTime.time - startTime.time) - (minutes * 60)
        val secondsString = if (seconds < 10) {
            "0${seconds}"
        } else "${seconds}"
        return "${minutes}:${secondsString}"
    }

    data class FullScanAnalyticsData(val scanNumber: Int) {
        lateinit var buttonPressedTime: Date
        lateinit var scanStartedTime: Date
        val frameworksScanTime: MutableMap<String, FullScanFrameworkScanTimeData> = mutableMapOf()
        lateinit var scanFinishedTime: Date
        lateinit var resultsWereFullyDisplayedTime: Date

        fun isFullScanFinished() = ::scanFinishedTime.isInitialized
        fun isFullScanStarted() = ::scanStartedTime.isInitialized
    }

    data class FullScanFrameworkScanTimeData(val startTime: Date) {
        var endTime: Date = Date()
        var totalTimeMinutes = 0L
    }
}