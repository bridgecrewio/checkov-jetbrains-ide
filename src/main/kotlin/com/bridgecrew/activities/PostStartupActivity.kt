package com.bridgecrew.activities
import com.bridgecrew.services.CheckovService
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project

import com.intellij.openapi.components.service

class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup(project)
        println("startup finished")
    }

    private fun installCheckovOnStartup(project: Project) {
       project.service<CheckovService>().installCheckov(project)
    }
}