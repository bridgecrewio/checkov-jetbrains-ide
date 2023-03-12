package com.bridgecrew.services.scan

//import com.bridgecrew.ResourceToCheckovResultsList
//import com.bridgecrew.extractFailesCheckAndParsingErrorsFromCheckovResult
//import com.bridgecrew.groupResultsByResource
import com.bridgecrew.errors.CheckovErrorHandlerService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.CheckovNotificationBalloon
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

//class CheckovResultException(message: String) : Exception(message)
//class CheckovResultParsingException(message: String) : Exception(message)


private val LOG = logger<CheckovScanService>()

@Service
class CheckovScanService {
    var selectedCheckovScanner: CheckovScanCommandsService? = null
    private var isFirstRun: Boolean = true
    private val settings = CheckovSettingsState().getInstance()
    private var  currentSingleScanFiles = setOf<String>()

    fun scanFile(filePath: String, project: Project) {
        
        try {
            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan a file using $selectedCheckovScanner")
//            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

//            currentFile = filePath
            val execCommand = prepareExecCommand(filePath)
//            val commandToPrint = replaceApiToken(execCommand.joinToString(" "))
//            LOG.info("Running command: $commandToPrint")
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

    private fun analyzeFileScan(result: String, errorCode: Int, project: Project, filePath: String) {
        if (!isValidScanResults(result, errorCode, filePath, ScanSourceType.FILE, project)) {

//            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
//            CheckovNotificationBalloon.showNotification(project, "Error while running Checkov scan on file $filePath, please check the logs for further action", NotificationType.ERROR)
            return
        }

        try {
            val extractionResult: CheckovResultExtractionData = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(result, filePath)

            if (extractionResult.parsingErrors.isNotEmpty()) {
                project.service<CheckovErrorHandlerService>().scanningParsingError(result, filePath, extractionResult.parsingErrors, ScanSourceType.FILE)
            }

            if (extractionResult.failedChecks.isEmpty()) {
                CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished, no errors have been detected for file: $filePath", NotificationType.INFORMATION)
                LOG.info("Checkov scanning finished, No errors have been detected for the file: $filePath")
                return
            }


//                project.service<ResultsCacheService>()
//                        .deleteAll() // TODO remove after MVP, where we want to display only one file results
//                project.service<ResultsCacheService>()
//                        .setResult(filePathRelativeToProject, resultsGroupedByResource)

            project.service<ResultsCacheService>().addCheckovResults(extractionResult.failedChecks)
            CheckovNotificationBalloon.showNotification(project, "Checkov scanning finished $filePath", NotificationType.INFORMATION)
//                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()
//                if (isFirstRun) {
//                    val errorsStr = if (resultsLength == 1) "error" else "errors"
//                    CheckovNotificationBalloon.showNotification(project, "Checkov has detected $resultsLength configuration $errorsStr in your file $filePath. Check out the tool window to analyze your code", NotificationType.INFORMATION)
////                    CheckovNotificationBalloon.showError(project, resultsLength)
//                    isFirstRun = false
//                }
//            }
        } catch (e: Exception) {
//            LOG.error("Error parsing checkov results \n" +
////                    "Raw response: $result\n" +
//                    "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n", )
//            e.printStackTrace()
//            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            CheckovNotificationBalloon.showNotification(project, "Error while running Checkov scan on file $filePath, please check the logs for further action", NotificationType.ERROR)
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
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()

        } catch (error: Exception) {
            project.service<CheckovErrorHandlerService>().scanningError(result, scanningSource, error, scanSourceType)
        }
    }

    private fun analyzeRepositoryScan(result: String, errorCode: Int, project: Project, framework: String) {
        LOG.info("finished scanning framework $framework")
//        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).frameworkScanningFinished()
        project.service<FullScanStateService>().fullScanFrameworkFinished(project)

        if (!isValidScanResults(result, errorCode, framework, ScanSourceType.FRAMEWORK, project)) {
//            CheckovNotificationBalloon.showNotification(project, "Error while running Checkov full repository scan on framework $framework, please check the logs for further action", NotificationType.ERROR)
//            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            return
        }

        try {
            val extractionResult: CheckovResultExtractionData =  CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(result, framework) // extractFailesCheckAndParsingErrorsFromCheckovResult(result, framework)

            if (extractionResult.parsingErrors.isNotEmpty()) {
                LOG.error("Error while parsing result for framework $framework on files ${extractionResult.parsingErrors}:\n" +
                        "------------------------------------------\n" +
                        result.substring((result.length - 1000).coerceAtLeast(0)) +
                        "\n------------------------------------------\n\n")
                CheckovNotificationBalloon.showNotification(project, "Checkov has failed to run on framework $framework due to parsing errors. Please make sure that your files - ${extractionResult.parsingErrors} - are valid", NotificationType.INFORMATION)

            }

            if (extractionResult.failedChecks.isEmpty()) {
//                CheckovErrorNotificationBalloon.showError(project, "Checkov scanning finished, No errors have been detected for framework $framework", NotificationType.INFORMATION)
                LOG.info("Checkov scanning finished, No errors have been detected for framework $framework")
                return
            }
            project.service<ResultsCacheService>().setCheckovResultsFromResultsList(extractionResult.failedChecks)
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()
        } catch (e: Exception) {
//            LOG.warn("Error parsing checkov results \n" +
////                    "Raw response: $result\n" +
//                    "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
//            e.printStackTrace()
            CheckovNotificationBalloon.showNotification(project, "Error while running Checkov full repository scan on framework $framework, please check the logs for further action", NotificationType.ERROR)
        }
//        catch (e: CheckovResultParsingException) {
//            LOG.error("Error while parsing result for framework $framework\n" +
//                    "------------------------------------------" +
//                    "Error reason: ${result.substring(0, result.length.coerceAtMost(1000))}" +
//                    "------------------------------------------"
//                    , e)
////            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningParsingError() // for checking
//        } //catch (e: CheckovResultException) {
//            CheckovErrorNotificationBalloon.showError(project, "Checkov scanning finished, No errors have been detected for framework $framework", NotificationType.INFORMATION)
//            e.printStackTrace()
//        }
    }

    fun scanProject(project: Project) {
        try {
//            CheckovNotificationBalloon.initialize()
            project.service<FullScanStateService>().fullScanStarted()
            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan the project $selectedCheckovScanner")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).projectScanningStarted()

            project.service<ResultsCacheService>().deleteAllCheckovResults() // TODO - save the previous state for the case where the client cancels the

            val execCommands: List<List<String>> = prepareRepositoryScanningExecCommand()

            execCommands.forEach { execCommand ->
                run {
                    val processHandler: ProcessHandler = OSProcessHandler(generateCheckovCommand(execCommand))

                    val frameworkIndex = execCommand.indexOf("--framework") + 1
                    val framework = execCommand[frameworkIndex]
                    val scanTask = ScanTask(project, "Checkov scanning repository by framework $framework", framework, processHandler, ScanSourceType.FRAMEWORK)
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

//    private fun getGroupedResults(res: String, project: Project, relativeFilePath: String): Pair<ResourceToCheckovResultsList, Int> {
//        val listOfCheckovResults = extractFailesCheckAndParsingErrorsFromCheckovResult(res)
////        project.service<ResultsCacheService>().setMockCheckovResultsFromResultsList(listOfCheckovResults) // MOCK
//
//        return Pair(groupResultsByResource(listOfCheckovResults, project, relativeFilePath), listOfCheckovResults.size)
//    }

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

    private class ScanTask(project: Project, title: String, val scanSource: String, val processHandler: ProcessHandler, val scanSourceType: ScanSourceType) :
            Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            LOG.info("Going to scan for ${scanSourceType.toString().lowercase()} $scanSource")
            indicator.isIndeterminate = false
            val output = ScriptRunnerUtil.getProcessOutput(processHandler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    DEFAULT_TIMEOUT)

            LOG.info("Checkov scan task finished successfully for ${scanSourceType.toString().lowercase()} $scanSource")

//            LOG.info(output)
//            project.service<CheckovScanService>().analyzeScan(output, processHandler.exitCode!!, project, filePath)
            project.service<CheckovScanService>().analyzeScan(output, processHandler.exitCode!!, project, scanSource, scanSourceType)

        }
    }

//    private class RepositoryScanTask(project: Project, title: String, val framework: String, val processHandler: ProcessHandler) :
//            Task.Backgroundable(project, title, true) {
//        override fun run(indicator: ProgressIndicator) {
//            try {
//                LOG.info("Going to scan full repository for framework $framework")
//                indicator.isIndeterminate = false
//                val output = ScriptRunnerUtil.getProcessOutput(processHandler,
//                        ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
//                        FULL_SCAN_FRAMEWORK_DEFAULT_TIMEOUT)
//                LOG.info("Checkov full repository task finished successfully for framework $framework")
////            LOG.info(output)
//
//                project.service<CheckovScanService>().analyzeRepositoryScan(output, processHandler.exitCode!!, project, framework)
//            } catch (e: Exception) {
//                LOG.error("error while scanning framework $framework in full scan", e)
//                project.service<FullScanStateService>().fullScanFrameworkFinished(project)
//
////                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).frameworkScanningFinished()
//            }
//        }
//    }

    enum class ScanSourceType {
        FILE,
        FRAMEWORK
    }
}



