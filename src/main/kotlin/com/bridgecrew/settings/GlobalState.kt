package com.bridgecrew.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil


@State(
    name = "com.bridgecrew.settings.CheckovGlobalState",
    storages = arrayOf(Storage("CheckovGlobalState.xml"))
)
class GlobalState() : PersistentStateComponent<GlobalState> {

    var accessKey: String = ""
    var secretKey: String = ""
    var certificate: String = ""
    var prismaURL: String = ""

    fun getInstance(): GlobalState? {
        return ApplicationManager.getApplication().getService(GlobalState::class.java)
    }

    override fun getState(): GlobalState = this

    override fun loadState(state: GlobalState) {
        XmlSerializerUtil.copyBean(state, this)
    }

}