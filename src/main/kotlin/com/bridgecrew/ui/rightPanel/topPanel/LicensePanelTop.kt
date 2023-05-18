package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.CheckovUtils
import java.awt.GridBagConstraints
import javax.swing.JPanel

class LicensePanelTop(result: BaseCheckovResult): CheckovDescriptionPanelTop(result) {

    init {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        val title = createTitleAndIcon(CheckovUtils.createLicenseTitle(result), result.severity)
        add(title, gbc)

        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.gridx = 1
        val actions = createDescriptionPanelTitleActions()
        add(actions, gbc)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        createSuppressionButton(panel)
        return panel
    }
}