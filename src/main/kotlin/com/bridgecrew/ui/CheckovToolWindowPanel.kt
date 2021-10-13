package com.bridgecrew.ui

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import javax.swing.*
import com.intellij.ui.components.labels.LinkLabel


class CheckovToolWindowPanel(val project: Project) : JPanel(), Disposable {
    val testPanel = JPanel()
    val text = "src/main/java/com/jfrog/ide/idea/ui/ComponentDetails.java"

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
