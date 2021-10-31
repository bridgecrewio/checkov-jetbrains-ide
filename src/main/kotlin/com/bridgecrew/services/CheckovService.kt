package com.bridgecrew.services

import com.bridgecrew.ResourceToCheckovResultsList
import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.groupResultsByResource
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner
import com.bridgecrew.services.checkov.PipenvCheckovRunner
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.getGitRepoName
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId

import kotlinx.coroutines.*
import org.json.JSONException

class TokenException(message:String): Exception(message)
class CheckovResultException(message:String): Exception(message)

private val LOG = logger<CheckovService>()

@Service
class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner(), PipenvCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private var isFirstRun: Boolean = true
    private val settings = CheckovSettingsState().getInstance()

    fun installCheckov(project: Project) {
        LOG.info("Trying to install Checkov")
        scope.launch {
            for (runner in checkovRunners) {
                LOG.info("Trying to install Checkov using ${runner.javaClass.kotlin}")
                val isCheckovInstalled = runner.installOrUpdate(project)
                if (isCheckovInstalled) {
                    selectedCheckovRunner = runner
                    LOG.info("Checkov installed successfully using ${runner.javaClass.kotlin}")
                    project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished()
                    break
                }
            }

            if (selectedCheckovRunner == null) {
                throw Exception("Could not install Checkov.")
            }
        }
        LOG.info("Finished installing checkov")

    }

    fun scanFile(filePath: String, project: Project) = runBlocking {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        if (runJobRunning !== null) {
            LOG.info("cancelling current running scan job due to newer request")
            runJobRunning!!.cancel()
        }

        LOG.info("Trying to scan a file using $selectedCheckovRunner")
        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

        var res = ""
        runJobRunning = scope.launch {
            try {
                val apiToken = settings?.apiToken ?: throw TokenException("missing api token")
                val pluginVersion = PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"
                val envs = getEnvs(pluginVersion)
                val execCommand = prepareExecCommand(filePath, project, apiToken, pluginVersion)

                res = project.service<CliService>().run(execCommand, envs)

                val filePathRelativeToProject = filePath.replace(project.basePath!!, "")
                val (resultsGroupedByResource, resultsLength) = getGroupedResults(res, project, filePathRelativeToProject)

                if (isActive) {
                    project.service<ResultsCacheService>().deleteAll() // TODO remove after MVP, where we want to display only one file results
                    project.service<ResultsCacheService>().setResult(filePathRelativeToProject, resultsGroupedByResource)
                    project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished()
                    if (isFirstRun) {
                        CheckovNotificationBalloon.showError(project, resultsLength)
                        isFirstRun = false
                    }
                }
                runJobRunning = null

            } catch (e: TokenException) {
                LOG.warn("Wasn't able to get api token\n" +
                        "Please insert an Api Token to continue")
                e.printStackTrace()
                runJobRunning = null
            } catch (e: JSONException) {
                LOG.error("Error parsing checkov results \n" +
                        "Raw response: $res\n" +
                        "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
                e.printStackTrace()
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
                runJobRunning = null
            } catch (e: Exception) {
                LOG.error("Error scanning file\n" +
                        "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
                e.printStackTrace()
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
                runJobRunning = null
            }

        }
    }

    private fun getGroupedResults(res: String, project: Project, relativeFilePath: String): Pair<ResourceToCheckovResultsList, Int> {
        val listOfCheckovResults = getFailedChecksFromResultString(res)
        return Pair(groupResultsByResource(listOfCheckovResults, project, relativeFilePath), listOfCheckovResults.size)
    }

    private fun prepareExecCommand(filePath: String, project: Project, apiToken: String, pluginVersion: String): String {
        val gitRepoName = getGitRepoName(filePath, project)
        var execCommand = selectedCheckovRunner!!.getExecCommand(filePath, apiToken, gitRepoName, pluginVersion)
        val certificateParams = getCertParams()
        return if (!certificateParams.isNullOrEmpty()) "$execCommand $certificateParams" else execCommand
    }

    private fun getEnvs(pluginVersion: String): Array<String>? {
        // getting current process environment variables
        val currEnvsMap = System.getenv()
        val currEnvList = currEnvsMap.toList().map { "${it.first}=${it.second}" }

        // adding checkov environment variables required
        val list: MutableList<String> = mutableListOf()
        list.add("BC_SOURCE_VERSION=$pluginVersion")
        list.add("BC_SOURCE=jetbrains")
        list.add("PRISMA_API_URL=${settings?.prismaURL}")
        list.addAll(currEnvList)

        return list.toTypedArray()
    }

    private fun getCertParams(): String? {
        val certPath = settings?.certificate
        if (!certPath.isNullOrEmpty()) {
            return "-ca $certPath"
        }
        return null
    }

}