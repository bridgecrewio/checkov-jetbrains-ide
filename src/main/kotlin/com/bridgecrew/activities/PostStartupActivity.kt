package com.bridgecrew.activities
import CheckovInstallerService
import com.bridgecrew.listeners.InitializationListener.Companion.INITIALIZATION_TOPIC
import com.bridgecrew.services.checkovService.PipCheckovService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.initializeRepoName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

import com.intellij.openapi.components.service

private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        initializeProject(project)
        val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        messageBusConnection.subscribe(INITIALIZATION_TOPIC).run {
            project.service<CheckovInstallerService>().install(project)
            project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
        }
        LOG.info("Startup activity finished")
    }

    private fun initializeProject(project: Project) {
        PipCheckovService.setCheckovPath(project)
        initializeRepoName(project)
    }
}