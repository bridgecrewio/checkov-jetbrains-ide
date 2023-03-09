package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Severity
import com.bridgecrew.utils.getSeverityIconBySeverity
import com.bridgecrew.utils.isCustomPolicy
import com.bridgecrew.utils.isUrl
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

open class CheckovDescriptionPanelTop : JPanel() {

    init {
        layout = BorderLayout()
        maximumSize = Dimension(Int.MAX_VALUE, 30)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
    }

    fun createTitleAndIcon(title: String, severity: Severity) : JLabel{
        return JLabel("<html><body style='width: 250px;'>${title}</html>", getSeverityIconBySeverity(severity), SwingConstants.LEFT)
    }

    fun getTitle(result: BaseCheckovResult): String {
        return if(isCustomPolicy(result)){
            "(custom policy) ${result.name}"
        } else {
            result.id
        }
    }

    fun createActionsPanel(): JPanel {
        val actionsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
        actionsPanel.add(Box.createHorizontalGlue())
        return actionsPanel
    }

    fun isShowDocumentationButton(result: BaseCheckovResult): Boolean {
        return ! isCustomPolicy(result) && result.guideline != null && isUrl(result.guideline)
    }
}