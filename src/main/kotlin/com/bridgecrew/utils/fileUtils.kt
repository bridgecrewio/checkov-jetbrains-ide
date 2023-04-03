package com.bridgecrew.utils
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import org.apache.commons.io.FilenameUtils
import org.jetbrains.rpc.LOG
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path

var checkovTempDirPath: Path = Files.createTempDirectory("checkov")
fun navigateToFile(project: Project, virtualFile: VirtualFile, startOffset: Int = 0) {
    val offset = calculateOffset(startOffset, virtualFile.length.toInt())
    PsiNavigationSupport.getInstance().createNavigatable(
            project,
            virtualFile,
            offset
    ).navigate(false)
}

fun navigateToFile(project: Project, filePath: String, startOffset: Int = 0) {
    val virtualFile: VirtualFile = LocalFileSystem.getInstance().findFileByPath(filePath)
            ?: return
    navigateToFile(project, virtualFile, startOffset)
}
fun calculateOffset(startOffset: Int, fileSize: Int): Int {
    if (startOffset < 0)
        return 0

    if (startOffset > fileSize)
        return fileSize

    return startOffset
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

fun getFileType(filePath: String): FileType {
    val fileParts = filePath.split(".")
    return if(fileParts.size > 1){
        when(fileParts[1]) {
            "json" -> FileType.JSON
            "tf" -> FileType.TERRAFORM
            "js" -> FileType.JAVASCRIPT
            "ts" -> FileType.TYPESCRIPT
            "py" -> FileType.PYTHON
            "txt" -> FileType.TEXT
            "yml", "yaml" -> FileType.YAML
            "Dockerfile" -> FileType.DOCKERFILE
            "xml" -> FileType.XML
            "kt" -> FileType.KOTLIN
            "java", "jar" -> FileType.JAVA
            else -> FileType.UNKNOWN
        }
    } else {
        when(filePath) { //no "dot" in file name
            "Dockerfile" -> FileType.DOCKERFILE
            else -> FileType.UNKNOWN
        }
    }
}

fun getGitIgnoreValues(project: Project): List<String> {
    try {
        val path = project.basePath + "/.gitignore"
        val gitignoreVirtualFile = LocalFileSystem.getInstance().findFileByPath(path)
        if (gitignoreVirtualFile == null) {
            LOG.info("no .gitignore file in this project")
            return arrayListOf()
        }

        return String(gitignoreVirtualFile.contentsToByteArray())
                .split(System.lineSeparator()).filter { raw -> !(raw.trim().startsWith("#") || raw.trim().isEmpty() )}

    } catch (e: Exception) {
        LOG.error(Exception("error while reading .gitignore file", e))
    }
    return arrayListOf()
}

fun extractFileNameFromPath(filePath: String): String {
    val filename: String = FilenameUtils.getName(filePath)
    val extension: String = FilenameUtils.getExtension(filename)
    return filename.removeSuffix(".$extension")
}

fun createCheckovTempFile(prefix: String, suffix: String): File {
    if (!Files.exists(checkovTempDirPath)) {
        checkovTempDirPath = Files.createTempDirectory("checkov")
    }
    return File.createTempFile(prefix,
            suffix,
            checkovTempDirPath.toFile())
}

fun deleteCheckovTempDir() {
    try {
        if (!Files.exists(checkovTempDirPath)) {
            return
        }

        LOG.info("Checking if Checkov temp dir should be deleted, current files - ${checkovTempDirPath.toFile().list()?.map { path -> path.toString() }}")
        if (checkovTempDirPath.toFile().list()!!.isEmpty() || checkovTempDirPath.toFile().list()!!.none { filePath -> !filePath.startsWith("error") && !filePath.startsWith("full_scan_state") }) {
            checkovTempDirPath.toFile().deleteRecursively()
        }
    } catch (e: Exception) {
        LOG.warn("could not delete temp directory in $checkovTempDirPath", e)
    }
}
