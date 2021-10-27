package com.bridgecrew.ui

import com.bridgecrew.settings.CheckovSettingsConfigurable
import com.bridgecrew.utils.getOffsetByLines
import com.bridgecrew.utils.updateFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.IdeBorderFactory
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Insets
import javax.swing.*
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project

class CheckovSettingsPanel(project: Project): JPanel() {

    init {

        layout = GridLayoutManager(3, 1, Insets(0, 0, 0, 0), -1, -1)

//        add(JLabel(SnykIcons.LOGO), baseGridConstraints(0))

        add(JLabel("Welcome to Checkov Jetbrains Plugin"), GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false
        ))

        add(JLabel("Please add an Api Token to start scanning your code"), GridConstraints(
            1,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false
        ))

        val settingsButton = JButton("Go To Checkov Settings")

        settingsButton.addActionListener {
            ApplicationManager.getApplication().invokeLater {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, CheckovSettingsConfigurable::class.java)
            }
        }

        add(settingsButton, GridConstraints(
            2,
            0,
            1,
            1,
            GridConstraints.ANCHOR_CENTER,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_WANT_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false
        ))

    }
}
