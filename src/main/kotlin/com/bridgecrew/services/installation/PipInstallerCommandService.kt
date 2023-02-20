package com.bridgecrew.services.installation

import CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipInstallerCommandService : InstallerCommandService {

    override fun getInstallCommand(): ArrayList<String> {
        return arrayListOf("pip3", "install", "-U", "--user", "checkov", "-i", "https://pypi.org/simple/")
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf(project.service<CliService>().checkovPath, "-v")
    }

    companion object {
        fun getWinCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("pip3", "show", "checkov")
        }

        fun getUnixCommandsForFindingCheckovPath(): ArrayList<String> {
            return arrayListOf("python3", "-c", "import site; print(site.USER_BASE)")
        }
    }
}