package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.buttons.SuppressionButton
import java.awt.BorderLayout
import javax.swing.JPanel

class LicensePanelTop(val result: BaseCheckovResult): CheckovDescriptionPanelTop() {

    init {
        add(createTitleAndIcon(result.name, result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        panel.add(SuppressionButton(result))
        return panel
    }
}