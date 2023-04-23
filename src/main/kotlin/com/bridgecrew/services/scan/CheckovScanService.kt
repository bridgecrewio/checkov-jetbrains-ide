package com.bridgecrew.services.scan

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.errors.CheckovErrorHandlerService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.utils.*
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.Charset
import javax.swing.SwingUtilities

private val LOG = logger<CheckovScanService>()

@Service
class CheckovScanService: Disposable {
    var selectedCheckovScanner: CheckovScanCommandsService? = null
    private val settings = CheckovSettingsState().getInstance()
    private val fullScanTasks = mutableSetOf<ScanTask.FrameworkScanTask>()
    private var singleFileCurrentScans = mutableMapOf<String, ScanTask.FileScanTask>()

    fun scanFile(filePath: String, project: Project) {
        try {
            if (singleFileCurrentScans.containsKey(filePath)) {
                singleFileCurrentScans[filePath]!!.cancelTask()
            }

            if (singleFileCurrentScans.size == DESIRED_NUMBER_OF_SINGLE_FILE_SCANS) {
                LOG.warn("${singleFileCurrentScans.size} scans for files are currently running. Please try scanning again in a couple of minutes")
                return
            }

            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan a file using $selectedCheckovScanner")

            val checkovResultFile = createCheckovTempFile("${extractFileNameFromPath(filePath)}-checkov-result", ".json")
            val execCommand = prepareExecCommand(filePath, checkovResultFile.path, ScanSourceType.FILE)
            val generalCommandLine = generateCheckovCommand(execCommand)

            val processHandler: ProcessHandler = OSProcessHandler.Silent(generalCommandLine)
            val scanTask = ScanTask.FileScanTask(project, "Checkov scanning file $filePath", filePath, processHandler, checkovResultFile)
            singleFileCurrentScans[filePath] = scanTask

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

            project.service<FullScanStateService>().saveCurrentState()
            project.service<ResultsCacheService>().deleteAllCheckovResults()

            project.service<FullScanStateService>().fullScanStarted()
            project.service<AnalyticsService>().fullScanStarted()

            for (framework in FULL_SCAN_FRAMEWORKS) {

                kotlin.run {
                    val checkovResultFile: File = createCheckovTempFile("$framework-checkov-result", ".json")
                    val execCommand: List<String> = prepareExecCommand(framework, checkovResultFile.path, ScanSourceType.FRAMEWORK)

                    val processHandler: ProcessHandler = OSProcessHandler.Silent(generateCheckovCommand(execCommand))


                    val scanTask = ScanTask.FrameworkScanTask(project, "Checkov scanning repository by framework $framework", framework, processHandler, checkovResultFile)
                    fullScanTasks.add(scanTask)
                    project.service<AnalyticsService>().fullScanByFrameworkStarted(framework)

                    ApplicationManager.getApplication().executeOnPooledThread {
                        kotlin.run {
                            try {
                                if (SwingUtilities.isEventDispatchThread()) {
                                    ProgressManager.getInstance().run(scanTask)
                                } else {
                                    ApplicationManager.getApplication().invokeLater {
                                        ProgressManager.getInstance().run(scanTask)
                                    }
                                }
                            } catch (e: ProcessCanceledException) {
                                LOG.warn("Process for running framework $framework was canceled")
                                project.service<FullScanStateService>().frameworkWasCancelled()
                            }
                        }
                    }
                }

            }
        } catch (e: Exception) {
            CheckovScanAction.resetActionDynamically(true)
            LOG.error(e)
            return
        }
    }

    fun cancelFullScan(project: Project) {
        LOG.info("Going to cancel full scan")
        project.service<FullScanStateService>().onCancel = true

        for (frameworkScanTask in fullScanTasks) {
            frameworkScanTask.cancelTask()
        }
    }

    private fun cancelAllScans() {
        for (scanTask in fullScanTasks + singleFileCurrentScans.values) {
            scanTask.cancelTask()
        }
    }

    private fun generateCheckovCommand(execCommand: List<String>): GeneralCommandLine {
        val pluginVersion = PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version
                ?: "UNKNOWN"
        val prismaUrl = settings?.prismaURL
        val certPath = settings?.certificate

        val generalCommandLine = GeneralCommandLine(execCommand)
        generalCommandLine.charset = Charset.forName("UTF-8")
        generalCommandLine.environment["BC_SOURCE_VERSION"] = pluginVersion
        generalCommandLine.environment["BC_SOURCE"] = "jetbrains"
        generalCommandLine.environment["LOG_LEVEL"] = "DEBUG"

        if (certPath?.isNotEmpty() == true) {
            generalCommandLine.environment["SSL_CERT_FILE"] = certPath
            generalCommandLine.environment["REQUESTS_CA_BUNDLE"] = certPath
        }

        if (!prismaUrl.isNullOrEmpty()) {
            generalCommandLine.environment["PRISMA_API_URL"] = prismaUrl
        }

        return generalCommandLine
    }

    private fun prepareExecCommand(scanningSource: String, checkovResultFilePath: String, scanSourceType: ScanSourceType): List<String> {
        val execCommand = if(scanSourceType == ScanSourceType.FILE)
            selectedCheckovScanner!!.getExecCommandForSingleFile(scanningSource, checkovResultFilePath) else
                selectedCheckovScanner!!.getExecCommandsForRepositoryByFramework(scanningSource, checkovResultFilePath)
        execCommand.addAll(getCertParams())


        val maskedCommand = replaceApiToken(execCommand.joinToString(" "))
        LOG.info("Running command: $maskedCommand")

        return execCommand
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
            scanTaskResult.deleteResultsFile()

        } catch (error: Exception) {
            LOG.warn("Error while analyzing scan results for framework $framework", error)
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
                scanTaskResult.deleteResultsFile()
                return
            }

            if (extractionResult.failedChecks.isEmpty()) {
                LOG.info("Checkov scanning finished, no errors have been detected for file: ${filePath.replace(project.basePath!!, "")}")
                scanTaskResult.deleteResultsFile()
                return
            }

            project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(ScanSourceType.FILE)

            scanTaskResult.deleteResultsFile()
        } catch (error: Exception) {
            LOG.warn("Error while analyzing scan results for file $filePath", error)
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, filePath, error, ScanSourceType.FILE)
        }
    }

    private fun isValidScanResults(scanTaskResult: ScanTaskResult, errorCode: Int, scanningSource: String, scanSourceType: ScanSourceType, project: Project): Boolean {
        if (scanTaskResult.errorReason.contains("Please check your API token")) {
            project.service<CheckovErrorHandlerService>().scanningError(scanTaskResult, scanningSource, Exception("Please check your API token"), scanSourceType)

            LOG.error("Please check you API token\n\n")
            return false
        }

        if (scanTaskResult.errorReason.contains("missing dependencies (e.g., helm or kustomize, which require those tools to be on your system")) {
            val errorMessage = "Framework $scanningSource was not scanned since it's probably not installed: ${scanTaskResult.errorReason}"
            LOG.warn(errorMessage)
            scanTaskResult.deleteResultsFile()
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

    override fun dispose() {
        cancelAllScans()
        deleteCheckovTempDir()
        CheckovScanAction.resetActionDynamically(true)
    }
}



