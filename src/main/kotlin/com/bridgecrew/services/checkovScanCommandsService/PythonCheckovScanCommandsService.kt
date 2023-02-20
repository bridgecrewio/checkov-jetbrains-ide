package com.bridgecrew.services.checkovScanCommandsService

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils

class PythonCheckovScanCommandsService(project: Project) : CheckovScanCommandsService(project) {
    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath)
    }

    override fun getDirectory(): String {
        return FilenameUtils.separatorsToSystem(project.basePath!!)
    }

    override fun getFilePath(originalFilePath: String): String {
        return FilenameUtils.separatorsToSystem(originalFilePath)
    }
}