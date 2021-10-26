package com.bridgecrew.services.checkov

import com.bridgecrew.services.CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

class PipenvCheckovRunner : CheckovRunner {
    private var checkovPath: String? = null

    override fun installOrUpdate(project: Project): Boolean {
        try {
            println("Trying to install Checkov using pipenv.")
            val command = "pipenv --python 3 install checkov"
            val res = project.service<CliService>().run(command)
            println("pipenv install command output: $res")

            val pipenvPath = project.service<CliService>().run("pipenv run which python")
            val checkovPathArray: MutableList<String> = pipenvPath.split('/').toMutableList()
            checkovPathArray.removeLast()
            checkovPathArray.add("checkov")
            checkovPath = checkovPathArray.joinToString(separator = "/")
            println("Setting checkovPath: $checkovPath")

            println("Checkov installed with pipenv successfully.")
            println("Using checkov version: ${getVersion(project)}")

            return true
        } catch (e: Exception) {
            println("Failed to install Checkov using pipenv.")
            e.printStackTrace()
            return false
        }
    }

    override fun getExecCommand(filePath: String, extensionVersion: String, bcToken: String, gitRepoName: String): String {
        return "${checkovPath} -s --bc-api-key $bcToken --repo-id $gitRepoName -f $filePath -o json"
    }

    private fun getVersion(project: Project): String {
        return project.service<CliService>().run("$checkovPath -v")
    }

}