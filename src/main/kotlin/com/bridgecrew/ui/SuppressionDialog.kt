package com.bridgecrew.ui

import com.intellij.openapi.ui.DialogWrapper
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class SuppressionDialog: DialogWrapper(true) {

    private val textField = JTextField()
    var userJustification = ""

    init {
        title = "Suppress Issue"
        init()
    }

    override fun createCenterPanel(): JComponent? {
        val panel = JPanel(BorderLayout())
        panel.preferredSize = Dimension(400, super.getPreferredSize().height)
        panel.border = BorderFactory.createEmptyBorder(20, 10, 20, 10)
        panel.add(JLabel("Justification: "), BorderLayout.NORTH)
        panel.add(textField, BorderLayout.CENTER)
        return panel
    }

    override fun doOKAction() {
        userJustification = textField.text
        super.doOKAction()
    }
}