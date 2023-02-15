package com.bridgecrew.activities
import CheckovInstallerService
import com.bridgecrew.services.ResultsCacheService
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
        PipCheckovService.setCheckovPath(project)
        getGitRepoName(project)
        project.service<CheckovInstallerService>().install(project)
        project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
//        project.service<ResultsCacheService>().setMockCheckovResultsFromExampleFile() // MOCK
        LOG.info("Startup activity finished")
    }
}