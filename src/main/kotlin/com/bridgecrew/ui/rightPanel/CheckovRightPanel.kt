package com.bridgecrew.ui.rightPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.ui.rightPanel.extraInfoPanel.*
import com.bridgecrew.ui.rightPanel.topPanel.*
import javax.swing.*

class CheckovRightPanel(var result: BaseCheckovResult): JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        add(createTitlePanel())
        add(JSeparator(JSeparator.HORIZONTAL))
        add(createExtraInfoPanel())
    }

    private fun createTitlePanel(): JPanel {
        val titlePanel = when(result.category) {
            Category.IAC -> IacPanelTop(result)
            Category.VULNERABILITIES -> VulnerabilitiesPanelTop(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsPanelTop(result)
            Category.LICENSES -> LicensePanelTop(result)
        }
        return titlePanel
    }

    private fun createExtraInfoPanel(): JPanel {
        val extraInfoPanel = when(result.category) {
            Category.IAC -> IacExtraInfoPanel(result)
            Category.VULNERABILITIES -> VulnerabilitiesExtraInfoPanel(result as VulnerabilityCheckovResult)
            Category.SECRETS -> SecretsExtraInfoPanel(result)
            Category.LICENSES -> LicenseExtraInfoPanel(result as LicenseCheckovResult)
        }
        return extraInfoPanel
    }
}