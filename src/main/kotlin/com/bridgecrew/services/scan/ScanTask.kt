package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.utils.DEFAULT_TIMEOUT
import com.bridgecrew.utils.createCheckovTempFile
import com.bridgecrew.utils.extractFileNameFromPath
import com.intellij.execution.ExecutionException
import com.intellij.execution.process.ProcessAdapter
import com.intellij.execution.process.ProcessEvent
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ProcessOutputTypes
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.util.Key
import java.io.File

data class ScanTaskResult(
        val checkovResult: File,
        val debugOutput: File,
        val errorReason: String)
{
    fun deleteResultsFile() {
        if (checkovResult.exists())
            checkovResult.delete()
        if (debugOutput.exists())
            debugOutput.delete()
    }
}

abstract class ScanTask(project: Project, title: String, private val sourceName: String, private val processHandler: ProcessHandler, val checkovResultFile: File) :
        Task.Backgroundable(project, title, true) {

    protected val LOG = logger<ScanTask>()

    val debugOutputFile: File = createCheckovTempFile("${sourceName}-debug-output", ".txt")
    var errorReason = ""

    protected var indicator: ProgressIndicator? = null

    protected fun getScanOutputs(): ScanTaskResult {
        LOG.assertTrue(!processHandler.isStartNotified)

        processHandler.addProcessListener(object : ProcessAdapter() {

            override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                try {
                    if (processHandler.isProcessTerminated || processHandler.isProcessTerminating) {
                        LOG.info("Process is terminating for $sourceName")
                        return
                    }

                    indicator!!.checkCanceled()

                    if (outputType == ProcessOutputTypes.SYSTEM) {
                        return
                    }

                    val text = event.text

                    if (outputType == ProcessOutputTypes.STDERR) {
                        debugOutputFile.appendText(text)
                        errorReason = updateErrorReason(text)
                    }

                    LOG.debug(text)
                } catch (e: ProcessCanceledException) {
                    LOG.info("Process was canceled for $sourceName during onTextAvailable, destroying process for processHandler", e)
                    processHandler.destroyProcess()
                }

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

    fun cancelTask() {
        if (this.indicator != null) {
            LOG.info("Going to cancel task for $sourceName")
            this.indicator!!.cancel()
            ProgressManager.canceled(indicator!!)
            processHandler.destroyProcess()
            deleteResultsFile()
            LOG.info("Task was canceled for $sourceName")
        }
    }

    fun deleteResultsFile() {
        if (checkovResultFile.exists())
            checkovResultFile.delete()
        if (debugOutputFile.exists())
            debugOutputFile.delete()
    }

    class FrameworkScanTask(project: Project, title: String, val framework: String, private val processHandler: ProcessHandler, checkovResultOutputFile: File) :
            ScanTask(project, title, framework, processHandler, checkovResultOutputFile), ProjectManagerListener {

        override fun run(indicator: ProgressIndicator) {
            try {
                this.indicator = indicator
                checkOnCancel()
                LOG.info("Going to scan for framework $framework")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs()
                indicator.checkCanceled()

                LOG.info("Checkov scan task finished successfully for framework $framework")

                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)

                project.service<CheckovScanService>().analyzeFrameworkScan(scanTaskResult, processHandler.exitCode!!, project, framework)

            } catch (e: ProcessCanceledException) {
                LOG.info("Task for framework $framework was canceled ", e)
                processHandler.destroyProcess()
                deleteResultsFile()
                project.service<FullScanStateService>().frameworkWasCancelled()
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(CheckovScanService.ScanSourceType.FRAMEWORK)
            } catch (error: Exception) {
                LOG.error("error while scanning framework $framework", error)
                project.service<AnalyticsService>().fullScanByFrameworkFinished(framework)
                project.service<FullScanStateService>().frameworkFinishedWithErrors(framework, ScanTaskResult(checkovResultFile, debugOutputFile, errorReason))
                throw error
            }
        }

        fun checkOnCancel() {
            if (project.service<FullScanStateService>().onCancel) {
                throw ProcessCanceledException(Exception("Could not start process on cancel state"))
            }
            indicator!!.checkCanceled()
        }
    }

    class FileScanTask(project: Project, title: String, private val filePath: String, private val processHandler: ProcessHandler, checkovResultFile: File):
            ScanTask(project, title, extractFileNameFromPath(filePath), processHandler, checkovResultFile) {
        override fun run(indicator: ProgressIndicator) {
            try {

                this.indicator = indicator
                indicator.checkCanceled()
                LOG.info("Going to scan for framework $filePath")
                indicator.isIndeterminate = false

                val scanTaskResult: ScanTaskResult = getScanOutputs()
                indicator.checkCanceled()

                LOG.info("Checkov scan task finished successfully for file $filePath")

                project.service<CheckovScanService>().analyzeFileScan(scanTaskResult, processHandler.exitCode!!, project, filePath)

            } catch (e: ProcessCanceledException) {
                LOG.info("Task for file $filePath was canceled", e)
                processHandler.destroyProcess()
                deleteResultsFile()
            }
            catch (error: Exception) {
                LOG.error("error while scanning file $filePath", error)
                throw error
            }
        }
    }
}