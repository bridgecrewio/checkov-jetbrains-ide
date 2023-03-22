package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.errors.CheckovErrorHandlerService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.utils.CheckovResultExtractionData
import com.bridgecrew.utils.CheckovUtils
import com.bridgecrew.utils.DEFAULT_TIMEOUT
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.nio.charset.Charset
import javax.swing.SwingUtilities

private val LOG = logger<CheckovScanService>()

@Service
class CheckovScanService {
    var selectedCheckovScanner: CheckovScanCommandsService? = null
    private val settings = CheckovSettingsState().getInstance()

    fun scanFile(filePath: String, project: Project) {
        
        try {
            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan a file using $selectedCheckovScanner")

            val execCommand = prepareExecCommand(filePath)
            val generalCommandLine = generateCheckovCommand(execCommand)

            val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
            val scanTask = ScanTask(project, "Checkov scanning file $filePath", filePath, processHandler, ScanSourceType.FILE)

            if (SwingUtilities.isEventDispatchThread()) {
                ProgressManager.getInstance().run(scanTask)
            } else {
                ApplicationManager.getApplication().invokeLater {
                    ProgressManager.getInstance().run(scanTask)
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
            return
        }
    }

    fun scanProject(project: Project) {
        try {
            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan the project $selectedCheckovScanner")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).projectScanningStarted()

            project.service<ResultsCacheService>().deleteAllCheckovResults() // TODO - save the previous state for the case where the client cancels the

            val execCommands: List<List<String>> = prepareRepositoryScanningExecCommand()

            project.service<FullScanStateService>().fullScanStarted()
            project.service<AnalyticsService>().fullScanStarted()

            execCommands.forEach { execCommand ->
                run {
                    val processHandler: ProcessHandler = OSProcessHandler(generateCheckovCommand(execCommand))

                    val frameworkIndex = execCommand.indexOf("--framework") + 1
                    val framework = execCommand[frameworkIndex]
                    val scanTask = ScanTask(project, "Checkov scanning repository by framework $framework", framework, processHandler, ScanSourceType.FRAMEWORK)
                    project.service<AnalyticsService>().fullScanByFrameworkStarted(framework)
                    if (SwingUtilities.isEventDispatchThread()) {
                        ProgressManager.getInstance().run(scanTask)
                    } else {
                        ApplicationManager.getApplication().invokeLater {
                            ProgressManager.getInstance().run(scanTask)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            LOG.error(e)
            return
        }
    }

    fun cancelScan() {
        print("canceling run")
        CheckovScanAction.resetActionDynamically(true)
    }

    private fun generateCheckovCommand(execCommand: List<String>): GeneralCommandLine {
        val pluginVersion = PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version
                ?: "UNKNOWN"
        val prismaUrl = settings?.prismaURL

        val generalCommandLine = GeneralCommandLine(execCommand)
        generalCommandLine.charset = Charset.forName("UTF-8")
        generalCommandLine.environment["BC_SOURCE_VERSION"] = pluginVersion
        generalCommandLine.environment["BC_SOURCE"] = "jetbrains"
        generalCommandLine.environment["LOG_LEVEL"] = "DEBUG"
        if (!prismaUrl.isNullOrEmpty()) {
            generalCommandLine.environment["PRISMA_API_URL"] = prismaUrl
        }

        return generalCommandLine
    }

    private fun prepareExecCommand(filePath: String): List<String> {
        val execCommand = selectedCheckovScanner!!.getExecCommandForSingleFile(filePath) + getCertParams()

        val maskedCommand = replaceApiToken(execCommand.joinToString(" "))
        LOG.info("Running command: $maskedCommand")

        return execCommand
    }

    private fun prepareRepositoryScanningExecCommand(): List<List<String>> {
        val execCommandsByFramework = selectedCheckovScanner!!.getExecCommandsForRepositoryByFramework()

        execCommandsByFramework.forEach { command ->
            run {
                command.addAll(getCertParams())
                val maskedCommand = replaceApiToken(command.joinToString(" "))
                LOG.info("Running command: $maskedCommand")
            }
        }

        return execCommandsByFramework
    }

    private fun getCertParams(): ArrayList<String> {
        val cmds = ArrayList<String>()
        val certPath = settings?.certificate
        if (!certPath.isNullOrEmpty()) {
            cmds.add("-ca")
            cmds.add(certPath)
            return cmds
        }
        return cmds
    }

    private fun replaceApiToken(command: String): String {
        val apiToknIndex = command.indexOf("--bc-api-key")
        return if (apiToknIndex >= 0) {
            val firstPos: Int = apiToknIndex + "--bc-api-key".length
            val lastPos: Int = command.indexOf("--repo-id", firstPos)
            command.substring(0, firstPos).toString() + " **-**-**-** " + command.substring(lastPos)
        } else {
            command
        }
    }
    private fun analyzeScan(result: String, errorCode: Int, project: Project, scanningSource: String, scanSourceType: ScanSourceType) {
        if (!isValidScanResults(result, errorCode, scanningSource, scanSourceType, project)) {
            return
        }

        try {
            val extractionResult: CheckovResultExtractionData = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(result, scanningSource)

            if (extractionResult.parsingErrors.isNotEmpty()) {
                project.service<CheckovErrorHandlerService>().scanningParsingError(result, scanningSource, extractionResult.parsingErrors, scanSourceType)
            }

            if (extractionResult.failedChecks.isEmpty()) {
                CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished, no errors have been detected for ${scanSourceType.toString().lowercase()}: $scanningSource", NotificationType.INFORMATION)
                LOG.info("Checkov scanning finished, No errors have been detected for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}")
                return
            }

            project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
            CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}, please check the results panel.", NotificationType.INFORMATION)
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(scanSourceType)

        } catch (error: Exception) {
            project.service<CheckovErrorHandlerService>().scanningError(result, scanningSource, error, scanSourceType)
        }
    }

    private fun isValidScanResults(result: String, errorCode: Int, scanningSource: String, scanSourceType: ScanSourceType, project: Project): Boolean {
        if (result.contains("Please check your API token")) {
            project.service<CheckovErrorHandlerService>().scanningError(result, scanningSource, Exception("Please check your API token"), scanSourceType)

            LOG.error("Please check you API token\n\n")
            return false
        }
        if (errorCode != 0 || result.contains("[ERROR]")) {
            project.service<CheckovErrorHandlerService>().scanningError(result, scanningSource, Exception("Error while scanning $scanningSource, exit code - $errorCode"), scanSourceType)
            return false
        }

        return true
    }

    private class ScanTask(project: Project, title: String, val scanSource: String, val processHandler: ProcessHandler, val scanSourceType: ScanSourceType) :
            Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            LOG.info("Going to scan for ${scanSourceType.toString().lowercase()} $scanSource")
            indicator.isIndeterminate = false
            val output = ScriptRunnerUtil.getProcessOutput(processHandler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    DEFAULT_TIMEOUT)

            LOG.info("Checkov scan task finished successfully for ${scanSourceType.toString().lowercase()} $scanSource")

            if (scanSourceType == ScanSourceType.FRAMEWORK) {
                project.service<FullScanStateService>().fullScanFrameworkFinished(project, scanSource)
            }

            project.service<CheckovScanService>().analyzeScan(output, processHandler.exitCode!!, project, scanSource, scanSourceType)

        }
    }

    enum class ScanSourceType {
        FILE,
        FRAMEWORK
    }
}



