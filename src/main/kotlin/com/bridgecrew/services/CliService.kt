package com.bridgecrew.services

import com.intellij.openapi.components.Service
import java.io.File

@Service
class CliService() {

    fun run(command: String, env: Array<String>? = null, dir: File? = null): String {
        println("[CliService] running $command")
        val checkovProcess = Runtime.getRuntime().exec(command, env, dir)
        val result = checkovProcess.inputStream.bufferedReader().use { it.readText() }
        val error = checkovProcess.errorStream.bufferedReader().use { it.readText() }
        val checkovExitCode = checkovProcess.waitFor()

        if (checkovExitCode != 0) {
            println("Failed to run cli $command")
            println(error)
            throw Exception("Failed to run cli command")
        }

        println("finish running $command")

        return result

    }

}