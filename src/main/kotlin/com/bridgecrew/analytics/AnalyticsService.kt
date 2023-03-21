package com.bridgecrew.analytics

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit

@Service
class AnalyticsService {
    private val LOG = logger<AnalyticsService>()

    private lateinit var fullScanData: FullScanData
    private var fullScanNumber = 0

    fun fullScanButtonWasPressed() {
        val fullScanButtonWasPressedDate = Date()
        fullScanNumber += 1
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan button was pressed")
        fullScanData = FullScanData(fullScanNumber)
        fullScanData.fullScanButtonPressedTime = fullScanButtonWasPressedDate
    }

    fun fullScanStarted() {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan started")
        fullScanData.fullScanStartedTime = Date()
    }

    fun fullScanByFrameworkStarted(framework: String) {
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan started for framework $framework")
        fullScanData.fullScanFrameworksScanTime[framework] = FullScanFrameworkScanTimeData(Date())
    }

    fun fullScanByFrameworkFinished(framework: String) {
        fullScanData.fullScanFrameworksScanTime[framework]!!.endTime = Date()
        fullScanData.fullScanFrameworksScanTime[framework]!!.totalTimeMinutes = fullScanData.fullScanFrameworksScanTime[framework]!!.endTime.time - fullScanData.fullScanFrameworksScanTime[framework]!!.startTime.time
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan finished for framework $framework and took ${fullScanData.fullScanFrameworksScanTime[framework]!!.totalTimeMinutes} ms")
    }

    fun fullScanFinished() {
        fullScanData.fullScanFinishedTime = Date()
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan finished")
    }

    fun fullScanResultsWereFullyDisplayed() {
        if (fullScanData.isFullScanFinished()) {
            fullScanData.fullScanResultsWereFullyDisplayedTime = Date()
            LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan results are fully displayed")
            logFullScanAnalytics()
        }

    }

    fun fullScanFrameworkError(framework: String) {
        fullScanData.fullScanFrameworkErrors.add(framework)
        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - error while scanning framework $framework")
    }

    private fun logFullScanAnalytics() {
        var maximumScanFramework = 0L
        var minimumScanFramework = 0L
        var maximumFramework = ""
        var minimumFramework = ""
        fullScanData.fullScanFrameworksScanTime.forEach { framework ->
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

        LOG.info("Prisma Plugin Analytics - scan #${fullScanNumber} - full scan analytics:\n" +
                "full scan took ${formatTimeAsString(fullScanData.fullScanButtonPressedTime, fullScanData.fullScanResultsWereFullyDisplayedTime)} minutes from pressing on the scan button to fully display the results\n" +
                "full scan took ${formatTimeAsString(fullScanData.fullScanStartedTime, fullScanData.fullScanFinishedTime)} minutes from starting checkov scans and finishing checkov scans for all frameworks\n" +
                "full scan took ${formatTimeAsString(fullScanData.fullScanButtonPressedTime, fullScanData.fullScanStartedTime)} minutes from pressing on the scan button to starting checkov scan\n" +
                "full scan took ${formatTimeAsString(fullScanData.fullScanFinishedTime, fullScanData.fullScanResultsWereFullyDisplayedTime)} minutes from finishing checkov scans for all frameworks to fully display the results\n" +
                "framework scan $maximumFramework took the most - ${formatTimeAsString(fullScanData.fullScanFrameworksScanTime[maximumFramework]!!.startTime, fullScanData.fullScanFrameworksScanTime[maximumFramework]!!.endTime)} minutes\n" +
                "framework scan $minimumFramework took the least - ${formatTimeAsString(fullScanData.fullScanFrameworksScanTime[minimumFramework]!!.startTime, fullScanData.fullScanFrameworksScanTime[minimumFramework]!!.endTime)} minutes\n" +
                "${fullScanData.fullScanFrameworkErrors.size} frameworks was finished with errors: ${fullScanData.fullScanFrameworkErrors}\n" +
                "frameworks scans:\n ${
                    fullScanData.fullScanFrameworksScanTime.map { framework ->
                        "framework ${framework.key} took ${formatTimeAsString(framework.value.startTime, framework.value.endTime)} minutes to be scanned\n"
                    }
                }\n" +
                "full scan button pressed on ${dateFormatter.format(fullScanData.fullScanButtonPressedTime)}\n" +
                "full scan button pressed on ${dateFormatter.format(fullScanData.fullScanButtonPressedTime)}\n" +
                "full scan started on ${dateFormatter.format(fullScanData.fullScanStartedTime)}\n" +
                "full scan finished on ${dateFormatter.format(fullScanData.fullScanFinishedTime)}\n" +
                "full scan results displayed on ${dateFormatter.format(fullScanData.fullScanResultsWereFullyDisplayedTime)}\n"
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

    data class FullScanData(val scanNumber: Int) {
        lateinit var fullScanButtonPressedTime: Date
        lateinit var fullScanStartedTime: Date
        val fullScanFrameworksScanTime: MutableMap<String, FullScanFrameworkScanTimeData> = mutableMapOf()
        lateinit var fullScanFinishedTime: Date
        lateinit var fullScanResultsWereFullyDisplayedTime: Date
        val fullScanFrameworkErrors = mutableSetOf<String>()

        fun isFullScanFinished() = ::fullScanFinishedTime.isInitialized
    }

    data class FullScanFrameworkScanTimeData(val startTime: Date) {
        var endTime: Date = Date()
        var totalTimeMinutes = 0L
    }
}