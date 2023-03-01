package com.bridgecrew.utils

import com.bridgecrew.results.Severity
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.uiDesigner.core.GridConstraints
import icons.CheckovIcons
import java.awt.Desktop
import java.net.URI
import java.net.URISyntaxException
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.JPanel

fun urlLink(guideline: String?, checkID: String): JPanel{
    val guidelineLabel = JPanel()
    if (guideline.isNullOrEmpty()){
        if (!checkID.startsWith("CKV")){
            // custom policy that does not include guidelines
            val noGuidelinesTitle = JLabel("No guidelines were provided for this policy. You can add your guidelines")
            val noGuidelines = LinkLabel.create("here") {
                try {
                    Desktop.getDesktop().browse(URI("https://www.bridgecrew.cloud/policies/edit/${checkID}"))
                } catch (ex: URISyntaxException) {
                }
            }
            guidelineLabel.add(noGuidelinesTitle)
            guidelineLabel.add(noGuidelines)
        }
        return guidelineLabel
    }
    if (isUrl(guideline)) {
        val url = LinkLabel.create(GUIDELINES_TITLE) {
            try {
                Desktop.getDesktop().browse(URI(guideline))
            } catch (ex: URISyntaxException) {
            }
        }
        guidelineLabel.add(url)
    } else {
        val titleName = JLabel(CUSTOM_GUIDELINES_TITLE)
        val customGuideline = JLabel(guideline)
        guidelineLabel.add(titleName)
        guidelineLabel.add(customGuideline)
    }
    return guidelineLabel
}

fun createGridRowCol(row: Int, col: Int = 0, align: Int = 0, fill: Int = 0): GridConstraints {
    return GridConstraints(
        row, col, 1, 1, align, fill, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
        null, 1, false
    )
}

private val severityIconMap: Map<Severity, Icon> = mapOf(
        Severity.CRITICAL to CheckovIcons.SeverityCritical,
        Severity.HIGH to CheckovIcons.SeverityHigh,
        Severity.MEDIUM to CheckovIcons.SeverityMedium,
        Severity.LOW to CheckovIcons.SeverityLow,
        Severity.UNKNOWN to CheckovIcons.SeverityUnknown
)

fun getSeverityIconBySeverity(severity: Severity): Icon {
    return severityIconMap[severity] ?: CheckovIcons.ErrorIcon
}