package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.SuppressionDialog
import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.getFileType
import com.bridgecrew.utils.navigateToFile
import com.intellij.ide.DataManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.LocalFileSystem
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

const val suppressionButtonText = "Suppress"

class SuppressionButton(private var result: BaseCheckovResult): CheckovLinkButton(suppressionButtonText), ActionListener  {

    private var isOpenDialog : Boolean
    init {
        addActionListener(this)
        isOpenDialog = true
    }


    override fun actionPerformed(e: ActionEvent?) {
        val fileType = getFileType(result.filePath)

        val dialog = SuppressionDialog()
        if(isOpenDialog) {
            dialog.show()
            isOpenDialog = false
            setDisabledLook()
            if(dialog.exitCode == DialogWrapper.OK_EXIT_CODE) {
                generateComment(fileType, dialog.userJustification)
            } else if (dialog.exitCode == DialogWrapper.CANCEL_EXIT_CODE) {
                isOpenDialog = true
                setEnabledLook()
            }
        }
    }

    private fun generateComment(fileType: FileType, userReason: String?) {
        val suppressionComment = generateCheckovSuppressionComment(userReason)
        val document = getDocument(result.absoluteFilePath)
        val lineNumber = getLineNumber(fileType)
        if(document != null && ! isSuppressionExists(document, lineNumber, suppressionComment) && ! isSuppressionExists(document, lineNumber + 1, suppressionComment)) {
            addTextToFile(document, lineNumber, suppressionComment)
        }
    }

    private fun getDocument(filePath: String): Document? {
        val file = LocalFileSystem.getInstance().findFileByPath(filePath) ?: return null
        return FileDocumentManager.getInstance().getDocument(file)
    }

    private fun isSuppressionExists(document: Document, lineNumber: Int, suppressionComment: String): Boolean{
        val checkLineNumber = if(lineNumber == 0) 0 else lineNumber -1
        val lineStartOffset = document.getLineStartOffset(checkLineNumber)
        val lineEndOffset = document.getLineEndOffset(checkLineNumber)
        val lineText = document.getText(TextRange(lineStartOffset, lineEndOffset)).trimEnd()
        val existingList= lineText.split(" ").filter { existingWord -> suppressionComment.split(" ").contains(existingWord) && existingWord.lowercase().contains("checkov") }
        return existingList.isNotEmpty()
    }

    private fun getLineNumber(fileType: FileType): Int {
        if(fileType == FileType.DOCKERFILE) {
            return 0
        }
        return result.fileLineRange[0]
    }

    private fun generateCheckovSuppressionComment(userReason: String?): String {
        val reason = if(userReason.isNullOrEmpty()) "ADD REASON" else userReason
        return "#checkov:skip=${result.id}: $reason"
    }

    private fun addTextToFile(document: Document, lineNumber: Int, suppressionComment: String) {
        val insertionOffset = document.getLineStartOffset(lineNumber)

        WriteCommandAction.runWriteCommandAction(null) {
            val editor = EditorFactory.getInstance().createEditor(document, null)
            val newLineText = "${suppressionComment}\n"

            val dataContext = DataManager.getInstance().dataContext
            val project = dataContext.getData("project") as Project

            document.insertString(insertionOffset, newLineText)
            editor.caretModel.moveToOffset(insertionOffset + newLineText.length)
            navigateToFile(project, result.absoluteFilePath, lineNumber + 1)

        }

        FileDocumentManager.getInstance().saveDocument(document)
    }
}