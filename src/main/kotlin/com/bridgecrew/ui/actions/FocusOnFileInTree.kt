package com.bridgecrew.ui.actions

import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.PANELTYPE
import com.intellij.ide.DataManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

class FocusOnFileInTree(val filePath: String) : ActionListener {

    override fun actionPerformed(e: ActionEvent?) {
        val dataContext = DataManager.getInstance().dataContext
        val project = dataContext.getData("project") as Project
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED, filePath)
    }
}