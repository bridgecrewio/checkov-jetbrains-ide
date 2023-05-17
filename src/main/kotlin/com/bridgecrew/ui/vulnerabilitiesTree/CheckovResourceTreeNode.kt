package com.bridgecrew.ui.vulnerabilitiesTree

import javax.swing.Icon

class CheckovResourceTreeNode (val resourceName: String, private val icon: Icon, override val relativePathNode: String): CheckovTreeNode{
    override fun toString(): String {
        return resourceName
    }

    override fun equals(other: Any?): Boolean {
        return other is CheckovResourceTreeNode && other.resourceName == resourceName && other.relativePathNode == relativePathNode
    }
    override fun getNodeIcon(): Icon {
        return icon
    }

    override fun hashCode(): Int {
        var result = resourceName.hashCode()
        result = 31 * result + icon.hashCode()
        result = 31 * result + relativePathNode.hashCode()
        return result
    }
}