package com.bridgecrew.services.checkov

import com.intellij.openapi.project.Project

val SKIP_CHECKS = arrayOf("CKV_AWS_52")

interface CheckovRunner {
    fun installOrUpdate(): Boolean
    fun run(filePath: String, extensionVersion: String, bcToken: String)
}