package com.bridgecrew.utils

import com.bridgecrew.CheckovResult
import com.bridgecrew.services.CheckovScanService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.uiDesigner.core.GridConstraints
import java.awt.Desktop
import java.awt.Font
import java.net.URI
import java.net.URISyntaxException
import javax.swing.JLabel
import javax.swing.JTextPane

fun urlLink(title:String, url: String): JLabel{
    var link = JLabel()

    if (isUrl(url)) {
        link = LinkLabel.create(title) {
            try {
                Desktop.getDesktop().browse(URI(url))
            } catch (ex: URISyntaxException) {
            }
        }
    }
    return link
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