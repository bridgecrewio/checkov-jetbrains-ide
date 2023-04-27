package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import java.awt.BorderLayout
import javax.swing.JPanel

class LicensePanelTop(result: BaseCheckovResult): CheckovDescriptionPanelTop(result) {

    init {
        add(createTitleAndIcon(result.name, result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        createSuppressionButton(panel)
        return panel
    }
}