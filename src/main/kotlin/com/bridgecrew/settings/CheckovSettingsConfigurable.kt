package com.bridgecrew.settings

import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.ui.CheckovSettingsComponent
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import javax.swing.JComponent

class CheckovSettingsConfigurable(val project: Project) : Configurable {

    private val checkovSettingsComponent = CheckovSettingsComponent()

    override fun getDisplayName(): String = "Checkov"

    override fun createComponent(): JComponent {
        return checkovSettingsComponent.getPanel()
    }

    override fun isModified(): Boolean {
        val settings = CheckovSettingsState().getInstance()
        return !checkovSettingsComponent.accessKeyField.text.equals(settings?.accessKey) ||
                !checkovSettingsComponent.secretKeyField.text.equals(settings?.secretKey) ||
                !checkovSettingsComponent.certificateField.text.equals(settings?.certificate) ||
                !checkovSettingsComponent.prismaURLField.text.equals(settings?.prismaURL)
    }

    override fun apply() {
        val settings = CheckovSettingsState().getInstance()

        val secretKeyModified = !checkovSettingsComponent.accessKeyField.text.equals(settings?.accessKey)
        val accessKeyModified = !checkovSettingsComponent.secretKeyField.text.equals(settings?.secretKey)

        settings?.secretKey = checkovSettingsComponent.secretKeyField.text.trim()
        settings?.accessKey = checkovSettingsComponent.accessKeyField.text.trim()
        settings?.certificate = checkovSettingsComponent.certificateField.text.trim()
        settings?.prismaURL = checkovSettingsComponent.prismaURLField.text.trim()

        if (accessKeyModified || secretKeyModified){
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
        }
    }

    override fun reset() {
        val setting = CheckovSettingsState().getInstance()
        checkovSettingsComponent.accessKeyField.text = setting?.accessKey
        checkovSettingsComponent.secretKeyField.text = setting?.secretKey
        checkovSettingsComponent.certificateField.text = setting?.certificate
        checkovSettingsComponent.prismaURLField.text = setting?.prismaURL


    }



}