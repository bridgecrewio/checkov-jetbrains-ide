package com.bridgecrew.utils

import com.intellij.ui.components.labels.LinkLabel
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Desktop
import java.awt.Font
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextPane

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

fun createTitle(title: String, font: Int, size: Int): JTextPane {
    val fontTitle = Font("Verdana", font, size)
    val Jtitle = JTextPane()
    Jtitle.font = fontTitle
    Jtitle.text = title
    return Jtitle
}


fun createGridRowCol(row: Int, col: Int = 0, align: Int = 0, fill: Int = 0): GridConstraints {
    return GridConstraints(
        row, col, 1, 1, align, fill, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
        null, 1, false
    )
}