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
        PipCheckovRunner.getPythonUserBasePath(project)
        getGitRepoName(project)
        installCheckovOnStartup(project)
        project.service<CheckovToolWindowManagerPanel>().subscribe(project)
        LOG.info("Startup activity finished")
    }

    private fun installCheckovOnStartup(project: Project) {
        LOG.info("Checkov Installation starting")
        project.service<CheckovInstallerService>().install(project)
        LOG.info("Checkov Installation finished")
    }


}