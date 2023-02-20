package com.bridgecrew.services.installation

import com.intellij.openapi.project.Project

class DockerInstallerCommandService : InstallerCommandService {

    override fun getInstallCommand(): ArrayList<String> {
        return arrayListOf("docker", "pull", "bridgecrew/checkov")
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf("docker", "run", "--rm", "--tty", "bridgecrew/checkov", "-v")
    }

    companion object {
        fun getCheckovImageIsRunningCommand(): ArrayList<String> {
            return arrayListOf("docker", "run", "--rm", "--tty", "bridgecrew/checkov", "-v")
        }
    }
}