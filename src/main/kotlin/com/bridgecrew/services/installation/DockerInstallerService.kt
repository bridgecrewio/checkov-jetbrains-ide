package com.bridgecrew.services.installation

import com.intellij.openapi.project.Project

class DockerInstallerService: InstallerService {
    override fun getInstallCommand(): ArrayList<String> {
        return arrayListOf("docker", "pull", "bridgecrew/checkov")
    }

    override fun getVersion(project: Project): ArrayList<String> {
        return arrayListOf("docker", "run", "--rm", "--tty", "bridgecrew/checkov", "-v")
    }

    // check if docker is installed in computer
    // if yes - pull image and define service
    // if not - move to python
}