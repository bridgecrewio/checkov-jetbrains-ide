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

        var currentCategory = CheckovToolWindowFactory.lastSelectedCategory

        fun onChangeCategory(category: Category?, project: Project) {
            val categoryAsList = if (category == null) null else listOf(category)

            val categorySeverities = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, categoryAsList, Severity.values().toList()).map{ result -> result.severity}
            // theres a category from before
            if (currentSelectedSeverities.size < Severity.values().toList().size) {
                if (currentSelectedSeverities.any { severity -> categorySeverities.contains(severity) }) {
                    enabledSeverities = categorySeverities
                } else {
                    enabledSeverities = listOf()
                }
            } else {
                enabledSeverities = categorySeverities
            }
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
            currentCategory = CheckovToolWindowFactory.lastSelectedCategory
        }
    }

    override fun actionPerformed(e: ActionEvent?) {
        val source = e?.source as JButton
        val buttonText = source.text
//        val severity: Severity? = severityTextToEnum[buttonText]
        severityFilterState[buttonText] = !severityFilterState[buttonText]!! //&&
//                severity != null && CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, null, listOf(severity)).isNotEmpty()
        val selectedSeverities = severityTextToEnum.filter { (key, _) ->  severityFilterState.filterValues { v-> v }.containsKey(key) }.values.toList()
        currentSelectedSeverities = selectedSeverities.ifEmpty { severityTextToEnum.values }.toList()
        if (currentCategory != CheckovToolWindowFactory.lastSelectedCategory) {
            currentCategory = CheckovToolWindowFactory.lastSelectedCategory
            val currentCategoryAsList = currentCategory?.let { listOf(it) }
            enabledSeverities = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults, currentCategoryAsList, null).map{ result -> result.severity}
        }
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_LOAD_TABS_CONTENT)
    }
}