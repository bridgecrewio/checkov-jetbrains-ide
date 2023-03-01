package com.bridgecrew.ui.buttons

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JOptionPane

const val documentationButtonText = "Documentation"

class DocumentationButton(private var link: String?, private var checkId: String) : CheckovLinkButton(documentationButtonText), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        JOptionPane.showMessageDialog(null, "documentation link is $link and checkId is $checkId")
    }
}