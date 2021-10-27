package com.bridgecrew.services.checkov

import com.intellij.openapi.project.Project

val SKIP_CHECKS = arrayOf("CKV_AWS_52")

interface CheckovRunner {
    fun installOrUpdate(project: Project): Boolean
    fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String): String
}