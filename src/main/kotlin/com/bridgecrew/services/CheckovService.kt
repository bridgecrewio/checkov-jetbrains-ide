package com.bridgecrew.services

import com.bridgecrew.services.checkov.CheckovRunner
import com.bridgecrew.services.checkov.DockerCheckovRunner
import com.bridgecrew.services.checkov.PipCheckovRunner

import kotlinx.coroutines.*

open class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private val checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun installCheckov() {
        println("Trying to install Checkov")
        scope.launch {
            for (runner in checkovRunners) {
                var isCheckovInstalled = runner.installOrUpdate()
                if (isCheckovInstalled) {
                    selectedCheckovRunner = runner
                    println("Checkov installed successfully using ${runner.javaClass.kotlin}")
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