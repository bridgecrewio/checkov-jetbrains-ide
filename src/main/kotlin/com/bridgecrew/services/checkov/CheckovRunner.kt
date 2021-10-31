package com.bridgecrew.services.checkov

import com.intellij.openapi.project.Project

interface CheckovRunner {
    fun installOrUpdate(project: Project): Boolean
    fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, pluginVersion: String): String
}