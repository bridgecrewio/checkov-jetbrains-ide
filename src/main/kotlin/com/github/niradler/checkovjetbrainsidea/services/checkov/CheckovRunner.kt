package com.github.niradler.checkovjetbrainsidea.services.checkov

interface CheckovRunner {
    fun installOrUpdate() : Boolean
    fun run(filePath: String, extensionVersion: String, bcToken: String): String
}