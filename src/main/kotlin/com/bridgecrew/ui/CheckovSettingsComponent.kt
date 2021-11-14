package com.bridgecrew.ui

import com.bridgecrew.utils.createGridRowCol
import javax.swing.JPanel
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Insets
import javax.swing.JLabel
import javax.swing.JTextField

class CheckovSettingsComponent () {
    private var rootPanel: JPanel = JPanel()
    val apiTokenField: JTextField = JTextField()
    val certificateField: JTextField = JTextField()
    val prismaURLField: JTextField = JTextField()

    init {
        rootPanel.layout = GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1)
        val settingsPanel = JPanel(GridLayoutManager(3, 2, Insets(0, 0, 0, 0), -1, -1))

        val apiTokenLabel = JLabel("Token (Required)")
        apiTokenLabel.labelFor = apiTokenField
        settingsPanel.add(apiTokenLabel, createGridRowCol(0,0,GridConstraints.ANCHOR_WEST))
        settingsPanel.add(apiTokenField, createGridRowCol(0,1,GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL))

        val prismaURLLabel = JLabel("Prisma URL ( Required if using Prisma Cloud Access Token)")
        prismaURLLabel.labelFor = prismaURLField
        settingsPanel.add(prismaURLLabel, createGridRowCol(1,0,GridConstraints.ANCHOR_WEST))
        settingsPanel.add(prismaURLField, createGridRowCol(1,1,GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL))

        val certificateLabel = JLabel("CA-Certificate")
        certificateLabel.labelFor = certificateField
        settingsPanel.add(certificateLabel, createGridRowCol(2,0,GridConstraints.ANCHOR_WEST))
        settingsPanel.add(certificateField, createGridRowCol(2,1,GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL))

        rootPanel.add(settingsPanel, GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_NORTH,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false
        ))

    }


    fun getPanel(): JPanel {
        return rootPanel
    }

}