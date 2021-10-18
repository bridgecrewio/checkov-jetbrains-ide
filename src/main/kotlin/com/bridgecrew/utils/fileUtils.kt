package com.bridgecrew.utils
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.uiDesigner.core.GridConstraints

fun navigateToFile(fileToNavigate: PsiFile) {
    PsiNavigationSupport.getInstance().createNavigatable(
        fileToNavigate.project,
        fileToNavigate.virtualFile,
        0
    ).navigate(false)
}

fun getPsFileByPath(path: String, project: Project): PsiFile? {
    val absolutePath = project.getBasePath() + if (path.startsWith("/")) path else "/$path"
    val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
    if (virtualFile != null) {
        return PsiManager.getInstance(project).findFile(virtualFile)
    }
    return null
}

fun updateFile(replaceString: String, project: Project, start:Int, end: Int){
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    val document = editor?.getDocument();
    WriteCommandAction.runWriteCommandAction(
        project
    ) { document?.replaceString(start, end, replaceString) }
}

fun createGridRowCol(row: Int, col: Int = 0, align: Int = 0): GridConstraints {
    return GridConstraints(
        row, col, 1, 1, align, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
        null, 1, false
    )
}

