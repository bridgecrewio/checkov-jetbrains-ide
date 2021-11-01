package com.bridgecrew.ui

import com.bridgecrew.settings.CheckovSettingsConfigurable
import com.bridgecrew.utils.createGridRowCol
import com.intellij.openapi.application.ApplicationManager
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Insets
import javax.swing.*
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon

class CheckovSettingsPanel(project: Project): JPanel() {

    init {

        layout = GridLayoutManager(5, 1, Insets(0, 0, 0, 0), -1, -1)

        add(JLabel(getIcon("/icons/checkov_l.svg")), createGridRowCol(0,0, GridConstraints.ANCHOR_CENTER))
        add(JLabel("\n\n"),createGridRowCol(1,0, GridConstraints.ANCHOR_CENTER))

        add(JLabel("Checkov Plugin would scan your infrastructure as code files."), createGridRowCol(2,0, GridConstraints.ANCHOR_CENTER))

        add(JLabel("Add an API Token to start getting results."), createGridRowCol(3,0, GridConstraints.ANCHOR_CENTER))

        val settingsButton = JButton("Open Settings")

        settingsButton.addActionListener {
            ApplicationManager.getApplication().invokeLater {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, CheckovSettingsConfigurable::class.java)
            }
        }

        add(settingsButton, createGridRowCol(4,0, GridConstraints.ANCHOR_CENTER))

    }
}
