package com.bridgecrew.ui

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;


class CheckovToolWindowFactory : ToolWindowFactory{
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val CheckovToolWindowPanel = CheckovToolWindowPanel(project)
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(CheckovToolWindowPanel.dividePanel(), null, false)
        contentManager.addContent(content)
    }

}