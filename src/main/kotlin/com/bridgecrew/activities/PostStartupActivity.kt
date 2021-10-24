package com.bridgecrew.activities
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project

import com.bridgecrew.services.CheckovServiceInstance

class PostStartupActivity : StartupActivity {
    private val checkov = CheckovServiceInstance

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup()
        println("startup finished")
    }

    private fun installCheckovOnStartup() {
        checkov.installCheckov()
    }
}