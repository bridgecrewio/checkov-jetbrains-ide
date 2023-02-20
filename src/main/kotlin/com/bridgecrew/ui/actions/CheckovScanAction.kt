package com.bridgecrew.ui.actions

import com.bridgecrew.services.scan.CheckovScanService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware
import javax.swing.JOptionPane

class CheckovScanAction : AnAction(AllIcons.Actions.Execute), DumbAware {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        JOptionPane.showMessageDialog(null, "This is where we start scanning", "Alert", JOptionPane.INFORMATION_MESSAGE)
        val project = actionEvent.project
        project?.service<CheckovScanService>()?.scanProject(project)
    }
}