package com.bridgecrew.ui.buttons

import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.ui.actions.FixAction
import com.intellij.ide.DataManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import javax.swing.JButton


class FixButton(val result: BaseCheckovResult) : JButton() {

    init {
        text = "Fix"
        addActionListener(FixAction(this, result))
    }
}