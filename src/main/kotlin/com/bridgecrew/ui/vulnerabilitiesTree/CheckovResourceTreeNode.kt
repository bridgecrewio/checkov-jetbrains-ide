package com.bridgecrew.ui.vulnerabilitiesTree

import javax.swing.Icon

class CheckovResourceTreeNode (val resourceName: String, private val icon: Icon): CheckovTreeNode{
    override fun toString(): String {
        return resourceName
    }

    override fun equals(other: Any?): Boolean {
        return other is CheckovResourceTreeNode && other.resourceName == resourceName
    }
    override fun getNodeIcon(): Icon {
        return icon
    }
}