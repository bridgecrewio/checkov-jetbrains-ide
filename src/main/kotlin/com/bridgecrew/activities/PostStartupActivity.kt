package com.bridgecrew.activities
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project

import com.bridgecrew.services.CheckovServiceInstance

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup()
        println("startup finished")
    }

    private fun installCheckovOnStartup() {
        val checkov = CheckovServiceInstance
        checkov.installCheckov()
        println("[installCheckovOnStartup] Using checkov version ${checkov.getVersion()}")
    }
}