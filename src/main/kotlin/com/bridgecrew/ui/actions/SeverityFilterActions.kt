package com.bridgecrew.ui.actions

import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovToolWindowFactory
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton

class SeverityFilterActions(val project: Project) : ActionListener {

    companion object {
        var severityFilterState = mutableMapOf(
                "I" to false,
                "L" to false,
                "M" to false,
                "H" to false,
                "C" to false
        )

        val severityTextToEnum = mapOf(
                "I" to Severity.INFO,
                "L" to Severity.LOW,
                "M" to Severity.MEDIUM,
                "H" to Severity.HIGH,
                "C" to Severity.CRITICAL
        )

        var enabledSeverities = Severity.values().toList()

        var currentSelectedSeverities = Severity.values().toList()

        fun onChangeCategory(category: Category?, project: Project) {
            updateEnabledSeverities(category, project)
        }

        fun onScanFinishedForDisplayingResults(project: Project) {
            updateEnabledSeverities(CheckovToolWindowFactory.lastSelectedCategory, project)
        }

        private fun updateEnabledSeverities(category: Category?, project: Project) {
            val categoryAsList = if (category == null) null else listOf(category)
            val categorySeverities = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, categoryAsList, Severity.values().toList()).map{ result -> result.severity}
            // no pressed category - display the category's severities
            if (currentSelectedSeverities.size == Severity.values().toList().size) {
                enabledSeverities = categorySeverities
                return
            }

            // there is a pressed category - check if it is included in the category severities - if so - display it, else - severities shouldn't be displayed
            if (currentSelectedSeverities.any { severity -> categorySeverities.contains(severity) }) {
                enabledSeverities = categorySeverities
                return
            }

            enabledSeverities = listOf()
        }

        fun restartState() {
            severityFilterState = mutableMapOf(
                    "I" to false,
                    "L" to false,
                    "M" to false,
                    "H" to false,
                    "C" to false
            )
            currentSelectedSeverities = Severity.values().toList()
            enabledSeverities = Severity.values().toList()
        }
    }

    override fun actionPerformed(e: ActionEvent?) {
        val source = e?.source as JButton
        val buttonText = source.text
        severityFilterState[buttonText] = !severityFilterState[buttonText]!!
        val selectedSeverities = severityTextToEnum.filter { (key, _) ->  severityFilterState.filterValues { v-> v }.containsKey(key) }.values.toList()
        currentSelectedSeverities = selectedSeverities.ifEmpty { severityTextToEnum.values }.toList()
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_LOAD_TABS_CONTENT)
    }
}