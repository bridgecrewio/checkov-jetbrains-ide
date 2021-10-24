package com.bridgecrew.services.checkov

val SKIP_CHECKS = arrayOf("CKV_AWS_52")

interface CheckovRunner {
    fun installOrUpdate(): Boolean
    fun run(filePath: String, extensionVersion: String, bcToken: String)
}