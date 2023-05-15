package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.ui.buttons.FixButton
import com.intellij.ide.DataManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.awt.BorderLayout
import javax.swing.JPanel

class IacPanelTop(result: BaseCheckovResult): CheckovDescriptionPanelTop(result) {

    init {
        add(createTitleAndIcon(getTitle(result), result.severity), BorderLayout.WEST)
        add(createDescriptionPanelTitleActions(), BorderLayout.EAST)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        if (isShowDocumentationButton(result)) {
            panel.add(result.guideline?.let { DocumentationButton(it) })
        }

        createSuppressionButton(panel)

        if(result.fixDefinition != null){
            FixButton(result)
            panel.add(FixButton(result))
        }
        return panel
    }
}