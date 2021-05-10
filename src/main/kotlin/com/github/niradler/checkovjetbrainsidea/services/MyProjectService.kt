package com.github.niradler.checkovjetbrainsidea.services

import com.github.niradler.checkovjetbrainsidea.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
        val checkovService  = CheckovService()
        checkovService.installCheckov()
        checkovService.scanFile()
    }
}
