package com.bridgecrew.ui.vulnerabilitiesTree

import com.intellij.icons.AllIcons
import javax.swing.Icon

data class CheckovFolderTreeNode(val pathName: String, override val relativePathNode: String): CheckovTreeNode {

    override fun toString(): String {
        return pathName
    }
    override fun equals(other: Any?): Boolean {
        return other is CheckovFolderTreeNode && other.pathName == pathName && other.relativePathNode == relativePathNode
    }


    override fun getNodeIcon(): Icon {
        return AllIcons.Nodes.Folder
    }

    override fun hashCode(): Int {
        var result = pathName.hashCode()
        result = 31 * result + relativePathNode.hashCode()
        return result
    }
}