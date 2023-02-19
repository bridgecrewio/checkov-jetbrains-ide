

import com.bridgecrew.listeners.CheckovInstallerListener
//import com.bridgecrew.services.checkovService.CheckovService
//import com.bridgecrew.services.checkovService.DockerCheckovService
//import com.bridgecrew.services.checkovService.PipCheckovService
//import com.bridgecrew.services.checkovService.PipenvCheckovService
import com.bridgecrew.services.installation.DockerInstallerService
import com.bridgecrew.services.installation.InstallerService
import com.bridgecrew.services.installation.PipInstallerService
import com.bridgecrew.services.installation.PipenvInstallerService
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.nio.charset.Charset
import javax.swing.SwingUtilities


private val LOG = logger<CheckovInstallerService>()

@Service
class CheckovInstallerService {

    fun install(
        project: Project,
    ) {
        val commands = ArrayList<Pair<InstallerService , ProcessHandler>>()
        val checkovServices = arrayOf(DockerInstallerService(), PipInstallerService(), PipenvInstallerService())
        for (service in checkovServices){
            try {
                val command = service.getInstallCommand()
                val generalCommandLine = GeneralCommandLine(command)
                generalCommandLine.charset = Charset.forName("UTF-8")
                val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
                commands.add(Pair(service, processHandler))
            } catch(e: Exception){
                LOG.info("Process is not installed in the machine, will not try to install $e")
                continue
            }
        }
        if (commands.isEmpty()){
            LOG.error("Checkov could not be installed, your machine is missing all 3 installtion options.\n Please install docker | pip | pipenv")
        }
        val installerTask =
            InstallerTask(project, "Installing checkov", commands)
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(installerTask)
        } else {
            ApplicationManager.getApplication().invokeLater {
                ProgressManager.getInstance().run(installerTask)
            }
        }
    }

    private class InstallerTask(project: Project, title: String, val services: ArrayList<Pair<InstallerService, ProcessHandler>>):
        Task.Backgroundable(project, title,true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            for (service in services) {
                val serviceObject = service.first
                val handler = service.second
                val output = ScriptRunnerUtil.getProcessOutput(handler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    720000000)
                if (handler.exitCode != 0 || output.contains("[ERROR]")) {
                    LOG.info("Failed to install using: ${serviceObject.javaClass.kotlin}")
                    continue
                }
//                if (serviceObject is PipenvCheckovService){
//                    PipenvCheckovService.getCheckovPath(project)
//                }
                LOG.info("Checkov installed successfully using ${serviceObject.javaClass.kotlin}")
                project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished(serviceObject)
                project.service<CliService>().run(serviceObject.getVersion(project), project, ::printCheckovVersion)
                break
            }
        }

        fun printCheckovVersion(output: String, exitCode: Int, project: Project){
            if (exitCode != 0 || output.contains("[ERROR]")) {
                LOG.warn("Failed to get checkov version")
                return
            }
            project.service<CliService>().checkovVersion = output.trim().replace("\n","")
            LOG.info("Checkov was installed version: $output")
        }

    }

}




