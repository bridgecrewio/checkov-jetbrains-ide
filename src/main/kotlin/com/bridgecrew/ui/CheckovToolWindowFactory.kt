package com.bridgecrew.ui

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.Disposer

class CheckovToolWindowFactory : ToolWindowFactory{
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val CheckovToolWindoTreeNode = CheckovToolWindowTreeNode(project)
        val contentManager = toolWindow.contentManager
        val content = contentManager.factory.createContent(CheckovToolWindoTreeNode.createTree(), null, false)
        contentManager.addContent(content)
    }

}