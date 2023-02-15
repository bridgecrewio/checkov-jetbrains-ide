package com.bridgecrew.ui.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import javax.swing.JOptionPane

class CheckovScanAction : AnAction(AllIcons.Actions.Execute), DumbAware {

    override fun actionPerformed(e: AnActionEvent) {
        JOptionPane.showMessageDialog(null, "This is where we start scanning", "Alert", JOptionPane.INFORMATION_MESSAGE);
    }
}