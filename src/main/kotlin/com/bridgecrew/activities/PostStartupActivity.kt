package com.bridgecrew.activities
import CheckovInstallerService
import com.bridgecrew.services.checkovService.PipCheckovService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.getGitRepoName
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

import com.intellij.openapi.components.service

private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        PipCheckovService.getPythonUserBasePath(project)
        getGitRepoName(project)
        installCheckovOnStartup(project)
        project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
        LOG.info("Startup activity finished")
    }

    private fun installCheckovOnStartup(project: Project) {
        LOG.info("Checkov Installation starting")
        project.service<CheckovInstallerService>().install(project)
        LOG.info("Checkov Installation finished")
    }


}