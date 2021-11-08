package com.bridgecrew.listeners
import com.bridgecrew.services.checkovRunner.CheckovRunner
import com.intellij.util.messages.Topic
import org.junit.runner.Runner
import kotlin.reflect.KClass

interface CheckovInstallerListener {

    companion object {
        val INSTALLER_TOPIC =
            Topic.create("Checkov installer", CheckovInstallerListener::class.java)
    }

    fun installerFinished(runnerClass: CheckovRunner)
}
