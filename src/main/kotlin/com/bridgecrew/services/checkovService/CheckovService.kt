package com.bridgecrew.services.checkovService

import com.intellij.openapi.project.Project

interface CheckovService {
    fun getInstallCommand(project: Project): ArrayList<String>
    fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String):ArrayList<String>
    fun getVersion(project: Project): ArrayList<String>
    }