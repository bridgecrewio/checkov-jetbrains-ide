package com.bridgecrew.services.installation

import com.intellij.openapi.project.Project

interface InstallerService {
    fun getInstallCommand(): ArrayList<String>
    fun getVersion(project: Project): ArrayList<String>
}