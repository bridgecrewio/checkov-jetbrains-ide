package com.bridgecrew.services.checkovService

import CheckovInstallerService
import CliService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.InitializationListener
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths

private val LOG = logger<PipCheckovService>()

class PipCheckovService(project: Project) : CheckovService(project) {

    override fun getInstallCommand(): ArrayList<String> {
        val cmds = arrayListOf("pip3", "install", "-U", "--user", "checkov", "-i", "https://pypi.org/simple/")
        return cmds
    }

//    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, prismaUrl: String?): ArrayList<String> {
//        val relevantFilePath = FilenameUtils.separatorsToSystem(filePath)
//        val cmds = arrayListOf(project.service<CliService>().checkovPath, "-s", "--bc-api-key",
//                apiToken, "--repo-id", gitRepoName, "-f", relevantFilePath, "-o", "json")
//        return cmds
//    }

    override fun getVersion(project: Project): ArrayList<String> {
        val cmds = arrayListOf(project.service<CliService>().checkovPath, "-v")
        return cmds
    }

    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath)
    }

    override fun getDirectory(): String {
        return FilenameUtils.separatorsToSystem(project.basePath!!)
    }

    companion object {
        fun setCheckovPath(project: Project) {
            // check if checkov installed globally
            isCheckovInstalledGloablly(project)

            // after this check, will check how to run checkov
        }

        private fun isCheckovInstalledGloablly(project: Project) {
            LOG.info("Checking global checkov installation with `checkov`")
            val cmds = arrayListOf("checkov", "-v")
            project.service<CliService>().run(cmds, project, ::checkGlobalCheckovCmd, ::checkGlobalCheckovCmd)
        }

        private fun getPythonUserBasePath(project: Project) {
            val os = System.getProperty("os.name").toLowerCase()
            if (os.contains("win")) {
                val command = arrayListOf("pip3", "show", "checkov")
                project.service<CliService>().run(command, project, ::updatePathWin)
            } else {
                val command = arrayListOf("python3", "-c", "import site; print(site.USER_BASE)")
                project.service<CliService>().run(command, project, ::updatePathUnix)

            }
        }

        private fun updatePathUnix(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                project.service<CheckovInstallerService>().install(project)
                return
            }
            if (project.service<CliService>().isCheckovInstalledGlobally) {
                project.service<CliService>().checkovPath = "checkov"
            } else {
                project.service<CliService>().checkovPath = Paths.get(output.trim(), "bin", "checkov").toString()
            }
            //here
            project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
        }

        private fun updatePathWin(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkovPath")
                project.service<CheckovInstallerService>().install(project)
                return
            }

            if (project.service<CliService>().isCheckovInstalledGlobally) {
                project.service<CliService>().checkovPath = "checkov.cmd"
                project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
                return
            }
            
            val outputLine = output.split('\n')
            for (line in outputLine) {
                if (line.trim().contains("Location: ")) {
                    LOG.info("Python location is  $line")
                    val sitePackagePath = line.split(' ')[1];
                    project.service<CliService>().checkovPath = Paths.get(Paths.get(sitePackagePath).parent.toString(), "Scripts", "checkov.cmd").toString()
                }
            }
            //here
            project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()

        }

        private fun updateCheckovInstalledGlobally(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.info("Checkov is not installed globally, running local command")
                project.service<CliService>().isCheckovInstalledGlobally = false
            } else {
                LOG.info("Checkov installed globally, will use it")
                project.service<CliService>().isCheckovInstalledGlobally = true
            }
            getPythonUserBasePath(project)
        }

        private fun checkGlobalCheckovCmd(output: String, exitCode: Int, project: Project) {
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.info("Checking global checkov installation with `checkov.cmd`")
                val cmds = arrayListOf("checkov.cmd", "-v")
                project.service<CliService>().run(cmds, project, ::updateCheckovInstalledGlobally, ::updateCheckovInstalledGlobally)
            } else {
                LOG.info("Checkov installed globally, will use it")
                project.service<CliService>().isCheckovInstalledGlobally = true
                getPythonUserBasePath(project)
            }
        }
    }
}

