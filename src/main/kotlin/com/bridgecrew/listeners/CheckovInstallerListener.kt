package com.bridgecrew.listeners
import com.bridgecrew.services.checkovService.CheckovService
import com.intellij.util.messages.Topic

interface CheckovInstallerListener {

    companion object {
        val INSTALLER_TOPIC =
            Topic.create("Checkov installer", CheckovInstallerListener::class.java)
    }

    fun installerFinished(serviceClass: CheckovService)
}
