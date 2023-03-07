package com.bridgecrew.ui.vulnerabilitiesTree

import com.bridgecrew.utils.FileType
import com.bridgecrew.utils.getFileType
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

    private val iconMap: Map<FileType, Icon> = mapOf(
            FileType.TERRAFORM to CheckovIcons.TerraformIcon,
            FileType.JSON to AllIcons.FileTypes.Json,
            FileType.UNKNOWN to AllIcons.FileTypes.Any_type
    )

    override fun getNodeIcon(): Icon {
        return iconMap[getFileType(fileName)] ?: AllIcons.FileTypes.Any_type
    }

    override fun hashCode(): Int {
        var result = fileName.hashCode()
        result = 31 * result + defaultFile.hashCode()
        result = 31 * result + iconMap.hashCode()
        return result
    }
}