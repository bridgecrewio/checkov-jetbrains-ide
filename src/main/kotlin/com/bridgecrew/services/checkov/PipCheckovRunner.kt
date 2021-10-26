package com.bridgecrew.services.checkov

import com.bridgecrew.services.CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.nio.file.Paths

class PipCheckovRunner : CheckovRunner {
    private var checkovPath: String? = null

    private fun isCheckovInstalledGlobally(): Boolean {
        return try {
            val checkovVersionExitCode = Runtime.getRuntime().exec("checkov -v").waitFor()
            checkovVersionExitCode == 0
        } catch (err: Exception) {
            false
        }
    }

    private fun getPythonUserBasePath(): String {
        val pythonUserBaseExecProcess = Runtime.getRuntime().exec(arrayOf("python3", "-c", "import site; print(site.USER_BASE)"))
        val pythonUserBaseExitCode = pythonUserBaseExecProcess.waitFor()
        if (pythonUserBaseExitCode !== 0) {
            println("Failed to get python user base path.")
            println(pythonUserBaseExecProcess.errorStream.bufferedReader().use { it.readText() })
            throw Exception("Failed to get python user base path")
        }

        val pythonUserBase = pythonUserBaseExecProcess.inputStream.bufferedReader().use { it.readText().trim() }
        return Paths.get(pythonUserBase, "bin", "checkov").toString()
    }

    override fun installOrUpdate(project: Project): Boolean {
        try {
            if (isCheckovInstalledGlobally()) {
                println("checkov already installed globally")
                this.checkovPath = "checkov"
                return true
            }

            println("Trying to install Checkov using pip.")
            val installCommand = "pip3 install -U --user --verbose checkov -i https://pypi.org/simple/"
            project.service<CliService>().run(installCommand)

            this.checkovPath = this.getPythonUserBasePath()

            println("Checkov installed with pip successfully.")
            println("checkovPath: $checkovPath")

            println("Using checkov version: ${getVersion()}")

            return true
        } catch (err: Exception) {
            println("Failed to install Checkov using pip.")
            err.printStackTrace()
            return false
        }
    }

    override fun getExecCommand(filePath: String, bcToken: String, gitRepoName: String): String {
            return "${checkovPath} -s --bc-api-key $bcToken --repo-id $gitRepoName -f $filePath -o json"
    }

    private fun getVersion(): String {
        val checkovProcess = Runtime.getRuntime().exec("${this.checkovPath} -v")
        return checkovProcess.inputStream.bufferedReader().use { it.readText() }
    }
}