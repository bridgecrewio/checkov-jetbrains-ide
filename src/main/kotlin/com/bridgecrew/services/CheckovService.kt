package com.bridgecrew.services

import com.bridgecrew.getFailedChecksFromResultString
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.intellij.openapi.project.Project

import kotlinx.coroutines.*

open class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private val cliService: CliService = CliServiceInstance
    private var isFirstRun: Boolean = true

    fun installCheckov(project: Project) {
        println("Trying to install Checkov")
        scope.launch {
            for (runner in checkovRunners) {
                val isCheckovInstalled = runner.installOrUpdate()
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

    fun scanFile(filePath: String, extensionVersion: String, token: String, project: Project) = runBlocking {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        if (runJobRunning !== null) {
            println("cancelling current running scan job due to newer request")
            runJobRunning!!.cancel()
        }

        println("Trying to scan a file using $selectedCheckovRunner")
        project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningStarted()

        val execCommand = selectedCheckovRunner!!.getExecCommand(filePath, extensionVersion, token)

        println("Exec command: $execCommand")

        runJobRunning = scope.launch {
            try {
                val res = cliService.run(execCommand)
                val listOfCheckovResults = getFailedChecksFromResultString(res)
                if (isActive) {
                    project.messageBus.syncPublisher(CheckovScanListener.SCAN_TOPIC).scanningFinished(listOfCheckovResults)
                    if (isFirstRun) {
                        CheckovNotificationBalloon.showError(project, listOfCheckovResults.size)
                        isFirstRun = false
                    }
                }
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

object CheckovServiceInstance : CheckovService() {
    init {
        println("CheckovServiceInstance invoked")
    }
}