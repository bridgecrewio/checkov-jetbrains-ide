

import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.services.checkovService.CheckovService
import com.bridgecrew.services.checkovService.DockerCheckovService
import com.bridgecrew.services.checkovService.PipCheckovService
import com.bridgecrew.services.checkovService.PipenvCheckovService
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
        val commands = ArrayList<Pair<CheckovService , ProcessHandler>>()
        val checkovServices = arrayOf(DockerCheckovService(project), PipCheckovService(project), PipenvCheckovService(project) )
        for (servoce in checkovServices){
            val command = servoce.getInstallCommand(project)
            val generalCommandLine = GeneralCommandLine(command)
            generalCommandLine.charset = Charset.forName("UTF-8")
            val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
            commands.add(Pair(servoce, processHandler))
        }

        val myBackgroundable =
            BackgroundableTask(project, "Installing checkov" ,commands)
        if (SwingUtilities.isEventDispatchThread()) {
            ProgressManager.getInstance().run(myBackgroundable)
        } else {
            ApplicationManager.getApplication().invokeLater {
                ProgressManager.getInstance().run(myBackgroundable)
            }
        }
    }

    private class BackgroundableTask(
        project: Project,
        title: String,
        val services: ArrayList<Pair<CheckovService , ProcessHandler>>,
    ) :
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
                if (serviceObject is PipenvCheckovService){
                    PipenvCheckovService.getCheckovPath(project)
                }
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




