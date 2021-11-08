package com.bridgecrew.services.checkovRunner

import CliService
import com.bridgecrew.listeners.CheckovScanListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.util.messages.Topic
import java.nio.file.Paths
private val LOG = logger<PipCheckovRunner>()

class PipCheckovRunner(val project: Project) : CheckovRunner {

    override fun getInstallCommand(project: Project): ArrayList<String> {
        val cmds =arrayListOf("pip3","install","-U","--user","checkov","-i","https://pypi.org/simple/")
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
         fun getPythonUserBasePath(project: Project) {
            val command = arrayListOf("python3", "-c", "import site; print(site.USER_BASE)")
            project.service<CliService>().run(command,project,::updatePath)
        }

        fun updatePath(output: String, exitCode: Int, project: Project){
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                return
            }
            project.service<CliService>().checkovPath =  Paths.get(output.trim(), "bin", "checkov").toString()
        }
    }
}