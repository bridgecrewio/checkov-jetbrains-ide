package com.bridgecrew.ui.actions

import com.bridgecrew.results.Severity
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovToolWindowManagerPanel
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton

class SeverityFilterActions(val project: Project) : ActionListener {

    companion object {
        val severityFilterState = mutableMapOf(
                "I" to false,
                "L" to false,
                "M" to false,
                "H" to false,
                "C" to false
        )

        val severityTextToEnum = mapOf(
                "I" to Severity.UNKNOWN,
                "L" to Severity.LOW,
                "M" to Severity.MEDIUM,
                "H" to Severity.HIGH,
                "C" to Severity.CRITICAL
        )

        var currentSelectedSeverities = Severity.values().toList()
    }

    override fun actionPerformed(e: ActionEvent?) {
        val source = e?.source as JButton
        val buttonText = source.text
        severityFilterState[buttonText] = !severityFilterState[buttonText]!!
        val selectedSeverities = severityTextToEnum.filter { (key, _) ->  severityFilterState.filterValues { v-> v }.containsKey(key) }.values.toList()
        currentSelectedSeverities = selectedSeverities.ifEmpty { severityTextToEnum.values }.toList()
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED)
    }
}