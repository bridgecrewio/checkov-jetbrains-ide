package com.github.niradler.checkovjetbrainsidea.services

import com.github.niradler.checkovjetbrainsidea.services.checkov.CheckovRunner
import com.github.niradler.checkovjetbrainsidea.services.checkov.DockerCheckovRunner
import com.github.niradler.checkovjetbrainsidea.services.checkov.PipCheckovRunner

class CheckovService {
    private var selectedCheckovRunner: CheckovRunner? = null
    private var checkovRunners = arrayOf(DockerCheckovRunner(), PipCheckovRunner())

    init {
        for (runner in checkovRunners) {
            var isCheckovInstalled = runner.installOrUpdate()
            if (isCheckovInstalled) {
                this.selectedCheckovRunner = runner
                break
            }
        }

        if (selectedCheckovRunner == null) {
            throw Exception("Could not install Checkov.")
        }
    }
}