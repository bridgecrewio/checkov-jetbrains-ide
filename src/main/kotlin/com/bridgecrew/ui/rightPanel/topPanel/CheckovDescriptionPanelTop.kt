package com.bridgecrew.ui.rightPanel.topPanel

import com.bridgecrew.results.*
import com.bridgecrew.ui.buttons.SuppressionButton
import com.bridgecrew.utils.*
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

open class CheckovDescriptionPanelTop(val result: BaseCheckovResult) : JPanel() {

    init {
        layout = BorderLayout()
        maximumSize = Dimension(Int.MAX_VALUE, 30)
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        background = UIUtil.getEditorPaneBackground()
    }

    fun createTitleAndIcon(title: String, severity: Severity): JLabel {
        return JLabel("<html><body style='width: max-width;" +
                "font-size: 11px;" +
                "white-space: nowrap;\n" +
                "  overflow: hidden;\n" +
                "  display: block;\n" +
                "  text-overflow: ellipsis;'>${title}</html>", getSeverityIconBySeverity(severity), SwingConstants.LEFT)
    }

    fun getTitle(result: BaseCheckovResult): String {
        if (CheckovUtils.isCustomPolicy(result)) {
            return result.name
        }

        if (result.category == Category.VULNERABILITIES &&
                (result.checkType == CheckType.SCA_PACKAGE || result.checkType == CheckType.SCA_IMAGE)) {
            return result.name
        }

        if (result.category == Category.IAC) {
            return (result as IacCheckovResult).checkName
        }

        return result.id
    }

    fun createActionsPanel(): JPanel {
        val actionsPanel = JPanel().apply { layout = BoxLayout(this, BoxLayout.X_AXIS) }
        actionsPanel.add(Box.createHorizontalGlue())
        return actionsPanel
    }

    fun isShowDocumentationButton(result: BaseCheckovResult): Boolean {
        return !CheckovUtils.isCustomPolicy(result) && result.guideline != null && isUrl(result.guideline)
    }

    fun createSuppressionButton(panel: JPanel) {
        val fileType: FileType = getFileType(result.filePath)
        if (SUPPRESSION_BUTTON_ALLOWED_FILE_TYPES.contains(fileType)) {
            panel.add(SuppressionButton(result))
        }
    }
}