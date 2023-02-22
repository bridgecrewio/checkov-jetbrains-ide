package com.bridgecrew.ui.actions

import com.bridgecrew.services.scan.CheckovScanService
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAware

class CheckovScanAction : AnAction(AllIcons.Actions.Execute), DumbAware {

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val project = actionEvent.project
        project?.service<CheckovScanService>()?.scanProject(project)
    }
}