package com.bridgecrew.services.checkovRunner

import com.intellij.openapi.project.Project

interface CheckovRunner {
    fun getInstallCommand(project: Project): ArrayList<String>
    fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String):ArrayList<String>
    fun getVersion(project: Project): ArrayList<String>
    }