package com.bridgecrew.initialization

import CheckovInstallerService
import CliService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.services.CheckovScanService
import com.bridgecrew.services.checkovService.CheckovService
import com.bridgecrew.services.checkovService.PipCheckovService
import com.bridgecrew.services.checkovService.PipenvCheckovService
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.nio.file.Paths
import java.util.*

private val LOG = logger<InitializationService>()

@Service
class InitializationService(private val project: Project) {

    private var isCheckovInstalledGlobally: Boolean = false

    fun initializeProject() {

    }

    fun installChekcovIfNeededAndSetCheckovPath() {
        project.messageBus.connect() // TODO - check if disposable
                .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object: CheckovInstallerListener {
                    override fun installerFinished(serviceClass: CheckovService) {
                        setSelectedCheckovService(serviceClass)
                        if (serviceClass is PipenvCheckovService){
                            updateCheckovPathAfterInstallation()
                        } else {
                            project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
                        }

//                        project.service<CheckovScanService>().selectedCheckovScanner = serviceClass
////                    project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()
//                        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
                    }
                })

        LOG.info("Checking global checkov installation with `checkov`")
        val cmds = arrayListOf("checkov", "-v")
        project.service<CliService>().run(cmds, project, this::checkGlobalCheckovCmd, this::checkGlobalCheckovCmd)
    }

    private fun checkGlobalCheckovCmd(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.info("Checking global checkov installation with `checkov.cmd`")
            val cmds = arrayListOf("checkov.cmd", "-v")
            project.service<CliService>().run(cmds, project, this::updateCheckovInstalledGlobally, this::updateCheckovInstalledGlobally)
            return
        }

        LOG.info("Checkov installed globally, will use it")
        isCheckovInstalledGlobally = true
        updatePythonBasePath(project)

    }

    private fun updateCheckovInstalledGlobally(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.info("Checkov is not installed globally, running local command")
            isCheckovInstalledGlobally = false
        } else {
            LOG.info("Checkov installed globally, will use it")
            isCheckovInstalledGlobally = true
        }
        updatePythonBasePath(project)
    }

    private fun updatePythonBasePath(project: Project) {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            val command = PipCheckovService.getWinCommandsForFindingCheckovPath()
            project.service<CliService>().run(command, project, this::updatePathWin)
        } else {
            val command = PipCheckovService.getWinCommandsForFindingCheckovPath()
            project.service<CliService>().run(command, project, this::updatePathUnix)

        }
    }

    private fun updatePathUnix(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.warn("Failed to get checkovPath")
            project.service<CheckovInstallerService>().install(project)
            return
        }

        if (isCheckovInstalledGlobally) {
            project.service<CliService>().checkovPath = "checkov"
        } else {
            project.service<CliService>().checkovPath = Paths.get(output.trim(), "bin", "checkov").toString()
        }
        //here
        setSelectedCheckovService(serviceClass)

        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
    }

    private fun updatePathWin(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.warn("Failed to get checkovPath")
            project.service<CheckovInstallerService>().install(project)
            return
        }

        if (isCheckovInstalledGlobally) {
            project.service<CliService>().checkovPath = "checkov.cmd"
            setSelectedCheckovService(serviceClass)

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
        setSelectedCheckovService(serviceClass)

        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()

    }

    private fun setSelectedCheckovService(serviceClass: CheckovService) {
        project.service<CheckovScanService>().selectedCheckovScanner = serviceClass
//                    project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()
//        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()

    }

    private fun updateCheckovPathAfterInstallation() {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            val command = arrayListOf("pipenv", "run", "where", "python")
            project.service<CliService>().run(command, project, this::updateCheckovPathWinAfterInstallation)
        } else {
            val command = arrayListOf("pipenv", "run", "which", "python")
            project.service<CliService>().run(command, project, this::updateCheckovPathUnixAfterInstallation)

        }
    }

    private fun updateCheckovPathUnixAfterInstallation(output: String, exitCode: Int, project: Project) {
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
        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
    }

    private fun updateCheckovPathWinAfterInstallation(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.warn("Failed to get checkovPath")
            return
        }
        val result = output.trim()
        val checkovPathArray = result.split('\n')
        LOG.info("Checkov path in Win is $result")
        project.service<CliService>().checkovPath = checkovPathArray[0]
        LOG.info("Setting checkovPath: ${project.service<CliService>().checkovPath}")
        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
    }

}