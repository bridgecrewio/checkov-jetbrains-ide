package com.bridgecrew.services

import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner
import com.intellij.openapi.project.Project

open class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())

    fun installCheckov(project: Project) {
        println("Trying to install Checkov")
        for (runner in checkovRunners) {
            var isCheckovInstalled = runner.installOrUpdate()
            if (isCheckovInstalled) {
                this.selectedCheckovRunner = runner
                println("Checkov installed successfully using ${runner.javaClass.kotlin}")
                project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished()
                return
            }
        }

        if (selectedCheckovRunner == null) {
            throw Exception("Could not install Checkov.")
        }
    }

    fun scanFile(filePath: String, extensionVersion: String, token: String) {
        if (selectedCheckovRunner == null) {
            throw Exception("Checkov is not installed")
        }

        var result = selectedCheckovRunner!!.run(filePath, extensionVersion, token)
        println(result)
    }

    fun getVersion(): String {
        println(this.selectedCheckovRunner)
        if (selectedCheckovRunner == null) {
            throw Exception("[CheckovService]: Checkov is not installed")
        }

        return selectedCheckovRunner!!.getVersion()
    }
}

object CheckovServiceInstance : CheckovService() {
    init {
        println("CheckovServiceInstance invoked")
    }

}