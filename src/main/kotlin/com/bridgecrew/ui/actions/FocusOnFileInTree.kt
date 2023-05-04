package com.bridgecrew.ui.actions

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JOptionPane

class FocusOnFileInTree(val filePath: String) : ActionListener {

    override fun actionPerformed(e: ActionEvent?) {
        JOptionPane.showMessageDialog(null, "Here we should focus on the file in $filePath")
    }
}