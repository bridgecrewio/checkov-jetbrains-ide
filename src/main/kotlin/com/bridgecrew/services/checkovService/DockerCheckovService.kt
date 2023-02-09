package com.bridgecrew.services.checkovService

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import java.nio.file.Paths

private val LOG = logger<DockerCheckovService>()

const val DOCKER_MOUNT_DIR = "/checkovScan"

class DockerCheckovService(project: Project) : CheckovService(project) {

    override fun getInstallCommand(): ArrayList<String> {
        val cmds = arrayListOf("docker", "pull", "bridgecrew/checkov")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, prismaUrl: String?): ArrayList<String> {
        val pluginVersion =
                PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"

        val fileName = Paths.get(filePath).fileName.toString()
        val image = "bridgecrew/checkov"
        val fileDir = "$filePath:/checkovScan/$fileName"
        val dockerCommand = arrayListOf("docker", "run", "--rm", "--tty", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
        if (!prismaUrl.isNullOrEmpty()) {
            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
        }
        dockerCommand.addAll(arrayListOf("--volume", fileDir, image))
        val checkovCommand = arrayListOf("-d", "/checkovScan", "-s", "--bc-api-key", apiToken, "--repo-id", gitRepoName, "-o", "json")
        val cmds = ArrayList<String>()
        cmds.addAll(dockerCommand)
        cmds.addAll(checkovCommand)
        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = arrayListOf("docker", "run", "--rm", "--tty", "bridgecrew/checkov", "-v")
        return cmds
    }

    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
        TODO("Not yet implemented")
    }

}