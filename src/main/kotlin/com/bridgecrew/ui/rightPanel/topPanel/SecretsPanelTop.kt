package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.SecretsCheckovResult
import com.bridgecrew.ui.buttons.FixButton
import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.getFileType
import java.awt.GridBagConstraints
import javax.swing.JPanel

class SecretsPanelTop(result: BaseCheckovResult): CheckovDescriptionPanelTop(result) {

    init {
        val gbc = GridBagConstraints()
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        val title = createTitleAndIcon((result as SecretsCheckovResult).checkName, result.severity)
        add(title, gbc)

        gbc.fill = GridBagConstraints.NONE
        gbc.weightx = 0.0
        gbc.gridx = 1
        val actions = createDescriptionPanelTitleActions()
        add(actions, gbc)
    }

    private fun createDescriptionPanelTitleActions(): JPanel {
        val panel = createActionsPanel()
        if (shouldEnableSuppressionButton()) {
            createSuppressionButton(panel)
        }
        if (result.fixDefinition != null) {
            panel.add(FixButton(result))
        }
        return panel
    }

    // TODO - Other file types should be supported later, waiting for Secrets team to implement this logic, currently will be disabled for Alpha version
    private fun shouldEnableSuppressionButton(): Boolean {
        val fileType = getFileType(result.filePath)
        return (fileType == FileType.TERRAFORM || fileType == FileType.YAML)
    }
}