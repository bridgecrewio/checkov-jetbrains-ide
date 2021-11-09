package com.bridgecrew.services.checkovService

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project

private val LOG = logger<PipenvCheckovService>()

class PipenvCheckovService(val project: Project) : CheckovService {

    override fun getInstallCommand(project: Project): ArrayList<String> {
        val cmds =arrayListOf("pipenv","--python","3","install","checkov")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): ArrayList<String> {
        val cmds = arrayListOf(project.service<CliService>().checkovPath, "-s","--bc-api-key",
            apiToken, "--repo-id", gitRepoName, "-f", filePath,"-o", "json")
        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = arrayListOf(project.service<CliService>().checkovPath,"-v")
        return cmds
    }

    companion object {
        fun getCheckovPath(project: Project) {
            val cmds =arrayListOf("pipenv","run","which","python")
            project.service<CliService>().run(cmds, project, ::updateCheckovPath)
        }

        fun updateCheckovPath(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                return
            }
            val result = output.trim()
            val checkovPathArray: MutableList<String> = result.split('/').toMutableList()
            checkovPathArray.removeLast()
            checkovPathArray.add("checkov")
            project.service<CliService>().checkovPath = checkovPathArray.joinToString(separator = "/")
            LOG.info("Setting checkovPath: ${project.service<CliService>().checkovPath}")
        }
    }
}