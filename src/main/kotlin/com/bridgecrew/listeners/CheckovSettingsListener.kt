package com.bridgecrew.listeners
import com.bridgecrew.settings.CheckovSettingsState
import com.intellij.util.messages.Topic

interface CheckovSettingsListener {

    companion object {
        val SETTINGS_TOPIC =
            Topic.create("Checkov settings", CheckovSettingsListener::class.java)
    }

    fun settingsUpdated(newState: CheckovSettingsState?)

}
