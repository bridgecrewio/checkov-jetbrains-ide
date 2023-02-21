package com.bridgecrew.ui.vulnerabilitiesTree

import com.intellij.icons.AllIcons
import javax.swing.Icon

data class CheckovFolderTreeNode(val pathName: String): CheckovTreeNode {

    override fun toString(): String {
        return pathName
    }
    override fun equals(other: Any?): Boolean {
        return other is CheckovFolderTreeNode && other.pathName == pathName
    }


    override fun getNodeIcon(): Icon {
        return AllIcons.Nodes.Folder
    }
}