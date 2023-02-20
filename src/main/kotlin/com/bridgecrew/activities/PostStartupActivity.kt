package com.bridgecrew.activities
import CheckovInstallerService
import com.bridgecrew.initialization.InitializationService
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.listeners.InitializationListener.Companion.INITIALIZATION_TOPIC
//import com.bridgecrew.services.checkovService.PipCheckovService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.initializeRepoName
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

import com.intellij.openapi.components.service
import com.intellij.openapi.vfs.LocalFileSystem

private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
//        val messageBusConnection = ApplicationManager.getApplication().messageBus.connect()
        project.messageBus.connect(project).subscribe(INITIALIZATION_TOPIC, object: InitializationListener {
            override fun initializationCompleted() {
//                project.service<CheckovInstallerService>().install(project)
                project.service<CheckovToolWindowManagerPanel>().subscribeToInternalEvents(project)
                project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()

            }

        })
        initializeProject(project)
//      project.service<ResultsCacheService>().setMockCheckovResultsFromExampleFile() // MOCK
        LOG.info("Startup activity finished")
    }

    private fun initializeProject(project: Project) {
//        PipCheckovService.setCheckovPath(project)
        val initializationService = InitializationService(project)
        initializationService.initializeProject()
//        initializeRepoName(project)
    }
}