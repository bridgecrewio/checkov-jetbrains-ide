package com.bridgecrew.services.installation

import com.intellij.openapi.project.Project

interface InstallerCommandService {
    fun getInstallCommand(): ArrayList<String>
    fun getVersion(project: Project): ArrayList<String>
}