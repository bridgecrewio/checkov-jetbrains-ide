package com.bridgecrew.services


import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import java.io.File

private val LOG = logger<CliService>()

@Service
class CliService {

    fun run(command: String, env: Array<String>? = null, dir: File? = null): String {
        val commandToPrint = replaceApiToken(command)
        LOG.info("Running command: $commandToPrint")
        val checkovProcess = Runtime.getRuntime().exec(command, env, dir)
        val result = checkovProcess.inputStream.bufferedReader().use { it.readText() }
        val error = checkovProcess.errorStream.bufferedReader().use { it.readText() }
        val checkovExitCode = checkovProcess.waitFor()

        if (checkovExitCode != 0) {
            println("Failed to run cli $commandToPrint")
            println(error)
            throw Exception("Failed to run cli command")
        }

        LOG.info("Finished running command: $commandToPrint")

        return result

    }

    private fun replaceApiToken (command: String): String{
        val apiToknIndex = command.indexOf("--bc-api-key")
        return if (apiToknIndex >= 0){
            val firstPos: Int = apiToknIndex + "--bc-api-key".length
            val lastPos: Int = command.indexOf("--repo-id", firstPos)
            command.substring(0, firstPos).toString() + " **-**-**-** " + command.substring(lastPos)
        } else{
            command
        }
    }
}
