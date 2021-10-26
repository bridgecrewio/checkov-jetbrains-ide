package com.bridgecrew.services

import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner
import com.bridgecrew.services.checkov.PipenvCheckovRunner
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.getGitRepoName
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

import kotlinx.coroutines.*
import org.json.JSONException

@Service
class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner(), PipenvCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private var isFirstRun: Boolean = true

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

    fun scanFile(filePath: String, extensionVersion: String, token: String?, project: Project) = runBlocking {
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

                if (token == null) {
                    throw Exception("missing api token") // TODO: fix exception type
                }

                val gitRepoName = getGitRepoName(filePath, project)
                val execCommand = selectedCheckovRunner!!.getExecCommand(filePath, extensionVersion, token, gitRepoName)

                println("Exec command: $execCommand")
                res = project.service<CliService>().run(execCommand)
                val listOfCheckovResults = getFailedChecksFromResultString(res)
                if (isActive) {
                    project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(listOfCheckovResults)
                    if (isFirstRun) {
                        CheckovNotificationBalloon.showError(project, listOfCheckovResults.size)
                        isFirstRun = false
                    }
                }
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
}