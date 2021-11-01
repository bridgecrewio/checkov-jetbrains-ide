package com.bridgecrew.ui

import com.bridgecrew.utils.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.bridgecrew.CheckovResult
import com.bridgecrew.ResourceToCheckovResultsList
import com.bridgecrew.services.ResultsCacheService

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.treeStructure.Tree

import javax.swing.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.ui.JBSplitter

import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.ui.ScrollPaneFactory
import javax.swing.tree.DefaultMutableTreeNode


class CheckovToolWindowTree(val project: Project, val split: JBSplitter, private val descriptionPanel: CheckovToolWindowDescriptionPanel) : SimpleToolWindowPanel(true, true), Disposable {
    private val resultsPanel = JPanel(BorderLayout())

    /**
     * Create scrollers panel around a Tree element
     * @return JScrollPane of the Tree element
     */
    fun createScroll(): JScrollPane{
        val tree = createTree()
        return ScrollPaneFactory.createScrollPane(
            tree,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }

    /**
     * Create a tree element from checkovResult list
     * @return Panel which contains a tree element
     */
    fun createTree() : JPanel {
        val fileToResourceMap = project.service<ResultsCacheService>().getAllResults()

        val rootNode = DefaultMutableTreeNode("")

        fileToResourceMap.forEach { (fileName, resources) ->
            val fileNode = createFileTree(resources!!, fileName)
            rootNode.add(fileNode)
        }

        val tree = Tree(rootNode).apply {
            this.isRootVisible = false
        }

        tree.selectionModel.addTreeSelectionListener {
            ApplicationManager.getApplication().invokeLater {
                val selectionPath = tree.selectionPath
                val node: DefaultMutableTreeNode = selectionPath!!.lastPathComponent as DefaultMutableTreeNode
                if (selectionPath.pathCount == CHECKNAMEDEPTH) {
                    val checkovTreeNode = node.userObject as CheckovTreeNode
                    val checkovResult = checkovTreeNode.checkovResultObject
                    split.setSecondComponent(descriptionPanel.createScroll(checkovResult))
                    navigateAndSelectFailure(tree, checkovResult)
                }
            }
        }

        resultsPanel.add(tree)
        return resultsPanel
    }

    private fun createFileTree(resources: ResourceToCheckovResultsList, fileName: String): DefaultMutableTreeNode {
        val rootNode = DefaultMutableTreeNode(fileName)

        resources.forEach { resource ->
            val resourceNode = DefaultMutableTreeNode(resource.key)
            resource.value.forEach { checkovResult ->
                val checkName = DefaultMutableTreeNode(CheckovCheckNameTreeNode(checkovResult))
                resourceNode.add(checkName)
            }
            rootNode.add(resourceNode)
        }

        return rootNode
    }

    /**
     * Navigate to the selected file and select the failed check area.
     * @return Unit
     */
    private fun navigateAndSelectFailure(tree: Tree, checkovResultObject: CheckovResult){
        val selectionPath = tree.selectionPath
        if (selectionPath.pathCount == CHECKNAMEDEPTH) {
            val fileToNavigate: PsiFile? =
                getPsFileByPath(selectionPath.parentPath.parentPath.lastPathComponent.toString(),
                    project = project)
            if (fileToNavigate != null) {
                navigateToFile(fileToNavigate)
            }
            val range = checkovResultObject.file_line_range
            val (startOffset, endOffset) = getOffsetHighlighByLines(range, project)
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            editor?.selectionModel?.setSelection(startOffset, endOffset)
        }
    }

    override fun dispose() = Unit
}
