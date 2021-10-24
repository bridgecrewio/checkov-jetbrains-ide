package com.bridgecrew.activities
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project

import com.bridgecrew.services.CheckovServiceInstance

class PostStartupActivity : StartupActivity {
    private val checkov = CheckovServiceInstance

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup()
//        this.scanProjectOnStartUp(project)
        println("startup finished")
    }

    private fun scanProjectOnStartUp(project: Project) {
        println("[scanProjectOnStartUp] scanning project")
        val results = this.checkov.scanFile("/Users/yyacoby/repos/terragoat/terraform/aws/ec2.tf", "unknown", "09f77e61-3c9a-4325-ace9-6210dc576c1a")
        println(results);
    }

    private fun installCheckovOnStartup() {
        this.checkov.installCheckov()
    }
}