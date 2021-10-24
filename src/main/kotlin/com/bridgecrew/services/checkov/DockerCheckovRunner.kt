package com.bridgecrew.services.checkov

import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Paths
import com.bridgecrew.services.CliService
import com.bridgecrew.services.CliServiceInstance
import kotlinx.coroutines.*

const val DOCKER_MOUNT_DIR = "/checkovScan"

class DockerCheckovRunner : CheckovRunner {
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private val cliService: CliService = CliServiceInstance

    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using Docker.")
            val dockerPullProcess = Runtime.getRuntime().exec("docker pull bridgecrew/checkov")
            val dockerPullExitCode = dockerPullProcess.waitFor()

            if (dockerPullExitCode != 0) {
                println("Failed to Docker pull Checkov Image.")
                println(dockerPullProcess.errorStream.bufferedReader().use { it.readText() })
                throw Exception("Failed to Docker pull Checkov Image")
            }

            println("Checkov installed with Docker successfully.")
            println("Using checkov version: ${getVersion()}")
            return true
        } catch (err: Exception) {
            println("Failed to install Checkov using Docker.")
            err.printStackTrace()
            return false
        }
    }

    override fun run(filePath: String, extensionVersion: String, bcToken: String) = runBlocking {
        println("Trying file scan using Docker.")
        if (runJobRunning !== null) {
            println("cancelling current running scan job due to newer request")
            runJobRunning!!.cancel()
        }

        runJobRunning = scope.launch {
            val scannedFileDirectory = File(filePath).parent.toString()
            val dockerParams = "docker run --rm --tty --env BC_SOURCE=vscode --env BC_SOURCE_VERSION=${extensionVersion} --volume ${scannedFileDirectory}:$DOCKER_MOUNT_DIR bridgecrew/checkov"
            println("Docker params: $dockerParams")

            val fileName = Paths.get(filePath).fileName.toString()
            val dockerFilePath = Paths.get(DOCKER_MOUNT_DIR, fileName).toString()
            val unixDockerFilPath = FilenameUtils.separatorsToUnix(dockerFilePath)
            val checkovParams = "-s --skip-check ${SKIP_CHECKS.joinToString(",")} --bc-api-key $bcToken --repo-id vscode/extension -f $unixDockerFilPath -o json"
            println("Checkov params: $checkovParams")
            val execCommand = "$dockerParams $checkovParams"
            val res = cliService.run(execCommand)
            if (isActive) {
                println(res)
                runJobRunning = null
            }
        }
    }

    private fun getVersion(): String {
        val dockerCheckovProcess = Runtime.getRuntime().exec("docker run --rm --tty bridgecrew/checkov -v")
        return dockerCheckovProcess.inputStream.bufferedReader().use { it.readText() }
    }
}