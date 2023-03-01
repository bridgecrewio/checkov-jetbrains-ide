package com.bridgecrew.ui.buttons

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JOptionPane

const val suppressionButtonText = "Suppression"

class SuppressionButton(private var checkId: String): CheckovLinkButton(suppressionButtonText), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        JOptionPane.showMessageDialog(null, "suppression clicked checkId is $checkId")
    }
}