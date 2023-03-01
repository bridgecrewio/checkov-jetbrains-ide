package com.bridgecrew.ui.buttons

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JOptionPane

const val fixCVEButtonText = "Fix CVE"
class FixCVEButton(private var id: String): CheckovLinkButton(fixCVEButtonText), ActionListener {

    init {
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        JOptionPane.showMessageDialog(null, "fix CVE clicked id is $id")
    }
}