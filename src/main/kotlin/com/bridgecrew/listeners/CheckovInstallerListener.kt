package com.bridgecrew.listeners
import com.intellij.util.messages.Topic

interface CheckovInstallerListener {

    companion object {
        val INSTALLER_TOPIC =
            Topic.create("Checkov installer", CheckovInstallerListener::class.java)
    }

    fun installerFinished()
}
