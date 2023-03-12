package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.ui.buttons.FixButton
import com.bridgecrew.ui.buttons.SuppressionButton
import java.awt.BorderLayout
import javax.swing.JPanel

class IacPanelTop(val result: BaseCheckovResult): CheckovDescriptionPanelTop() {

    init {
        add(createTitleAndIcon(getTitle(result), result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        if(isShowDocumentationButton(result)){
            panel.add(result.guideline?.let { DocumentationButton(it) })
        }
        panel.add(SuppressionButton(result))
        if(result.fixDefinition != null){
            panel.add(FixButton(result))
        }
        return panel
    }
}