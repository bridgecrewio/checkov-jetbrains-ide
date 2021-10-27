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
        return !checkovSettingsComponent.apiTokenField.text.equals(settings?.apiToken) ||
                !checkovSettingsComponent.certificateField.text.equals(settings?.certificate) ||
                !checkovSettingsComponent.prismaURLField.text.equals(settings?.prismaURL)
    }

    override fun apply() {
        val settings = CheckovSettingsState().getInstance()
        val apiTokenModified = !checkovSettingsComponent.apiTokenField.text.equals(settings?.apiToken)
        settings?.apiToken = checkovSettingsComponent.apiTokenField.text
        settings?.certificate = checkovSettingsComponent.certificateField.text
        settings?.prismaURL = checkovSettingsComponent.prismaURLField.text
        if (apiTokenModified){
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
        }

    }

    override fun reset() {
        val setting = CheckovSettingsState().getInstance()
        checkovSettingsComponent.apiTokenField.text = setting?.apiToken
        checkovSettingsComponent.certificateField.text = setting?.certificate
        checkovSettingsComponent.prismaURLField.text = setting?.prismaURL


    }



}