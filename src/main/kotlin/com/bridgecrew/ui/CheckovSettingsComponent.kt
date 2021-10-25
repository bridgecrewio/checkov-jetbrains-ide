package com.bridgecrew.ui

import com.intellij.ui.IdeBorderFactory
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
    val checkovVersionField: JTextField = JTextField()

    init {
        rootPanel.layout = GridLayoutManager(2, 2, Insets(0, 0, 0, 0), -1, -1)

        val mandatorySettingsPanel = JPanel(GridLayoutManager(1, 2, Insets(0, 0, 0, 0), -1, -1))
        mandatorySettingsPanel.border = IdeBorderFactory.createTitledBorder("Mandatory settings")

        val apiTokenLabel = JLabel("Checkov: Token")
        apiTokenLabel.labelFor = apiTokenField
        mandatorySettingsPanel.add(
            apiTokenLabel,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        mandatorySettingsPanel.add(
            apiTokenField,
            GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )
        rootPanel.add(mandatorySettingsPanel, GridConstraints(
            0,
            0,
            1,
            1,
            GridConstraints.ANCHOR_NORTHWEST,
            GridConstraints.FILL_HORIZONTAL,
            GridConstraints.SIZEPOLICY_CAN_SHRINK or GridConstraints.SIZEPOLICY_CAN_GROW,
            GridConstraints.SIZEPOLICY_FIXED,
            null,
            null,
            null,
            0,
            false
        ))

        val optionalSettingsPanel = JPanel(GridLayoutManager(2, 2, Insets(0, 0, 0, 0), -1, -1))
        optionalSettingsPanel.border = IdeBorderFactory.createTitledBorder("Optional Settings")


        val certificateLabel = JLabel("Checkov: Certificate")
        certificateLabel.labelFor = certificateField
        optionalSettingsPanel.add(
            certificateLabel,
            GridConstraints(
                0,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        optionalSettingsPanel.add(
            certificateField,
            GridConstraints(
                0,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        val checkovVersionLabel = JLabel("Checkov: Checkov Version")
        checkovVersionLabel.labelFor = checkovVersionField
        optionalSettingsPanel.add(
            checkovVersionLabel,
            GridConstraints(
                1,
                0,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_NONE,
                GridConstraints.SIZEPOLICY_FIXED,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        optionalSettingsPanel.add(
            checkovVersionField,
            GridConstraints(
                1,
                1,
                1,
                1,
                GridConstraints.ANCHOR_WEST,
                GridConstraints.FILL_HORIZONTAL,
                GridConstraints.SIZEPOLICY_WANT_GROW,
                GridConstraints.SIZEPOLICY_FIXED,
                null,
                null,
                null,
                0,
                false
            )
        )

        rootPanel.add(optionalSettingsPanel, GridConstraints(
            1,
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