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

        val volumeDir = "${FilenameUtils.separatorsToUnix(project.basePath!!)}:/${volumeDirectory}"
        val dockerCommand = arrayListOf("docker", "run", "--rm", "--tty", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
        val prismaUrl = settings?.prismaURL
        if (!prismaUrl.isNullOrEmpty()) {
            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
        }
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