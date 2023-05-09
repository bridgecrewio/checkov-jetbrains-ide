package com.bridgecrew.ui.vulnerabilitiesTree

import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer


class CheckovTreeRenderer : DefaultTreeCellRenderer() {
    override fun getTreeCellRendererComponent(tree: JTree?,
                                              value: Any?,
                                              selected: Boolean,
                                              expanded: Boolean,
                                              leaf: Boolean,
                                              row: Int,
                                              hasFocus: Boolean): Component
    {
        super.getTreeCellRendererComponent(tree, value, selected, true, leaf, row, hasFocus)

        if(value is DefaultMutableTreeNode){
            val userObject = value.userObject
            if(userObject is CheckovTreeNode){
                icon = userObject.getNodeIcon()
            }
        }

        return this
    }
}