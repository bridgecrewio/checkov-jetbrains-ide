package com.bridgecrew.ui.topPanel

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import icons.CheckovIcons
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.time.Duration
import java.time.Instant
import javax.swing.*

data class ScanResultMetadata(
        val totalIssues: Int,
        val totalPassed: Int,
        val scanDuration: Long
)

class CheckovTopPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    init {
        val actionToolbarPanel = JPanel(FlowLayout(FlowLayout.LEFT, 0, 0))

        createActionGroupPanel(actionToolbarPanel)
        createSeparator(actionToolbarPanel)
        addSeverityLabel(actionToolbarPanel)
        addFilterActions(actionToolbarPanel)
        addScanStatusLabel(actionToolbarPanel)

        add(actionToolbarPanel, BorderLayout.NORTH)
        toolbar = actionToolbarPanel
    }

    private fun createActionGroupPanel(actionToolbarPanel: JPanel) {
        CheckovActionToolbar.setComponent(this)
        actionToolbarPanel.add(CheckovActionToolbar.actionToolBar.component)
    }

    private fun addSeverityLabel(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(JLabel("Severity:"))
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 24)))
    }

    private fun createSeparator(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 24)))
        val separator = JSeparator(JSeparator.VERTICAL)
        separator.preferredSize = Dimension(5, 24)
        actionToolbarPanel.add(separator)
        actionToolbarPanel.add(Box.createRigidArea(Dimension(5, 24)))
    }

    private fun addScanStatusLabel(actionToolbarPanel: JPanel) {
        val metadata = getScanResultMetadata()
        createSeparator(actionToolbarPanel)
        if (metadata.scanDuration > 0) {
            val labelText = "Total issues: ${metadata.totalIssues}, passed: ${metadata.totalPassed} of ${metadata.totalIssues.plus(metadata.totalPassed)} tests - ${metadata.scanDuration}s"
            val scanStatusBar = JLabel(labelText)
            scanStatusBar.icon = CheckovIcons.ErrorIcon
            actionToolbarPanel.add(scanStatusBar)
        }
    }

    private fun getScanResultMetadata(): ScanResultMetadata {
        val fullScanData = project.service<AnalyticsService>().getFullScanData()
        val scanResultMetadata: ScanResultMetadata = if (fullScanData != null) {
            val now = Instant.now()
            val startedTime = if (fullScanData.isFullScanStarted()) fullScanData.fullScanStartedTime.toInstant() else now
            val finishedTime = if (fullScanData.isFullScanFinished()) fullScanData.fullScanFinishedTime.toInstant() else now
            val totalScanTimeDuration = Duration.between(startedTime, finishedTime)
            ScanResultMetadata(totalIssues = fullScanData.totalFailed, totalPassed = fullScanData.totalPassed, scanDuration = totalScanTimeDuration.toSeconds())
        } else {
            ScanResultMetadata(totalIssues = 0, totalPassed = 0, scanDuration = 0)
        }
        return scanResultMetadata
    }

    private fun addFilterActions(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(createButton("I"));
        actionToolbarPanel.add(createButton("L"));
        actionToolbarPanel.add(createButton("M"));
        actionToolbarPanel.add(createButton("H"));
        actionToolbarPanel.add(createButton("C"));
    }

    private fun createButton(text: String): JButton? {
        val button = JButton(text)
        button.preferredSize = Dimension(24, 24)
        button.addActionListener(SeverityFilterActions())
        return button
    }

    override fun dispose() = Unit
}