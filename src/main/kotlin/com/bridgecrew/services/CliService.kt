package com.bridgecrew.services

open class CliService() {

    fun run(command: String): String {
        println("[CliService] running $command")
        val checkovProcess = Runtime.getRuntime().exec(command)
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

object CliServiceInstance : CliService() {
    init {
        println("CliServiceInstance invoked")
    }
}