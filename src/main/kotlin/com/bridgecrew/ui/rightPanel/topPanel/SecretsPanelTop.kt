package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.buttons.FixButton
import com.bridgecrew.ui.buttons.SuppressionButton
import java.awt.BorderLayout
import javax.swing.JPanel

class SecretsPanelTop(val result: BaseCheckovResult): CheckovDescriptionPanelTop() {

    init {
        add(createTitleAndIcon(result.name, result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        panel.add(SuppressionButton(result.id))
        if(result.fixDefinition != null){
            panel.add(FixButton(result))
        }
        return panel
    }
}