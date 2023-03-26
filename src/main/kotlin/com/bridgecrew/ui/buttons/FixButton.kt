package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.getOffsetReplaceByLines
import com.bridgecrew.utils.updateFile
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.ProjectManager
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton
import javax.swing.JOptionPane

class FixButton(val result: BaseCheckovResult) : JButton(), ActionListener {

    init {
        text = "Fix"
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
//        JOptionPane.showMessageDialog(null, "Fix clicked") // Maybe display a dialog for showing we're going to fix?
        val project = ProjectManager.getInstance().defaultProject
        ApplicationManager.getApplication().invokeLater {
            if(result.fixDefinition != null){
                val (start, end) = getOffsetReplaceByLines(result.fileLineRange, project)
                updateFile(result.fixDefinition, project, start, end)
            }
        }
    }
}