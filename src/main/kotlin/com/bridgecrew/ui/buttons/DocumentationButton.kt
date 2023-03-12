package com.bridgecrew.ui.buttons

import java.awt.Desktop
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.net.URI

const val documentationButtonText = "Documentation"

class DocumentationButton(private var link: String) : CheckovLinkButton(documentationButtonText), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        Desktop.getDesktop().browse(URI(link))
    }
}