package com.bridgecrew.ui.topPanel

import com.intellij.openapi.actionSystem.ActionGroup
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionToolbar
import javax.swing.JComponent

object CheckovActionToolbar {
    
    val actionToolBar: ActionToolbar

    init {
        val actionManager = ActionManager.getInstance()
        val actionGroupToolbar = actionManager.getAction("com.bridgecrew.checkovScanActions") as ActionGroup
        actionToolBar = actionManager.createActionToolbar("Checkov Action Toolbar", actionGroupToolbar, true)
    }

    fun setComponent(component: JComponent) {
        actionToolBar.setTargetComponent(component)
    }
}