package com.bridgecrew.services

import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner
import com.intellij.openapi.project.Project

import kotlinx.coroutines.*

open class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun installCheckov(project: Project) {
        println("Trying to install Checkov")
        scope.launch {
            for (runner in checkovRunners) {
                var isCheckovInstalled = runner.installOrUpdate()
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

    fun scanFile(filePath: String, extensionVersion: String, token: String) {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        selectedCheckovRunner!!.run(filePath, extensionVersion, token)
    }
}

object CheckovServiceInstance : CheckovService() {
    init {
        println("CheckovServiceInstance invoked")
    }

}