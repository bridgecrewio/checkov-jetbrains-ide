package com.bridgecrew.ui

import javax.swing.JPanel
import java.awt.*
import javax.swing.JLabel
import javax.swing.JTextField

class CheckovSettingsComponent() {
    private var rootPanel: JPanel = JPanel()
    val secretKeyField: JTextField = JTextField()
    val accessKeyField: JTextField = JTextField()
    val certificateField: JTextField = JTextField()
    val prismaURLField: JTextField = JTextField()

    init {
        rootPanel.layout = GridBagLayout()
        val settingsPanel = JPanel(GridBagLayout())

        val constraints = GridBagConstraints()
        constraints.fill = GridBagConstraints.HORIZONTAL
        constraints.anchor = GridBagConstraints.NORTHWEST
        constraints.insets = Insets(0, 0, 5, 20)

        createSettingsRow(settingsPanel, constraints, "Access Key (Required):", accessKeyField, 0)
        createSettingsRow(settingsPanel, constraints, "Secret Key (Required):", secretKeyField, 1)
        createSettingsRow(settingsPanel, constraints, "Prisma URL (Required):", prismaURLField, 2)
        createSettingsRow(settingsPanel, constraints, "CA-Certificate:", certificateField, 3)

        constraints.gridx = 0
        constraints.gridy = 0
        constraints.fill = GridBagConstraints.VERTICAL
        rootPanel.add(settingsPanel, constraints)

        //add an empty panel to place the settings on the top left
        constraints.gridy = 1
        constraints.weighty = 1.0
        constraints.weightx = 1.0
        rootPanel.add(JPanel(), constraints)
    }

    private fun createSettingsRow(settingsPanel: JPanel, constraints: GridBagConstraints, keyText: String, inputField: JTextField, gridY: Int) {
        constraints.gridx = 0
        constraints.gridy = gridY
        constraints.ipady = 10
        val accessKeyLabel = JLabel(keyText)
        accessKeyLabel.labelFor = accessKeyField
        settingsPanel.add(accessKeyLabel, constraints)
        constraints.gridx = 1
        constraints.ipady = 0
        settingsPanel.add(inputField, constraints)
    }

    fun getPanel(): JPanel {
        return rootPanel
    }
}