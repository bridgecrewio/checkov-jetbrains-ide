package com.github.niradler.checkovjetbrainsidea.services.checkov

import java.nio.file.Paths

class PipCheckovRunner: CheckovRunner {
    private var checkovPath: String? = null

    private fun isCheckovInstalledGlobally(): Boolean {
        val checkovVersionExitCode = Runtime.getRuntime().exec("checkov -v").waitFor()
        return checkovVersionExitCode == 0
    }

    private fun getPythonUserBasePath(): String {
        val execProcess = Runtime.getRuntime().exec("python3 -c \'import site; print(site.USER_BASE)\'")
        execProcess.waitFor()
        val pythonUserBase = execProcess.outputStream.toString().trim()
        return Paths.get(pythonUserBase, "bin", "checkov").toString()
    }

    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using pip3.")
            val pipInstallProcess = Runtime.getRuntime().exec("pip3 install -U --user --verbose checkov -i https://pypi.org/simple/")
            val pipInstallExitCode = pipInstallProcess.waitFor()

            if (pipInstallExitCode !== 0) {
                println("Failed to Docker pull Checkov Image.")
                println(pipInstallProcess.errorStream.bufferedReader().use { it.readText() })
                throw Exception("Failed to pip install Checkov")
            }

            println("Checkov installed with pip3 successfully.")

            if (isCheckovInstalledGlobally()) {
                this.checkovPath = "checkov"
            } else {
                this.checkovPath = this.getPythonUserBasePath()
            }

            return true
        } catch (err :Exception) {
            println("Failed to install Checkov using pip3.")
            err.printStackTrace()
            return false
        }
    }

    override fun run(filePath: String, extensionVersion: String, bcToken: String): String {
        TODO("Not yet implemented")
    }
}