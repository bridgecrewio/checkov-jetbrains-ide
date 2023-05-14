package com.bridgecrew.ui.actions

import com.bridgecrew.listeners.ErrorBubbleFixListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.JButton

class FixAction(private val buttonInstance: JButton, val result: BaseCheckovResult) : ActionListener {

    private val LOG = logger<FixAction>()

    override fun actionPerformed(e: ActionEvent?) {
        buttonInstance.isEnabled = false
        ApplicationManager.getApplication().invokeLater {
            applyFixDefinition()
        }
        val project = ProjectManager.getInstance().defaultProject
        project.messageBus.syncPublisher(ErrorBubbleFixListener.ERROR_BUBBLE_FIX_TOPIC).fixClicked()
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
                document.replaceString(startOffset, endOffset, result.fixDefinition!!)
                FileDocumentManager.getInstance().saveDocument(document)
                navigateToFile(project, virtualFile, result.codeDiffFirstLine)
            }
        } catch (e: Exception) {
            LOG.warn("error while trying to apply fix", e)
            buttonInstance.isEnabled = true
        }
    }
}