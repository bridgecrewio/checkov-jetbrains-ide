package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.errors.CheckovErrorHandlerService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.utils.CheckovResultExtractionData
import com.bridgecrew.utils.CheckovUtils
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressManager
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

            val processHandler: ProcessHandler = OSProcessHandler.Silent(generalCommandLine)
            val scanTask = ScanTask.FileScanTask(project, "Checkov scanning file $filePath", filePath, processHandler)

            ApplicationManager.getApplication().executeOnPooledThread {
                kotlin.run {
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
                    val processHandler: ProcessHandler = OSProcessHandler.Silent(generateCheckovCommand(execCommand))

                    val frameworkIndex = execCommand.indexOf("--framework") + 1
                    val framework = execCommand[frameworkIndex]
                    val scanTask = ScanTask.FrameworkScanTask(project, "Checkov scanning repository by framework $framework", framework, processHandler)
                    project.service<AnalyticsService>().fullScanByFrameworkStarted(framework)
                    ApplicationManager.getApplication().executeOnPooledThread {
                        kotlin.run {
                            if (SwingUtilities.isEventDispatchThread()) {
                                ProgressManager.getInstance().run(scanTask)
                            } else {
                                ApplicationManager.getApplication().invokeLater {
                                    ProgressManager.getInstance().run(scanTask)
                                }
                            }
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

    fun analyzeFrameworkScan(scanTaskResult: ScanTaskResult, errorCode: Int, project: Project, framework: String) {
        if (!isValidScanResults(scanTaskResult, errorCode, framework, ScanSourceType.FRAMEWORK, project)) {
            return
        }

        try {
            val extractionResult: CheckovResultExtractionData = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(scanTaskResult.checkovResult.readText(), framework)

            if (extractionResult.parsingErrorsSize > 0) {
                project.service<FullScanStateService>().parsingErrorsFoundInFiles(framework, extractionResult.parsingErrorsSize)
            }

            if (extractionResult.failedChecks.isEmpty()) {
                project.service<FullScanStateService>().frameworkFinishedWithNoErrors(framework)
            } else {
                project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(ScanSourceType.FRAMEWORK)

                project.service<FullScanStateService>().frameworkScanFinishedAndDetectedIssues(framework, extractionResult.failedChecks.size)
            }

            project.service<FullScanStateService>().totalPassedCheckovChecks += extractionResult.passedChecksSize
            project.service<FullScanStateService>().totalFailedCheckovChecks += extractionResult.failedChecks.size
            scanTaskResult.checkovResult.delete()
            scanTaskResult.debugOutput.delete()

        } catch (error: Exception) {
            LOG.warn("Error while analyzing scan results for framework $framework")
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, framework, error, ScanSourceType.FRAMEWORK)
        }
    }

    fun analyzeFileScan(scanTaskResult: ScanTaskResult, errorCode: Int, project: Project, filePath: String) {
        if (!isValidScanResults(scanTaskResult, errorCode, filePath, ScanSourceType.FILE, project)) {
            return
        }

        try {
            val extractionResult: CheckovResultExtractionData = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(scanTaskResult.checkovResult.readText(), filePath)

            if (extractionResult.parsingErrorsSize > 0) {
                project.service<CheckovErrorHandlerService>().notifyAboutParsingError(filePath, ScanSourceType.FILE)
                scanTaskResult.checkovResult.delete()
                scanTaskResult.debugOutput.delete()
                return
            }

            if (extractionResult.failedChecks.isEmpty()) {
                LOG.info("Checkov scanning finished, no errors have been detected for file: ${filePath.replace(project.basePath!!, "")}")
                scanTaskResult.checkovResult.delete()
                scanTaskResult.debugOutput.delete()
                return
            }

            project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(ScanSourceType.FRAMEWORK)


            scanTaskResult.checkovResult.delete()
            scanTaskResult.debugOutput.delete()

        } catch (error: Exception) {
            LOG.warn("Error while analyzing scan results for file $filePath")
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, filePath, error, ScanSourceType.FILE)
        }
    }
//    fun analyzeScan(scanTaskResult: ScanTaskResult, errorCode: Int, project: Project, scanningSource: String, scanSourceType: ScanSourceType) {
//        if (!isValidScanResults(scanTaskResult, errorCode, scanningSource, scanSourceType, project)) {
//            return
//        }
//
//        try {
//            val extractionResult: CheckovResultExtractionData = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(scanTaskResult.checkovResult.readText(), scanningSource)
//
//            if (extractionResult.parsingErrors.isNotEmpty()) {
//                project.service<CheckovErrorHandlerService>().scanningParsingError(scanTaskResult, scanningSource, extractionResult.parsingErrors, scanSourceType)
//            }
//
//            if (!isScanFinishedWithoutErrors(extractionResult, scanSourceType, scanningSource, project)) {
//                project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
//                LOG.info("Checkov scanning finished for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}, ${extractionResult.parsingErrors.size} errors have been detected in $scanningSource")
////            CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}, please check the results panel.", NotificationType.INFORMATION)
//                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(scanSourceType)
//
//                if (scanSourceType == ScanSourceType.FRAMEWORK)
//                    project.service<FullScanStateService>().frameworkScanFinishedAndDetectedIssues()
//            }
//
////            if ( dextractionResult.failedChecks.isEmpty()) {
////                if (extractionResult.parsingErrors.isEmpty()) {
//////                    CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished, no errors have been detected for ${scanSourceType.toString().lowercase()}: $scanningSource", NotificationType.INFORMATION)
////                    LOG.info("Checkov scanning finished, no errors have been detected for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}")
////                    project.service<FullScanStateService>().frameworkFinishedWithNoErrors(scanningSource)
////
////                }
////                scanTaskResult.checkovResult.delete()
////                scanTaskResult.debugOutput.delete()
////                return
////            }
//
//
//            project.service<FullScanStateService>().totalPassedCheckovChecks += extractionResult.passedChecksSize
//            project.service<FullScanStateService>().totalFailedCheckovChecks += extractionResult.failedChecks.size
//            scanTaskResult.checkovResult.delete()
//            scanTaskResult.debugOutput.delete()
//
//        } catch (error: Exception) {
//            LOG.warn("Error while analyzing scan results for ${scanSourceType.toString().lowercase()} $scanningSource")
//            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, scanningSource, error, scanSourceType)
//        }
//    }


//    private fun isScanFinishedWithoutErrors(extractionResult: CheckovResultExtractionData, scanSourceType: ScanSourceType, scanningSource: String, project: Project): Boolean {
//        if (extractionResult.failedChecks.isNotEmpty()) {
//            return false
//        }
//
//        if (extractionResult.parsingErrors.isEmpty()) {
////                    CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished, no errors have been detected for ${scanSourceType.toString().lowercase()}: $scanningSource", NotificationType.INFORMATION)
//            LOG.info("Checkov scanning finished, no errors have been detected for ${scanSourceType.toString().lowercase()}: ${scanningSource.replace(project.basePath!!, "")}")
//
//            if (scanSourceType == ScanSourceType.FRAMEWORK)
//                project.service<FullScanStateService>().frameworkFinishedWithNoErrors(scanningSource)
//
//        }
//
//        return true
//    }

    private fun isValidScanResults(scanTaskResult: ScanTaskResult, errorCode: Int, scanningSource: String, scanSourceType: ScanSourceType, project: Project): Boolean {
        if (scanTaskResult.errorReason.contains("Please check your API token")) {
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, scanningSource, Exception("Please check your API token"), scanSourceType)

            LOG.error("Please check you API token\n\n")
            return false
        }

        if (scanTaskResult.errorReason.contains("missing dependencies (e.g., helm or kustomize, which require those tools to be on your system")) {
            val errorMessage = "Framework $scanningSource was not scanned since it's probably not installed: ${scanTaskResult.errorReason}"
            LOG.warn(errorMessage)
            scanTaskResult.checkovResult.delete()
            scanTaskResult.debugOutput.delete()
            project.service<FullScanStateService>().frameworkWasNotScanned(scanningSource)
            return false

        }

        if (errorCode != 0 || scanTaskResult.errorReason.isNotEmpty()) {
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, scanningSource, Exception("Error while scanning $scanningSource, exit code - $errorCode, error reason - ${scanTaskResult.errorReason}"), scanSourceType)
            return false
        }

        return true
    }

    enum class ScanSourceType {
        FILE,
        FRAMEWORK
    }
}



