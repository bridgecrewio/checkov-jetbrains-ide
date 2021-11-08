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

    private fun isCheckovInstalledGlobally(): Boolean {
        return try {
            val checkovVersionExitCode = Runtime.getRuntime().exec("checkov -v").waitFor()
            checkovVersionExitCode == 0
        } catch (err: Exception) {
            false
        }
    }

    override fun getInstallCommand(project: Project): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.add("pip3")
        cmds.add("install")
        cmds.add("-U")
        cmds.add("--user")
        cmds.add("checkov")
        cmds.add("-i")
        cmds.add("https://pypi.org/simple/")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): ArrayList<String> {
        val cmds = arrayListOf(project.service<CliService>().checkovPath, "-s","--bc-api-key",
            apiToken, "--repo-id", "bridgecrewio/terragoat", "-f", filePath,"-o", "json")
        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.add(project.service<CliService>().checkovPath)
        cmds.add("-v")
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