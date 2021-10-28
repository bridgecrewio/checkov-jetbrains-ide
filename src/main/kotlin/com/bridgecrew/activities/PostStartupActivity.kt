package com.bridgecrew.activities
import com.bridgecrew.services.CheckovService
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.intellij.openapi.diagnostic.logger

import com.intellij.openapi.components.service
private val LOG = logger<PostStartupActivity>()

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        LOG.info("Startup activity starting")
        this.installCheckovOnStartup(project)
        LOG.info("Startup activity finished")
    }

    private fun installCheckovOnStartup(project: Project) {
        LOG.info("Checkov Installation starting")
        project.service<CheckovService>().installCheckov(project)
        LOG.info("Checkov Installation finished")
    }
}