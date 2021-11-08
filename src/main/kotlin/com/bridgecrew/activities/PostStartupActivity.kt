package com.bridgecrew.activities
import CheckovInstallerService
import CliService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.services.CheckovScanService
import com.bridgecrew.services.checkovRunner.CheckovRunner
import com.bridgecrew.services.checkovRunner.PipCheckovRunner
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.PANELTYPE
import com.bridgecrew.utils.getGitRepoName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

import com.intellij.openapi.components.service
import java.nio.file.Paths

private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        this.installCheckovOnStartup(project)
        PipCheckovRunner.getPythonUserBasePath(project)
        getGitRepoName(project)
        subscribe(project)
        println("This is the current project from pre install activity ${project.getBasePath()}")

        LOG.info("Startup activity finished")
    }

    private fun installCheckovOnStartup(project: Project) {
        LOG.info("Checkov Installation starting")
        project.service<CheckovInstallerService>().install(project)
        LOG.info("Checkov Installation finished")
    }

    private fun subscribe(project: Project){
        project.messageBus.connect()
            .subscribe(CheckovScanListener.SCAN_TOPIC, object: CheckovScanListener {
                override fun scanningStarted() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVSTARTED)
                }

                override fun scanningFinished() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().displayResults()

                    }
                }

                override fun scanningFinished(fileName: String) {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVFINISHED, fileName)

                    }
                }

                override fun scanningError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVERROR)
                    }
                }

                override fun scanningParsingError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVPARSINGERROR)
                    }
                }
            })

        project.messageBus.connect()
            .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object: CheckovInstallerListener {
                override fun installerFinished(runnerClass: CheckovRunner) {
                    println("in subscript installer")
                    project.service<CheckovScanService>().selectedCheckovRunner = runnerClass
                    project.service<CheckovToolWindowManagerPanel>().subscribeToListeners()
                }
            })

        project.messageBus.connect()
            .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object: CheckovSettingsListener {
                override fun settingsUpdated() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
                }
            })
        println("end manager init")

    }
}