package com.bridgecrew.services.scan

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
import java.io.File
import java.util.*

data class ScanTaskResult(
        val checkovResult: File,
        val debugOutput: File,
        val errorReason: String
)

class ScanTask(project: Project, title: String, private val scanSource: String, private val processHandler: ProcessHandler, private val scanSourceType: CheckovScanService.ScanSourceType) :
        Task.Backgroundable(project, title, true) {

    private val LOG = logger<ScanTask>()

    private val sourceName = if (scanSourceType == CheckovScanService.ScanSourceType.FILE) { extractFileNameFromPath(scanSource) } else scanSource
    val checkovResultFile: File = File.createTempFile("${sourceName}-checkov-result", ".tmp")
    val debugOutputFile: File = File.createTempFile("${sourceName}-debug-output", ".tmp")
    var errorReason = ""
    //    val checkovOutputBuilder = StringBuilder()
//    val debugOutputBuilder = StringBuilder()
    override fun run(indicator: ProgressIndicator) {
        LOG.info("Going to scan for ${scanSourceType.toString().lowercase()} $scanSource")
        indicator.isIndeterminate = false

        val scanTaskResult: ScanTaskResult = getScanOutputs()

        LOG.info("Checkov scan task finished successfully for ${scanSourceType.toString().lowercase()} $scanSource")

        if (scanSourceType == CheckovScanService.ScanSourceType.FRAMEWORK) {
            project.service<CheckovScanService>().analyzeScan(scanTaskResult, processHandler.exitCode!!, project, scanSource, scanSourceType)
        }

    }

    private fun getScanOutputs(): ScanTaskResult {
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
        if ((text.contains("[ERROR]") || text.contains("Please check your API token") && !text.contains("Please check your API token"))) {
            return text
        }
        return ""
    }
}