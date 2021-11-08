package com.bridgecrew.services.checkovRunner

import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Paths

private val LOG = logger<DockerCheckovRunner>()

const val DOCKER_MOUNT_DIR = "/checkovScan"

class DockerCheckovRunner(val project: Project) : CheckovRunner {

    override fun getInstallCommand(project: Project): ArrayList<String> {
        val cmds =arrayListOf("docker","pull","bridgecrew/checkov")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): ArrayList<String> {
        val fileName = Paths.get(filePath).fileName.toString()
        val image = "bridgecrew/checkov"
        val fileDir = "$filePath:/checkovScan/$fileName"
        val dockerCommand = arrayListOf("docker","run","--rm", "--tty","--env","BC_SOURCE=jetbrains","--env","BC_SOURCE_VERSION=$pluginVersion", "--volume", fileDir, image)
        val checkovCommand = arrayListOf("-d", "/checkovScan", "-s", "--bc-api-key", apiToken, "--repo-id", gitRepoName, "-o", "json" )
        val cmds= ArrayList<String>()
        cmds.addAll(dockerCommand)
        cmds.addAll(checkovCommand)
        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = arrayListOf("docker","run","--rm", "--tty","bridgecrew/checkov","-v")
        return cmds
    }

}