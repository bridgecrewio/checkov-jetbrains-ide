package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.errors.CheckovErrorHandlerService
import com.bridgecrew.utils.DEFAULT_TIMEOUT
import com.bridgecrew.utils.extractFileNameFromPath
import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.workspaceModel.ide.impl.jps.serialization.FileInDirectorySourceNames
import java.io.File

data class ScanTaskResult(
        val checkovResult: File,
        val debugOutput: File,
        val errorReason: String
)

abstract class ScanTask(project: Project, title: String, private val sourceName: String, private val processHandler: ProcessHandler, private val scanSourceType: CheckovScanService.ScanSourceType) :
        Task.Backgroundable(project, title, true) {

    protected val LOG = logger<ScanTask>()

//    private val sourceName = if (scanSourceType == CheckovScanService.ScanSourceType.FILE) {
//        extractFileNameFromPath(scanSource)
//    } else scanSource
    val checkovResultFile: File = File.createTempFile("${sourceName}-checkov-result", ".tmp")
    val debugOutputFile: File = File.createTempFile("${sourceName}-debug-output", ".tmp")
    var errorReason = ""

//    override fun run(indicator: ProgressIndicator) {
//        try {
//            LOG.info("Going to scan for ${scanSourceType.toString().lowercase()} $scanSource")
//            indicator.isIndeterminate = false
//
//            val scanTaskResult: ScanTaskResult = getScanOutputs()
//
//            LOG.info("Checkov scan task finished successfully for ${scanSourceType.toString().lowercase()} $scanSource")
//
//            if (scanSourceType == CheckovScanService.ScanSourceType.FRAMEWORK) {
//                project.service<AnalyticsService>().fullScanByFrameworkFinished(scanSource)
//            }
//            project.service<CheckovScanService>().analyzeScan(scanTaskResult, processHandler.exitCode!!, project, scanSource, scanSourceType)
//
//        } catch (error: Exception) {
//            LOG.error("error while scanning ${scanSourceType.toString().lowercase()} $scanSource", error)
//            if (scanSourceType == CheckovScanService.ScanSourceType.FRAMEWORK) {
//                project.service<FullScanStateService>().fullScanFrameworkFinished(project, scanSource)
//                project.service<FullScanStateService>().frameworkFinishedWithErrors(scanSource, ScanTaskResult(checkovResultFile, debugOutputFile, errorReason))
//            }
//            throw error
//        }
//
//    }

    protected fun getScanOutputs(): ScanTaskResult {
        LOG.assertTrue(!processHandler.isStartNotified)

        processHandler.addProcessListener(object : ProcessAdapter() {

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                if (outputType == ProcessOutputTypes.SYSTEM) {
                    return
                }

                val text = event.text

                if (outputType == ProcessOutputTypes.STDOUT) {
                    checkovResultFile.appendText(text)
                } else if (outputType == ProcessOutputTypes.STDERR) {
                    debugOutputFile.appendText(text)
                    errorReason = updateErrorReason(text)
                }

                LOG.debug(text)
            }
        })

        processHandler.startNotify()
        if (!processHandler.waitFor(DEFAULT_TIMEOUT)) {
            throw ExecutionException("Script execution took more than ${(DEFAULT_TIMEOUT / 1000)} seconds")
        }

        return ScanTaskResult(checkovResultFile, debugOutputFile, errorReason)
    }

    private fun updateErrorReason(text: String): String {
       if (text.contains("[ERROR]")) {
           return text.substring(text.indexOf("ERROR"))
       }

        if (text.contains("Please check your API token")) {
            return text
        }

        return ""
    }

    class FrameworkScanTask(project: Project, title: String, private val framework: String, private val processHandler: ProcessHandler):
            ScanTask(project, title, framework, processHandler, CheckovScanService.ScanSourceType.FRAMEWORK) {
        override fun run(indicator: ProgressIndicator) {
            try {
                LOG.info("Going to scan for framework $framework")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs()

                LOG.info("Checkov scan task finished successfully for framework $framework")

                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)

                project.service<CheckovScanService>().analyzeFrameworkScan(scanTaskResult, processHandler.exitCode!!, project, framework)

            } catch (error: Exception) {
                LOG.error("error while scanning framework $framework", error)
                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)

//                project.service<FullScanStateService>().fullScanFrameworkFinished(framework)
                project.service<FullScanStateService>().frameworkFinishedWithErrors(framework, ScanTaskResult(checkovResultFile, debugOutputFile, errorReason))
                throw error
            }
        }
    }

    class FileScanTask(project: Project, title: String, private val filePath: String, private val processHandler: ProcessHandler):
            ScanTask(project, title, extractFileNameFromPath(filePath), processHandler, CheckovScanService.ScanSourceType.FILE) {
        override fun run(indicator: ProgressIndicator) {
            try {
                LOG.info("Going to scan for framework $filePath")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs()

                LOG.info("Checkov scan task finished successfully for file $filePath")

                project.service<CheckovScanService>().analyzeFileScan(scanTaskResult, processHandler.exitCode!!, project, filePath)

            } catch (error: Exception) {
                LOG.error("error while scanning file $filePath", error)
                throw error
            }
        }
    }
}