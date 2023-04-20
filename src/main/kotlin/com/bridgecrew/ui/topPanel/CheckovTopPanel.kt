package com.bridgecrew.ui.topPanel

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.ui.buttons.SeverityFilterButton
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
        val fullScanAnalyticsData: AnalyticsService.FullScanAnalyticsData? = project.service<AnalyticsService>().fullScanData
        val scanResultMetadata: ScanResultMetadata = if (fullScanAnalyticsData != null) {
            val now = Instant.now()
            val startedTime = if (fullScanAnalyticsData.isFullScanStarted()) fullScanAnalyticsData.scanStartedTime.toInstant() else now
            val finishedTime = if (fullScanAnalyticsData.isFullScanFinished()) fullScanAnalyticsData.scanFinishedTime.toInstant() else now
            val totalScanTimeDuration = Duration.between(startedTime, finishedTime)
            val checkovResults = project.service<ResultsCacheService>().checkovResults
            val totalIssues = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(checkovResults, Category.values().toList(), Severity.values().toList()).size
            ScanResultMetadata(totalIssues = totalIssues, totalPassed = project.service<FullScanStateService>().totalPassedCheckovChecks, scanDuration = totalScanTimeDuration.toSeconds())
        } else {
            ScanResultMetadata(totalIssues = 0, totalPassed = 0, scanDuration = 0)
        }
        return scanResultMetadata
    }

    private fun addFilterActions(actionToolbarPanel: JPanel) {
        actionToolbarPanel.add(SeverityFilterButton(project, "I", Severity.INFO));
        actionToolbarPanel.add(SeverityFilterButton(project,"L", Severity.LOW));
        actionToolbarPanel.add(SeverityFilterButton(project,"M", Severity.MEDIUM));
        actionToolbarPanel.add(SeverityFilterButton(project,"H", Severity.HIGH));
        actionToolbarPanel.add(SeverityFilterButton(project,"C", Severity.CRITICAL));
    }

    override fun dispose() = Unit
}