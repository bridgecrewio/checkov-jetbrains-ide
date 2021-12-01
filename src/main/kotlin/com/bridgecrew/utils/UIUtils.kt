package com.bridgecrew.utils

import com.intellij.ui.components.labels.LinkLabel
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Desktop
import java.awt.Font
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JLabel
import javax.swing.JTextPane

fun urlLink(title:String, guideline: String): JLabel{
    val guidelineLabel = JLabel()
    val titleName = JLabel(title)
    guidelineLabel.add(titleName)
    if (isUrl(guideline)) {
        val url = LinkLabel.create(guideline) {
            try {
                Desktop.getDesktop().browse(URI(guideline))
            } catch (ex: URISyntaxException) {
            }
        }
        guidelineLabel.add(url)
    } else {
        val customGuideline = JLabel(guideline)
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