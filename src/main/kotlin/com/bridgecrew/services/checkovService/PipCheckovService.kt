package com.bridgecrew.services.checkovService

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Paths
private val LOG = logger<PipCheckovService>()

class PipCheckovService(val project: Project) : CheckovService {

    override fun getInstallCommand(project: Project): ArrayList<String> {
        val cmds =arrayListOf("pip3","install","-U","--user","checkov","-i","https://pypi.org/simple/")
        return cmds
    }

    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): ArrayList<String> {
        val relevantFilePath = FilenameUtils.separatorsToSystem(filePath)
        val cmds = arrayListOf(project.service<CliService>().checkovPath, "-s","--bc-api-key",
            apiToken, "--repo-id", gitRepoName, "-f", relevantFilePath,"-o", "json")
        return cmds
    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = arrayListOf(project.service<CliService>().checkovPath,"-v")
        return cmds
    }

    companion object {
        fun isCheckovInstalledGloablly(project: Project){
            val cmds =arrayListOf("checkov","-v")
            project.service<CliService>().run(cmds,project,::updateGlobalCheckov)
        }

         fun getPythonUserBasePath(project: Project) {
             val os = System.getProperty("os.name").toLowerCase()
             if (os.contains("win")){
                 val command = arrayListOf("pip3", "show", "checkov")
                 project.service<CliService>().run(command,project,::updatePathWin)
             } else {
                 val command = arrayListOf("python3", "-c", "import site; print(site.USER_BASE)")
                 project.service<CliService>().run(command,project,::updatePathUnix)

             }
        }

        private fun updatePathUnix(output: String, exitCode: Int, project: Project){
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                return
            }
            project.service<CliService>().checkovPath =  Paths.get(output.trim(), "bin", "checkov").toString()
        }

        private fun updatePathWin(output: String, exitCode: Int, project: Project){
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                return
            }
            val outputLine = output.split('\n')
            for (line in outputLine) {
                if (line.trim().contains("Location: ")){
                    LOG.info("Python location is  $line")
                    val sitePackagePath = line.split(' ')[1];
                    project.service<CliService>().checkovPath =  Paths.get(Paths.get(sitePackagePath).parent.toString(), "Scripts", "checkov.cmd").toString()
                }
            }
        }

        private fun updateGlobalCheckov(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.info("Checkov is not installed globally, running local command")
                project.service<CliService>().isCheckovInstalledGlobally = false
            } else {
                LOG.info("Checkov installed globally, will use it")
                project.service<CliService>().isCheckovInstalledGlobally = true
            }
        }
    }
}

