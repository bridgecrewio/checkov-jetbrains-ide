package com.github.niradler.checkovjetbrainsidea.services.checkov

class DockerCheckovRunner: CheckovRunner {
    override fun installOrUpdate(): Boolean {
        try {
            println("Trying to install Checkov using Docker.")
            Runtime.getRuntime().exec("docker pull bridgecrew/checkov:latest")
            println("Checkov installed with Docker successfully.")
            return true
        } catch (err :Exception) {
            println("Failed to install Checkov using Docker.")
            err.printStackTrace()
            return false
        }
    }

    override fun run() {
        TODO("Not yet implemented")
    }
}