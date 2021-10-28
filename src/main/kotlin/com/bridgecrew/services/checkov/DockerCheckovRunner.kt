package com.bridgecrew.services.checkov

import com.bridgecrew.services.CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Paths
private val LOG = logger<DockerCheckovRunner>()

const val DOCKER_MOUNT_DIR = "/checkovScan"

class DockerCheckovRunner : CheckovRunner {

    override fun installOrUpdate(project: Project): Boolean {
        try {
            LOG.info("Trying to install Checkov using Docker.")
            project.service<CliService>().run("docker pull bridgecrew/checkov")

            LOG.info("Checkov installed with Docker successfully.")
            LOG.info("Using checkov version: ${getVersion()}")
            return true
        } catch (err: Exception) {
            LOG.info("Failed to install Checkov using Docker.")
            err.printStackTrace()
            return false
        }
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String): String {
        val scannedFileDirectory = File(filePath).parent.toString()
        val dockerParams = "docker run --rm --tty --env BC_SOURCE=jetbrains --env BC_SOURCE_VERSION=unknown --volume ${scannedFileDirectory}:$DOCKER_MOUNT_DIR bridgecrew/checkov"

        val fileName = Paths.get(filePath).fileName.toString()
        val dockerFilePath = Paths.get(DOCKER_MOUNT_DIR, fileName).toString()
        val unixDockerFilPath = FilenameUtils.separatorsToUnix(dockerFilePath)
        val checkovParams = "-s --bc-api-key $apiToken --repo-id $gitRepoName -f $unixDockerFilPath -o json"

        return "$dockerParams $checkovParams"
    }

    private fun getVersion(): String {
        val dockerCheckovProcess = Runtime.getRuntime().exec("docker run --rm --tty bridgecrew/checkov -v")
        return dockerCheckovProcess.inputStream.bufferedReader().use { it.readText() }
    }
}