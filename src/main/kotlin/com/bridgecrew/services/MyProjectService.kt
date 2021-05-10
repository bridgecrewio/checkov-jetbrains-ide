package com.bridgecrew.services

import com.bridgecrew.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
        val checkovService = CheckovService()
        checkovService.installCheckov()
    }
}
