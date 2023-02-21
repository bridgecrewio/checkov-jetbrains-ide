package com.bridgecrew.ui.vulnerabilitiesTree

import com.intellij.icons.AllIcons
import icons.CheckovIcons
import javax.swing.Icon

class CheckovFileTreeNode(val fileName: String): CheckovTreeNode {

    private val defaultFile = "DEFAULT_FILE" // for files without dot notation type like Dockerfile

    override fun toString(): String {
        return fileName
    }

    override fun equals(other: Any?): Boolean {
        return other is CheckovFileTreeNode && other.fileName == fileName
    }

    private val iconMap: Map<String, Icon> = mapOf(
            "tf" to CheckovIcons.TerraformIcon,
            "json" to AllIcons.FileTypes.Json,
            defaultFile to AllIcons.FileTypes.Any_type
    )

    override fun getNodeIcon(): Icon {
        return getFileType().let { iconMap[it] } ?: CheckovIcons.ErrorIcon
    }

    private fun getFileType(): String {
        val fileParts = fileName.split(".")
        return if(fileParts.size > 1){
            fileParts[1]
        } else {
            defaultFile
        }
    }
}