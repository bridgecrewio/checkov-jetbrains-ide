package com.bridgecrew.utils
import com.bridgecrew.services.checkovService.DOCKER_MOUNT_DIR
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.net.URL

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

fun getOffsetHighlighByLines(range:List<Int>, project: Project): Pair<Int,Int>{
    val startLine: Int = range.getOrElse(0, { 1 }) - 1
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    val document = editor?.getDocument();
    var startOffset = document?.getLineStartOffset(startLine)
    var endOffset = document?.getLineEndOffset(startLine)
    if (endOffset == null) {
        endOffset = 0
    }
    if(startOffset == null) {
        startOffset = 0
    }
    return Pair(startOffset, endOffset)
}

fun getOffsetReplaceByLines(range:List<Int>, project: Project): Pair<Int,Int>{
    val startLine: Int = range.getOrElse(0, { 1 }) - 1
    val endLine: Int = range.getOrElse(1, { 1 }) - 1
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    val document = editor?.getDocument();
    var startOffset = document?.getLineStartOffset(startLine)
    var endOffset = document?.getLineEndOffset(endLine)
    if (endOffset == null) {
        endOffset = 0
    }
    if(startOffset == null) {
        startOffset = 0
    }
    return Pair(startOffset, endOffset)
}

fun updateFile(replaceString: String, project: Project, start:Int, end: Int){
    val editor = FileEditorManager.getInstance(project).selectedTextEditor
    val document = editor?.getDocument();
    WriteCommandAction.runWriteCommandAction(
        project
    ) { document?.replaceString(start, end, replaceString) }
    FileDocumentManager.getInstance().saveDocument(document!!)
}


fun normalizeFilePathToAbsolute(fileName: String, projectBasePath: String, fileRelativePath: String = ""): String {
    return if (fileName.startsWith(DOCKER_MOUNT_DIR)) {
        val relativePathParts = mutableListOf(fileRelativePath.split("/")).removeLast()
        val relativeFileDirectory = relativePathParts.joinToString("/")
        "$projectBasePath${relativeFileDirectory}"
    } else {
        fileName
    }
}

/**
 * Helper function that validates url string.
 */
fun isUrl(url: String?): Boolean {
    if (url.isNullOrEmpty()) {
        return false
    }
    try {
        URL(url)
        return true
    } catch (e: Throwable) {
        return false
    }
}
