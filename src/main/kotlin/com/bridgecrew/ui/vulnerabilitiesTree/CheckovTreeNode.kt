package com.bridgecrew.ui.vulnerabilitiesTree

import javax.swing.Icon

interface CheckovTreeNode {

    val relativePathNode: String

    fun getNodeIcon(): Icon
}