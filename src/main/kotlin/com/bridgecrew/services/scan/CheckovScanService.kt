package com.bridgecrew.services.scan

import com.bridgecrew.ResourceToCheckovResultsList
import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.groupResultsByResource
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.DEFAULT_TIMEOUT
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import org.json.JSONException
import java.nio.charset.Charset
import javax.swing.SwingUtilities

class CheckovResultException(message: String) : Exception(message)
class CheckovResultParsingException(message: String) : Exception(message)


private val LOG = logger<CheckovScanService>()

@Service
class CheckovScanService {
    var selectedCheckovScanner: CheckovScanCommandsService? = null
    private var isFirstRun: Boolean = true
    private val settings = CheckovSettingsState().getInstance()
    private var currentFile = ""

    fun scanFile(filePath: String, project: Project) {
        try {
            if (selectedCheckovScanner == null) {
                LOG.warn("Checkov is not installed")
            }

            LOG.info("Trying to scan a file using $selectedCheckovScanner")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

            currentFile = filePath
            val execCommand = prepareExecCommand(filePath)
            val commandToPrint = replaceApiToken(execCommand.joinToString(" "))
            LOG.info("Running command: $commandToPrint")
            val generalCommandLine = generateCheckovCommand(execCommand)

            val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
            val scanTask = ScanTask(project, "Checkov scanning file $currentFile", filePath, processHandler)
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

    private fun analyzeScan(result: String, errorCode: Int, project: Project, filePath: String) {
        if (!isValidScanResults(result, errorCode, project)) {
            return
        }

        try {
            if (filePath == currentFile) {  // To show only the last run of checkov ( on the opened file)
                val filePathRelativeToProject = filePath.replace(project.basePath!!, "")
                val (resultsGroupedByResource, resultsLength) = getGroupedResults(result,
                        project,
                        filePathRelativeToProject)
                project.service<ResultsCacheService>()
                        .deleteAll() // TODO remove after MVP, where we want to display only one file results
                project.service<ResultsCacheService>()
                        .setResult(filePathRelativeToProject, resultsGroupedByResource)
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()
                if (isFirstRun) {
                    CheckovNotificationBalloon.showError(project, resultsLength)
                    isFirstRun = false
                }
            }
        } catch (e: JSONException) {
            LOG.warn("Error parsing checkov results \n" +
                    "Raw response: $result\n" +
                    "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
            e.printStackTrace()
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
        } catch (e: CheckovResultParsingException) {
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningParsingError()
        } catch (e: CheckovResultException) {
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(filePath)
        }
    }

    private fun analyzeRepositoryScan(result: String, errorCode: Int, project: Project, framework: String) {
        LOG.info("finished scanning framework $framework")
        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).frameworkScanningFinished()

        if (!isValidScanResults(result, errorCode, project)) {
            return
        }

        try {
            val failedResults = getFailedChecksFromResultString(result)
            project.service<ResultsCacheService>().setCheckovResultsFromResultsList(failedResults)
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()
        } catch (e: JSONException) {
            LOG.warn("Error parsing checkov results \n" +
                    "Raw response: $result\n" +
                    "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
            e.printStackTrace()
        } catch (e: CheckovResultParsingException) {
            e.printStackTrace()
        } catch (e: CheckovResultException) {
            e.printStackTrace()
        }
    }

    fun scanProject(project: Project) {
        try {
            CheckovNotificationBalloon.initialize()
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
                    val scanTask = RepositoryScanTask(project, "Checkov scanning repository by framework $framework", framework, processHandler)
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

    private fun isValidScanResults(result: String, errorCode: Int, project: Project): Boolean {
        if (result.contains("Please check your API token")) {
            LOG.warn("Please check you API token\n\n $result")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            return false
        }
        if (errorCode != 0 || result.contains("[ERROR]")) {
            LOG.warn("Error scanning file\n" +
                    "To report: open an issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n $result")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            return false
        }

        return true
    }

    private fun getGroupedResults(res: String, project: Project, relativeFilePath: String): Pair<ResourceToCheckovResultsList, Int> {
        val listOfCheckovResults = getFailedChecksFromResultString(res)
//        project.service<ResultsCacheService>().setMockCheckovResultsFromResultsList(listOfCheckovResults) // MOCK

        return Pair(groupResultsByResource(listOfCheckovResults, project, relativeFilePath), listOfCheckovResults.size)
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

    private class ScanTask(project: Project, title: String, val filePath: String, val processHandler: ProcessHandler) :
            Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            val output = ScriptRunnerUtil.getProcessOutput(processHandler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    DEFAULT_TIMEOUT)
            LOG.info("Checkov task output:")
            LOG.info(output)
            project.service<CheckovScanService>().analyzeScan(output, processHandler.exitCode!!, project, filePath)
        }
    }

    private class RepositoryScanTask(project: Project, title: String, val framework: String, val processHandler: ProcessHandler) :
            Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            val output = ScriptRunnerUtil.getProcessOutput(processHandler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    DEFAULT_TIMEOUT)
            LOG.info("Checkov full repository task output:")
            LOG.info(output)

            project.service<CheckovScanService>().analyzeRepositoryScan(output, processHandler.exitCode!!, project, framework)
        }
    }
}



