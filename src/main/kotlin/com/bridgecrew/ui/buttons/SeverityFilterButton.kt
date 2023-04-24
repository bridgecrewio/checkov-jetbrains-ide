package com.bridgecrew.ui.buttons

import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.UIManager
import javax.swing.plaf.basic.BasicButtonUI

class SeverityFilterButton(val project: Project,text: String, severity: Severity): JButton(text) {

    init {
        addActionListener(SeverityFilterActions(project))
        preferredSize = Dimension(24, 24)
        border = null
        isOpaque = true
        ui = object: BasicButtonUI() {
            override fun paint(g: Graphics?, c: JComponent?) {
                val g2d = g?.create() as Graphics2D
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
                g2d.color = c?.background
                c?.height?.let { g2d.fillRect(0,0,  c.width, it) }
                super.paint(g, c)
                g2d.dispose()
            }
        }

        isEnabled = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, null, listOf(severity)).isNotEmpty() ||
                (project.service<FullScanStateService>().isFullScanRunning && !project.service<FullScanStateService>().isFrameworkResultsWereDisplayed)
//        isEnabled = CheckovResultsListUtils.getCurrentResultsSeverities(project.service<ResultsCacheService>().checkovResults).contains(severity)
    }

    override fun updateUI() {
        super.updateUI()
        val isClicked = SeverityFilterActions.severityFilterState[text]
        val defaultBG = UIManager.getColor("Button.background")
        background = if(isClicked == true) JBColor.GRAY else defaultBG
    }

    override fun doClick() {
        super.doClick()
    }
}