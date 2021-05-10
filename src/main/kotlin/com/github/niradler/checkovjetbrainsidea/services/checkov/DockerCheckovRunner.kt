package com.github.niradler.checkovjetbrainsidea.services.checkov

import org.apache.commons.io.FilenameUtils
import java.io.File
import java.nio.file.Paths

const val DOCKER_MOUNT_DIR = "/checkovScan"
val SKIP_CHECKS = arrayOf("CKV_AWS_52")

class DockerCheckovRunner : CheckovRunner {
    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using Docker.")
            val dockerPullProcess = Runtime.getRuntime().exec("docker pull bridgecrew/checkov:latest")
            var dockerPullExitCode = dockerPullProcess.waitFor()

            if (dockerPullExitCode != 0) {
                println("Failed to Docker pull Checkov Image.")
                println(dockerPullProcess.errorStream.bufferedReader().use { it.readText() })
                throw Exception("Failed to Docker pull Checkov Image")
            }

            println("Checkov installed with Docker successfully.")
            return true
        } catch (err: Exception) {
            println("Failed to install Checkov using Docker.")
            err.printStackTrace()
            return false
        }
    }

    override fun run(filePath: String, extensionVersion: String, bcToken: String): String {
        println("Trying file scan using Docker.")
        val scannedFileDirectory = File(filePath).parent.toString()
        val dockerParams = "docker run --rm --tty --env BC_SOURCE=vscode --env BC_SOURCE_VERSION=${extensionVersion} --volume ${scannedFileDirectory}:${DOCKER_MOUNT_DIR} bridgecrew/checkov"
        println("Docker params: $dockerParams")

        val fileName = Paths.get(filePath).fileName.toString()
        val dockerFilePath = Paths.get(DOCKER_MOUNT_DIR, fileName).toString()
        val unixDockerFilPath = FilenameUtils.separatorsToUnix(dockerFilePath)
        val checkovParams = "-s --skip-check ${SKIP_CHECKS.joinToString(",")} --bc-api-key $bcToken --repo-id vscode/extension -f $unixDockerFilPath -o json"
        println("Checkov params: $checkovParams")

        val execCommand = "$dockerParams $checkovParams"
        println("Exec: $execCommand")
        val dockerCheckovProcess = Runtime.getRuntime().exec(execCommand)
        var dockerCheckovExitCode = dockerCheckovProcess.waitFor()

        if (dockerCheckovExitCode != 0) {
            println("Failed to run Checkov using Docker.")
            println(dockerCheckovProcess.errorStream.bufferedReader().use { it.readText() })
            throw Exception("Failed to run Checkov using Docker")
        }

        return dockerCheckovProcess.inputStream.bufferedReader().use { it.readText() }
    }
}