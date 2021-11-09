

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.process.OSProcessHandler
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.process.ScriptRunnerUtil
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import java.io.File
import java.nio.charset.Charset
import javax.swing.SwingUtilities

private val LOG = logger<CliService>()

@Service
class CliService {
    var checkovPath: String = ""
    var checkovVersion: String = ""

    fun run(
        commands: ArrayList<String>,
        project: Project,
        function: (output: String, exitCode: Int, project: Project) -> Unit,
    ) {
        val commandToPrint = commands.joinToString(" ")
        LOG.info("Running command: $commandToPrint")
        val generalCommandLine = GeneralCommandLine(commands)
        generalCommandLine.charset = Charset.forName("UTF-8")
        generalCommandLine.setWorkDirectory(project.getBasePath())

        val processHandler: ProcessHandler = OSProcessHandler(generalCommandLine)
        val myBackgroundable =
            BackgroundableTask(project, "running cli command $commandToPrint", processHandler, function )
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
        val processHandler: ProcessHandler,
        val function: (output: String, exitCode: Int, project: Project) -> Unit,
    ) :
        Task.Backgroundable(project, title,true) {
        override fun run(indicator: ProgressIndicator) {
            indicator.isIndeterminate = false
            val output =
                ScriptRunnerUtil.getProcessOutput(processHandler,
                    ScriptRunnerUtil.STDOUT_OR_STDERR_OUTPUT_KEY_FILTER,
                    720000000)
            function(output,  processHandler.exitCode!!, project)
        }

    }

}




