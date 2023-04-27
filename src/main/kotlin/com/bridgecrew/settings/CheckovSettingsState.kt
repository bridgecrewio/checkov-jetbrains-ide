package com.bridgecrew.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil


@State(
    name = "com.bridgecrew.settings.CheckovSettingsState",
    storages = arrayOf(Storage("CheckovSettingsState.xml"))
)
class CheckovSettingsState() : PersistentStateComponent<CheckovSettingsState> {

    var accessKey: String = ""
    var secretKey: String = ""
    var certificate: String = ""
    var prismaURL: String = ""

    fun getApiKey(): String {
        if(accessKey.isNotEmpty() && secretKey.isNotEmpty()){
            return "$accessKey::$secretKey"
        }

        return ""
    }

    fun getInstance(): CheckovSettingsState? {
        return ApplicationManager.getApplication().getService(CheckovSettingsState::class.java)
    }

    override fun getState(): CheckovSettingsState = this

    override fun loadState(state: CheckovSettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }


}