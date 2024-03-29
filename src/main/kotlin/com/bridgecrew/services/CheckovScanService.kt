package com.bridgecrew.services

import com.bridgecrew.ResourceToCheckovResultsList
import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.groupResultsByResource
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener

import com.bridgecrew.services.checkovService.CheckovService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.DEFAULT_TIMEOUT
import com.bridgecrew.utils.defaultRepoName
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.*
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
import com.intellij.openapi.util.Key
import org.json.JSONException
import java.nio.charset.Charset
import javax.swing.SwingUtilities

class TokenException(message:String): Exception(message)
class CheckovResultException(message:String): Exception(message)
class CheckovResultParsingException(message:String): Exception(message)


private val LOG = logger<CheckovScanService>()

@Service
class CheckovScanService {
    var selectedCheckovScanner: CheckovService? = null
    private var isFirstRun: Boolean = true
    private val settings = CheckovSettingsState().getInstance()
    private var currentFile = ""
    var gitRepo = defaultRepoName


    fun scanFile(filePath: String, project: Project) {
        if (selectedCheckovScanner == null) {
            LOG.warn("Checkov is not installed")
        }

        LOG.info("Trying to scan a file using $selectedCheckovScanner")
        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

        val apiToken = settings?.apiToken
        if (apiToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
            LOG.warn("Wasn't able to get api token\n" +
                    "Please insert an Api Token to continue")
            return
        }

        currentFile = filePath
        val pluginVersion =
            PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"
        val prismaUrl = settings?.prismaURL

        val execCommand = prepareExecCommand(filePath, project, apiToken, pluginVersion, prismaUrl)
        val commandToPrint = replaceApiToken(execCommand.joinToString(" "))
        LOG.info("Running command: $commandToPrint")
        val generalCommandLine = GeneralCommandLine(execCommand)
        generalCommandLine.charset = Charset.forName("UTF-8")
        generalCommandLine.environment["BC_SOURCE_VERSION"] = pluginVersion
        generalCommandLine.environment["BC_SOURCE"] = "jetbrains"
        generalCommandLine.environment["LOG_LEVEL"] = "DEBUG"
        if (!prismaUrl.isNullOrEmpty()) {
            generalCommandLine.environment["PRISMA_API_URL"] = prismaUrl
        }

        val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
        val scanTask =
            ScanTask(project, "Checkov scanning file $currentFile",filePath, processHandler)
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(scanTask)
        } else {
            ApplicationManager.getApplication().invokeLater {
                ProgressManager.getInstance().run(scanTask)
            }
        }
    }

    private fun analyzeScan(checkovResult: String, debugOutput: String, errorCode: Int, project: Project, filePath: String){
        if (debugOutput.contains("Please check your API token")) {
            LOG.warn("Please check you API token\n\n" +
                    " $debugOutput\n$checkovResult")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            return
        }
        if (errorCode != 0 || debugOutput.contains("[ERROR]")) {
            LOG.warn("Error scanning file\n" +
                    "To report: open an issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n $debugOutput\n$checkovResult")
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
            return
        }
        try {
            if (filePath == currentFile) {  // To show only the last run of checkov ( on the opened file)
                val filePathRelativeToProject = filePath.replace(project.basePath!!, "")
                val (resultsGroupedByResource, resultsLength) = getGroupedResults(checkovResult,
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
                    "Raw response: $debugOutput\n$checkovResult\n" +
                    "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
            e.printStackTrace()
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
        } catch (e: CheckovResultParsingException){
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningParsingError()
        } catch (e: CheckovResultException) {
            project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(filePath)
        }
    }


    private fun getGroupedResults(res: String, project: Project, relativeFilePath: String): Pair<ResourceToCheckovResultsList, Int> {
        val listOfCheckovResults = getFailedChecksFromResultString(res)
        return Pair(groupResultsByResource(listOfCheckovResults, project, relativeFilePath), listOfCheckovResults.size)
    }

    private fun prepareExecCommand(filePath: String, project: Project, apiToken: String, pluginVersion: String, prismaUrl: String? = ""): ArrayList<String> {
        val execCommand = selectedCheckovScanner!!.getExecCommand(filePath, apiToken, gitRepo, pluginVersion, prismaUrl)
        return getCertParams(execCommand)
    }


    private fun getCertParams(cmds: ArrayList<String>): ArrayList<String> {
        val certPath = settings?.certificate
        val noCertVerify = settings?.noCertVerify
        if (!certPath.isNullOrEmpty()) {
            cmds.add("-ca")
            cmds.add(certPath)
        }

        if (noCertVerify == true) {
            cmds.add("--no-cert-verify")
        }

        return cmds
    }

    private class ScanTask(project: Project, title: String, val filePath: String, val processHandler: ProcessHandler) :
            Task.Backgroundable(project, title, true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            val processOutputs = getProcessOutputs()
            LOG.info("Checkov task output:")
            LOG.info(processOutputs.first + "\n" + processOutputs.second)
            project.service<CheckovScanService>().analyzeScan(processOutputs.second, processOutputs.first, processHandler.exitCode!!, project, filePath)
        }

        private fun getProcessOutputs(): Pair<String, String> {
            LOG.assertTrue(!processHandler.isStartNotified)
            val checkovOutputBuilder = StringBuilder()
            val debugOutputBuilder = StringBuilder()
            processHandler.addProcessListener(object : ProcessAdapter() {
                override fun onTextAvailable(event: ProcessEvent, outputType: Key<*>) {
                    if (outputType == ProcessOutputTypes.SYSTEM) {
                        return
                    }

                    val text = event.text

                    if (outputType == ProcessOutputTypes.STDOUT) {
                        checkovOutputBuilder.append(text)
                    } else if (outputType == ProcessOutputTypes.STDERR) {
                        debugOutputBuilder.append(text)
                    }

                    LOG.debug(text)
                }
            })

            processHandler.startNotify()
            if (!processHandler.waitFor(DEFAULT_TIMEOUT)) {
                throw ExecutionException("Script execution took more than ${(DEFAULT_TIMEOUT / 1000)} seconds")
            }
            return Pair(debugOutputBuilder.toString(), checkovOutputBuilder.toString())
        }
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
}



