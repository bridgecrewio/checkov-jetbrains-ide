package com.bridgecrew.ui.errorBubble

import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.ui.actions.FixAction
import com.bridgecrew.ui.actions.FocusOnFileInTree
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.ui.buttons.CheckovLinkButton
import com.bridgecrew.ui.buttons.DocumentationButton
import com.bridgecrew.utils.CheckovUtils
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import icons.CheckovIcons
import java.awt.Dimension
import javax.swing.*
import com.intellij.openapi.project.ProjectManager

class ErrorBubbleActionsPanel(val result: BaseCheckovResult) : JPanel() {

    init {
        layout = BoxLayout(this, BoxLayout.X_AXIS)
        addFixIfNeeded()
        addConsoleButton()
        addDocumentationIfNeeded()
        addLogo()
        maximumSize = Dimension(ErrorBubbleInnerPanel.PANEL_WIDTH, this.preferredSize.height)
    }

    private fun addFixIfNeeded() {
        if (result.fixDefinition != null) {
            val fixButton = CheckovLinkButton("Fix")
            fixButton.addActionListener(FixAction(fixButton, result))
            add(fixButton)
        }
    }

    private fun addConsoleButton() {
        val button = CheckovLinkButton("Console")
        button.addActionListener(FocusOnFileInTree(result.filePath))
        add(button)
    }

    private fun addDocumentationIfNeeded() {
        if (result.guideline != null && !CheckovUtils.isCustomPolicy(result)) {
            add(DocumentationButton(result.guideline))
        }
    }

    private fun addLogo() {
        add(Box.createHorizontalGlue())
        add(JLabel(CheckovIcons.prismaIcon))
        add(Box.createRigidArea(Dimension(5, 0)))
        add(JLabel("Prisma Cloud"))
        add(Box.createRigidArea(Dimension(10, 0)))
    }
}