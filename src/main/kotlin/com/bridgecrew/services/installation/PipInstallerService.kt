package com.bridgecrew.services.installation

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipInstallerService: InstallerService {
    override fun getInstallCommand(): ArrayList<String> {
        return arrayListOf("pip3", "install", "-U", "--user", "checkov", "-i", "https://pypi.org/simple/")
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath, "-v")
    }
}