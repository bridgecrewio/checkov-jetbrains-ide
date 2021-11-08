package com.bridgecrew.ui

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.util.Disposer


class CheckovToolWindowFactory : ToolWindowFactory{
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val checkovToolWindowPanel = CheckovToolWindowPanel(project)
        println("This is the current project from tool windos ${project.getBasePath()}")
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(checkovToolWindowPanel, null, false)
        contentManager.addContent(content)

        Disposer.register(project, checkovToolWindowPanel)
    }

}