package com.bridgecrew.services

import com.bridgecrew.getFailedChecksFromResultString
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
import com.intellij.openapi.project.Project

import kotlinx.coroutines.*
import org.json.JSONException

class TokenException(message:String): Exception(message)

@Service
class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner(), PipenvCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private var isFirstRun: Boolean = true
    private val settings = CheckovSettingsState().getInstance()

    fun installCheckov(project: Project) {
        println("Trying to install Checkov")
        scope.launch {
            for (runner in checkovRunners) {
                val isCheckovInstalled = runner.installOrUpdate(project)
                if (isCheckovInstalled) {
                    selectedCheckovRunner = runner
                    println("Checkov installed successfully using ${runner.javaClass.kotlin}")
                    project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished()
                    break
                }
            }

            if (selectedCheckovRunner == null) {
                throw Exception("Could not install Checkov.")
            }
        }
    }

    fun scanFile(filePath: String, project: Project) = runBlocking {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        if (runJobRunning !== null) {
            println("cancelling current running scan job due to newer request")
            runJobRunning!!.cancel()
        }

        println("Trying to scan a file using $selectedCheckovRunner")
        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

        var res = ""
        runJobRunning = scope.launch {
            try {

                val apiToken = settings?.apiToken ?: throw TokenException("missing api token")
                val envs = getEnvs()
                val execCommand = prepareExecCommand(filePath, project, apiToken)
                println("Exec command: $execCommand")

                res = project.service<CliService>().run(execCommand, envs)
                val listOfCheckovResults = getFailedChecksFromResultString(res)

                if (isActive) {
                    project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(listOfCheckovResults)
                    if (isFirstRun) {
                        CheckovNotificationBalloon.showError(project, listOfCheckovResults.size)
                        isFirstRun = false
                    }
                }
                runJobRunning = null

            } catch (e: TokenException) {
                println("Wasn't able to get api token")
                e.printStackTrace()
                runJobRunning = null
            } catch (e: JSONException) {
                println("Error parsing checkov results:")
                e.printStackTrace()
                println("raw response:")
                println(res)
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
                runJobRunning = null
            } catch (e: Exception) {
                println("Error scanning file")
                e.printStackTrace()
                project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningError()
                runJobRunning = null
            }
        }
    }

    private fun prepareExecCommand(filePath: String, project: Project, apiToken: String): String {
        val gitRepoName = getGitRepoName(filePath, project)
        var execCommand = selectedCheckovRunner!!.getExecCommand(filePath, apiToken, gitRepoName)
        val certificateParams = getCertParams()
        return if (!certificateParams.isNullOrEmpty()) "$execCommand $certificateParams" else execCommand
    }

    private fun getEnvs(): Array<String>? {
        // getting current process environment variables
        val currEnvsMap = System.getenv()
        val currEnvList = currEnvsMap.toList().map { "${it.first}=${it.second}" }

        // adding checkov environment variables required
        val list: MutableList<String> = mutableListOf()
        list.add("BC_SOURCE_VERSION=unknown")
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