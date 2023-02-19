package com.bridgecrew.services.installation

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipenvInstallerService: InstallerService {
    override fun getInstallCommand(): ArrayList<String> {
        return arrayListOf("pipenv","--python","3","install","checkov")
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath,"-v")
    }
}