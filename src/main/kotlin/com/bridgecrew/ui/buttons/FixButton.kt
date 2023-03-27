package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton


class FixButton(val result: BaseCheckovResult) : JButton(), ActionListener {

    private val LOG = logger<FixButton>()

    init {
        text = "Fix"
        addActionListener(this)
    }

    override fun actionPerformed(e: ActionEvent?) {
        this.isEnabled = false
        ApplicationManager.getApplication().invokeLater {
            applyFixDefinition()
        }
    }

    private fun applyFixDefinition() {
        try {
            val startLine: Int = result.fileLineRange.getOrElse(0) { 1 } - 1
            val endLine: Int = result.fileLineRange.getOrElse(1) { 1 } - 1

            val virtualFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(result.absoluteFilePath)
                    ?: return
            val document: Document? = FileDocumentManager.getInstance().getDocument(virtualFile)

            val startOffset = document!!.getLineStartOffset(startLine)
            val endOffset = document.getLineEndOffset(endLine)

            val dataContext = DataManager.getInstance().dataContext
            val project = dataContext.getData("project") as Project

            WriteCommandAction.runWriteCommandAction(project) {
                navigateToFile(project, virtualFile, startOffset)

                document.replaceString(startOffset, endOffset, result.fixDefinition!!)
                FileDocumentManager.getInstance().saveDocument(document)
            }
        } catch (e: Exception) {
            LOG.warn("error while trying to apply fix", e)
            this.isEnabled = true
        }

    }
}