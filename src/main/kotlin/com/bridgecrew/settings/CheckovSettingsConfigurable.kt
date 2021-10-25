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
                !checkovSettingsComponent.checkovVersionField.text.equals(settings?.checkovVersion)
    }

    override fun apply() {
        val setting = CheckovSettingsState().getInstance()
        setting?.apiToken = checkovSettingsComponent.apiTokenField.text
        setting?.certificate = checkovSettingsComponent.certificateField.text
        setting?.checkovVersion = checkovSettingsComponent.checkovVersionField.text
    }

    override fun reset() {
        val setting = CheckovSettingsState().getInstance()
        checkovSettingsComponent.apiTokenField.text = setting?.apiToken
        checkovSettingsComponent.certificateField.text = setting?.certificate
        checkovSettingsComponent.checkovVersionField.text = setting?.checkovVersion


    }



}