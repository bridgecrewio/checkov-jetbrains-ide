package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.CheckovUtils
import java.awt.BorderLayout
import javax.swing.JPanel

class LicensePanelTop(result: BaseCheckovResult): CheckovDescriptionPanelTop(result) {

    init {
        add(createTitleAndIcon(CheckovUtils.createLicenseTitle(result), result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        createSuppressionButton(panel)
        return panel
    }
}