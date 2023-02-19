package com.bridgecrew.ui.actions

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JOptionPane

class SeverityFilterActions : ActionListener {
    override fun actionPerformed(e: ActionEvent?) {
        val source = e?.source as JButton
        val buttonText = source.text
        JOptionPane.showMessageDialog(null, "Will filter by $buttonText")
    }
}