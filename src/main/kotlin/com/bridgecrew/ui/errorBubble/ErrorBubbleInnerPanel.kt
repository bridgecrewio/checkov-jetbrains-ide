package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.results.VulnerabilityCheckovResult
import com.bridgecrew.ui.CodeDiffPanel
import com.bridgecrew.utils.UNKNOWN_LICENSES_DESCRIPTION
import com.bridgecrew.utils.VIOLATED_LICENSES_DESCRIPTION
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import javax.swing.*


class ErrorBubbleInnerPanel(val result: BaseCheckovResult, private val vulnerabilityCount: Int, private val index: Int, private val total: Int, private val callback: navigationCallback) : JPanel() {

    companion object {
        const val MIN_INNER_PANEL_HEIGHT = 75
        const val PANEL_HEIGHT = 200
        const val PANEL_WIDTH = 500
        const val MAX_TITLE_TEXT_WIDTH = 400
    }

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        val topPanel = ErrorBubbleTopPanel(result, getTitleByCategory(), index, total, callback)
        topPanel.alignmentX = Component.LEFT_ALIGNMENT
        add(topPanel)

        add(JSeparator(JSeparator.HORIZONTAL), BorderLayout.SOUTH)
        add(Box.createRigidArea(Dimension(0, 5)))

        addCenterPanelByCategory()

        add(Box.createRigidArea(Dimension(0, 5)))
        val actionsPanel = ErrorBubbleActionsPanel(result)
        actionsPanel.alignmentX = Component.LEFT_ALIGNMENT
        actionsPanel.border = BorderFactory.createEmptyBorder(0, 30, 0, 0)
        add(actionsPanel)

        preferredSize = Dimension(PANEL_WIDTH, PANEL_HEIGHT)
    }

    private fun getTitleByCategory(): String {
        return if (result.category == Category.VULNERABILITIES) {
            if (vulnerabilityCount > 1) {
                "Package contains $vulnerabilityCount vulnerabilities"
            } else {
                (result as VulnerabilityCheckovResult).violationId.toString()
            }
        } else if (result.category == Category.LICENSES){
            "${(result as LicenseCheckovResult).policy}/${result.licenseType}"
        } else {
            result.name
        }
    }

    private fun addCenterPanelByCategory() {
        when (result.category) {
            Category.VULNERABILITIES -> {
                if((result as VulnerabilityCheckovResult).rootPackageVersion == null || result.rootPackageFixVersion == null) {
                    buildCenterPanel("No automated fix is available")
                } else {
                    val text = "Bump from version ${(result as VulnerabilityCheckovResult).rootPackageVersion} to ${result.rootPackageFixVersion} to fix"
                    if (vulnerabilityCount > 1) {
                        buildCenterPanel("$text all vulnerabilities")
                    } else {
                        buildCenterPanel("$text the vulnerability")
                    }
                }
            }

            Category.IAC -> {
                if(result.fixDefinition != null){
                    val codeDiffPanel = CodeDiffPanel(result, false)
                    val scroll = JBScrollPane(codeDiffPanel)
                    scroll.border = BorderFactory.createEmptyBorder(0, 30, 0, 0)
                    scroll.alignmentX = Component.LEFT_ALIGNMENT
                    SwingUtilities.invokeLater(Runnable {
                        scroll.viewport.viewPosition = Point(0, 0)
                    })
                    add(scroll)
                } else {
                    buildCenterPanel("No automated fix is available")
                }
            }

            Category.SECRETS -> {
                buildCenterPanel(result.description ?: "")
            }

            Category.LICENSES -> {
                val text = if(result.id == "BC_LIC_1") {
                    VIOLATED_LICENSES_DESCRIPTION
                } else if(result.id == "BC_LIC_2") {
                    UNKNOWN_LICENSES_DESCRIPTION
                } else {
                    "No description available for policy ${result.id}"
                }
                buildCenterPanel(text)
            }
        }
    }

    private fun buildCenterPanel(text: String) {
        val centerPanel = ErrorBubbleCenterPanel(result, text, total)
        centerPanel.alignmentX = Component.LEFT_ALIGNMENT
        centerPanel.border = BorderFactory.createEmptyBorder(0, 30, 0, 0)
        add(centerPanel)
    }
}