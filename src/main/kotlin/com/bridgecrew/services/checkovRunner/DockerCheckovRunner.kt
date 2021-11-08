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
        val cmds = ArrayList<String>()
        cmds.add("docker")
        cmds.add("pull")
        cmds.add("bridgecrew/checkov")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): ArrayList<String> {
        val scannedFileDirectory = File(filePath).parent.toString()
        val dockerParams = "docker run --rm --tty --env BC_SOURCE=jetbrains --env BC_SOURCE_VERSION=$pluginVersion --volume ${scannedFileDirectory}:$DOCKER_MOUNT_DIR bridgecrew/checkov"

        val cmds = ArrayList<String>()
        cmds.add("docker")
        cmds.add("run")
//        cmds.add("--rm")
        cmds.add("--tty")
        cmds.add("--env")
        cmds.add("BC_SOURCE=jetbrains")
        cmds.add("--env")
        cmds.add("BC_SOURCE_VERSION=$pluginVersion")

        cmds.add("--volume")
//        cmds.add("${scannedFileDirectory}:$DOCKER_MOUNT_DIR")
        val fileName = Paths.get(filePath).fileName.toString()

        cmds.add("$filePath:/checkovScan/$fileName")
        cmds.add("bridgecrew/checkov")



        cmds.add("-d")
        cmds.add("/checkovScan")

        cmds.add("-s")
        cmds.add("--bc-api-key")
        cmds.add(apiToken)
        cmds.add("--repo-id")
        cmds.add("bridgecrewio/terragoat")
        cmds.add("-o")
        cmds.add("json")

        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.add("docker")
        cmds.add("run")
        cmds.add("--rm")
        cmds.add("--tty")
        cmds.add("bridgecrew/checkov")
        cmds.add("-v")
        return cmds
    }

}