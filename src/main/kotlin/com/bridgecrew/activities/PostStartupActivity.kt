package com.bridgecrew.activities
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.bridgecrew.services.CheckovServiceInstance

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup(project)
        println("startup finished")
    }

    private fun installCheckovOnStartup(project: Project) {
        val checkov = CheckovServiceInstance
        checkov.installCheckov(project)
        println("[installCheckovOnStartup] Using checkov version ${checkov.getVersion()}")
    }
}