package com.bridgecrew.services.checkovScanCommandsService

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils

class DockerCheckovScanCommandsService(project: Project) : CheckovScanCommandsService(project) {

    private val image = "bridgecrew/checkov"
    private val volumeDirectory = "checkovScan"
    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
        val pluginVersion =
                PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"

        val dockerCommand = arrayListOf("docker", "run", "--rm", "-a", "stdout", "-a", "stderr", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
        val prismaUrl = settings?.prismaURL
        val certPath = settings?.certificate
        if (!prismaUrl.isNullOrEmpty()) {
            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
        }

        if (certPath?.isNotEmpty() == true) {
            var volumeCaFile = "$certPath:/usr/lib/ssl/cert.pem"
            dockerCommand.addAll(arrayListOf("--volume", volumeCaFile, "--env", "SSL_CERT_FILE=/usr/lib/ssl/cert.pem", "--env", "REQUESTS_CA_BUNDLE=/usr/lib/ssl/cert.pem"))
        }
        val volumeDir = "${FilenameUtils.separatorsToUnix(project.basePath!!)}:/${volumeDirectory}"
        dockerCommand.addAll(arrayListOf("--volume", volumeDir, image))
        return dockerCommand

    }

    override fun getDirectory(): String {
        return volumeDirectory
    }

    override fun getFilePath(originalFilePath: String): String {
        return originalFilePath.replace(project.basePath!!, volumeDirectory)
    }
}