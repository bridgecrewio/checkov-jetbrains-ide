package com.bridgecrew.services.checkov

import com.bridgecrew.services.CliService
import com.bridgecrew.services.CliServiceInstance
import com.bridgecrew.ui.CheckovResourceTreeNode
import com.intellij.openapi.application.PathManager
import java.nio.file.Paths

import com.bridgecrew.ui.CheckovToolWindow
import com.bridgecrew.ui.CheckovToolWindowDescriptionPanel
import com.bridgecrew.ui.CheckovToolWindowTree
import kotlinx.coroutines.*


class PipCheckovRunner : CheckovRunner {
    private var checkovPath: String? = null
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private var runJobRunning: Job? = null
    private val cliService: CliService = CliServiceInstance

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

    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using pip.")
            val pipInstallProcess = Runtime.getRuntime().exec("pip3 install -U --user --verbose checkov -i https://pypi.org/simple/")
            pipInstallProcess.inputStream.bufferedReader().use { println(it.readText()) }
            val pipInstallExitCode = pipInstallProcess.waitFor()
            if (pipInstallExitCode !== 0) {
                println("Failed to install Checkov using pip.")
                println(pipInstallProcess.errorStream.bufferedReader().use { it.readText() })
                throw Exception("Failed to install Checkov using pip")
            }

            println("Checkov installed with pip successfully.")

            if (isCheckovInstalledGlobally()) {
                this.checkovPath = "checkov"
            } else {
                this.checkovPath = this.getPythonUserBasePath()
            }

            println("Using checkov version: ${getVersion()}")

            return true
        } catch (err: Exception) {
            println("Failed to install Checkov using pip.")
            err.printStackTrace()
            return false
        }
    }

    override fun run(filePath: String, extensionVersion: String, bcToken: String) = runBlocking {
        if (runJobRunning !== null) {
            println("cancelling current running scan job due to newer request")
            runJobRunning!!.cancel()
        }
        runJobRunning = scope.launch {
            val execCommand =
//                "${checkovPath} -s --skip-check ${SKIP_CHECKS.joinToString(",")} --bc-api-key $bcToken --repo-id yyacoby/terragoat-yuval -f $filePath -o json"
                "${checkovPath} -s --skip-check ${SKIP_CHECKS.joinToString(",")} -f $filePath -o json"
            val res = cliService.run(execCommand)
            if (isActive) {
                println(res)
                runJobRunning = null
            }
        }
    }

    private fun getVersion(): String {
        val checkovProcess = Runtime.getRuntime().exec("${this.checkovPath} -v")
        return checkovProcess.inputStream.bufferedReader().use { it.readText() }
    }
}