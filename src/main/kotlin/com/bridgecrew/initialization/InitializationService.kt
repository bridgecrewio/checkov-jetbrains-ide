package com.bridgecrew.initialization

import CheckovInstallerService
import CliService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.services.checkovScanCommandsService.CheckovScanCommandsService
import com.bridgecrew.services.checkovScanCommandsService.DockerCheckovScanCommandsService
import com.bridgecrew.services.checkovScanCommandsService.PythonCheckovScanCommandsService
import com.bridgecrew.services.installation.DockerInstallerCommandService
import com.bridgecrew.services.installation.InstallerCommandService
import com.bridgecrew.services.installation.PipInstallerCommandService
import com.bridgecrew.services.installation.PipenvInstallerCommandService
import com.bridgecrew.utils.initializeRepoName
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import java.nio.file.Paths

private val LOG = logger<InitializationService>()

@Service
class InitializationService(private val project: Project) {

    private var isCheckovInstalledGlobally: Boolean = false

    fun initializeProject() {
        initializeCheckvoScanService()
        initializeRepoName(project)
    }

    private fun installChekcovIfNeededAndSetCheckovPath() {
        project.messageBus.connect() // TODO - check if disposable
                .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object : CheckovInstallerListener {
                    override fun installerFinished(serviceClass: InstallerCommandService) {
                        if (serviceClass is PipenvInstallerCommandService) {
                            updateCheckovPathAfterInstallation()
                        } else {
                            setSelectedCheckovServiceFromInstaller(serviceClass)
                        }
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
        isCheckovInstalledGlobally = if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.info("Checkov is not installed globally, running local command")
            false
        } else {
            LOG.info("Checkov installed globally, will use it")
            true
        }
        updatePythonBasePath(project)
    }

    private fun updatePythonBasePath(project: Project) {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            val command = PipInstallerCommandService.getWinCommandsForFindingCheckovPath()
            project.service<CliService>().run(command, project, this::updatePathWin)
        } else {
            val command = PipInstallerCommandService.getUnixCommandsForFindingCheckovPath()
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

        setSelectedCheckovService(PythonCheckovScanCommandsService(project))
    }

    private fun updatePathWin(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.contains("[ERROR]")) {
            LOG.warn("Failed to get checkovPath")
            project.service<CheckovInstallerService>().install(project)
            return
        }

        if (isCheckovInstalledGlobally) {
            project.service<CliService>().checkovPath = "checkov.cmd"
            setSelectedCheckovService(PythonCheckovScanCommandsService(project))
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

        setSelectedCheckovService(PythonCheckovScanCommandsService(project))
    }

    private fun setSelectedCheckovServiceFromInstaller(installerServivce: InstallerCommandService) {
        when (installerServivce) {
            is DockerInstallerCommandService -> {
                setSelectedCheckovService(DockerCheckovScanCommandsService(project))
            }

            is PipInstallerCommandService, is PipenvInstallerCommandService -> {
                setSelectedCheckovService(PythonCheckovScanCommandsService(project))
            }
        }
    }

    private fun setSelectedCheckovService(serviceClass: CheckovScanCommandsService) {
        project.service<CheckovScanService>().selectedCheckovScanner = serviceClass
        project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
    }

    private fun updateCheckovPathAfterInstallation() {
        val os = System.getProperty("os.name").lowercase()
        if (os.contains("win")) {
            val command = PipenvInstallerCommandService.getWinCommandsForFindingCheckovPath()
            project.service<CliService>().run(command, project, this::updateCheckovPathWinAfterInstallation)
        } else {
            val command = PipenvInstallerCommandService.getUnixCommandsForFindingCheckovPath()
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
        setSelectedCheckovService(PythonCheckovScanCommandsService(project))
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
        setSelectedCheckovService(PythonCheckovScanCommandsService(project))
    }

    private fun checkIfDockerIsRunningCheckovImage(output: String, exitCode: Int, project: Project) {
        if (exitCode != 0 || output.lowercase().trim().contains("cannot connect to the Docker")) {
            LOG.info("Docker can't be used as scan service, trying to check if installed globally") // TODO - if docker is up in installation and then down...?
            installChekcovIfNeededAndSetCheckovPath()
            return
        }

        setSelectedCheckovService(DockerCheckovScanCommandsService(project))
    }

    private fun initializeCheckvoScanService() {
        val command = DockerInstallerCommandService.getCheckovImageIsRunningCommand()
        project.service<CliService>().run(command, project, this::checkIfDockerIsRunningCheckovImage, this::checkIfDockerIsRunningCheckovImage)
    }

}