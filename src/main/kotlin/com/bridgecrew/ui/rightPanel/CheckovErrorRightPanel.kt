package com.bridgecrew.ui.rightPanel

import com.bridgecrew.results.*
import com.bridgecrew.ui.rightPanel.extraInfoPanel.*
import com.bridgecrew.ui.rightPanel.topPanel.*
import com.intellij.util.ui.UIUtil
import javax.swing.*

class CheckovErrorRightPanel(var result: BaseCheckovResult): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        background = UIUtil.getEditorPaneBackground() ?: background
        add(createTitlePanel())
        add(JSeparator(JSeparator.HORIZONTAL))
        add(createExtraInfoPanel())
    }

    private fun createTitlePanel(): JPanel {
        val titlePanel = when(result.category) {
            Category.IAC -> IacPanelTop(result as IacCheckovResult)
            Category.VULNERABILITIES -> VulnerabilitiesPanelTop(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsPanelTop(result as SecretsCheckovResult)
            Category.LICENSES -> LicensePanelTop(result as LicenseCheckovResult)
        }
        return titlePanel
    }

    private fun createExtraInfoPanel(): JPanel {
        val extraInfoPanel = when(result.category) {
            Category.IAC -> IacExtraInfoPanel(result)
            Category.VULNERABILITIES -> VulnerabilitiesExtraInfoPanel(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsExtraInfoPanel(result as SecretsCheckovResult)
            Category.LICENSES -> LicenseExtraInfoPanel(result as LicenseCheckovResult)
        }
        return extraInfoPanel
    }
}