

import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.services.checkovRunner.CheckovRunner
import com.bridgecrew.services.checkovRunner.DockerCheckovRunner
import com.bridgecrew.services.checkovRunner.PipCheckovRunner
import com.bridgecrew.services.checkovRunner.PipenvCheckovRunner
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
import java.nio.file.Paths
import javax.swing.SwingUtilities


private val LOG = logger<CheckovInstallerService>()

@Service
class CheckovInstallerService {

    fun install(
        project: Project,
    ) {
        val commands = ArrayList<Pair<CheckovRunner , ProcessHandler>>()
        val checkovRunners = arrayOf(DockerCheckovRunner(project), PipCheckovRunner(project), PipenvCheckovRunner(project) )
        for (runner in checkovRunners){
            val command = runner.getInstallCommand(project)
            val generalCommandLine = GeneralCommandLine(command)
            generalCommandLine.charset = Charset.forName("UTF-8")
            val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
            commands.add(Pair(runner, processHandler))
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
        val runners: ArrayList<Pair<CheckovRunner , ProcessHandler>>,
    ) :
        Task.Backgroundable(project, title,true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            for (runner in runners) {
                val runnerObject = runner.first
                val handler = runner.second
                val output = ScriptRunnerUtil.getProcessOutput(handler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    720000000)
                println(output)
                println(handler.exitCode)
                if (handler.exitCode != 0 || output.contains("[ERROR]")) {
                    LOG.info("Failed to install using: ${runnerObject.javaClass.kotlin}")
                    continue
                }
                if (runnerObject is PipenvCheckovRunner){
                    PipenvCheckovRunner.getCheckovPath(project)
                }
                LOG.info("Checkov installed successfully using ${runnerObject.javaClass.kotlin}")
                project.messageBus.syncPublisher(CheckovInstallerListener.INSTALLER_TOPIC).installerFinished(runnerObject)
                project.service<CliService>().run(runnerObject.getVersion(project), project, ::printCheckovVersion)
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




