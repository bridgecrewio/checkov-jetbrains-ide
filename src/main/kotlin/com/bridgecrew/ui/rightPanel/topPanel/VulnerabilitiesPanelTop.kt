package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.ui.buttons.FixButton
import com.bridgecrew.ui.buttons.FixCVEButton
import com.bridgecrew.ui.buttons.SuppressionButton
import com.bridgecrew.utils.isCustomPolicy
import java.awt.BorderLayout
import javax.swing.JPanel

class VulnerabilitiesPanelTop(val result: VulnerabilityCheckovResult): CheckovDescriptionPanelTop() {

    init {
        add(createTitleAndIcon(getTitle(result), result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        if(! isCustomPolicy(result)){
            panel.add(DocumentationButton(result.guideline, result.id))
        }
        panel.add(SuppressionButton(result.id))
        if(result.fixDefinition != null){
            panel.add(FixCVEButton(result.id))
            panel.add(FixButton(result))
        }
        return panel
    }
}