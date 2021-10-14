package com.bridgecrew.ui

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter


import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.components.labels.LinkLabel
import java.awt.*
import javax.swing.*
import javax.swing.event.*

class CheckovToolWindowPanel(val project: Project) : JPanel(), Disposable {
    val testPanel = JPanel()
    val text = "src/main/java/com/jfrog/ide/idea/ui/ComponentDetails.java"
    val frame = JPanel(GridBagLayout())
    fun dividePanel(): JPanel{
        val left = CheckovToolWindowTreeNode(project).createTree()
        val right = CheckovToolWindowDescriptionPanel().createScroll()
        val split = JBSplitter()
        split.setFirstComponent(left)
        split.setSecondComponent(right)
        return split
    }

    fun filePathWithLink(): JPanel {
        val hyperlinkLabelTest = LinkLabel.create(text) {
            val fileToNavigate: PsiFile? = getPsFileByPath(pathTest=text, project=project)
            if (fileToNavigate != null) {
                navigateToFile(fileToNavigate)
            }
            updateFile()
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            editor?.selectionModel?.setSelection(0, 100)
        }
        testPanel.add(hyperlinkLabelTest)
        return testPanel
    }

    private fun updateFile(){
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        editor?.selectionModel?.setSelection(0, 30)
        val document = editor?.getDocument();
        WriteCommandAction.runWriteCommandAction(
            project
        ) { document?.replaceString(100, 120, "Yana's test replacement") }
    }
    private fun navigateToFile(fileToNavigate: PsiFile) {
        PsiNavigationSupport.getInstance().createNavigatable(
            fileToNavigate.project,
            fileToNavigate.virtualFile,
            0
        ).navigate(false)
    }

    private fun getPsFileByPath(pathTest: String, project: Project): PsiFile? {
        val absolutePath = project.getBasePath() + if (pathTest.startsWith("/")) pathTest else "/$pathTest"
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findFile(virtualFile)
        }
        return null
    }

    override fun dispose() = Unit

}
