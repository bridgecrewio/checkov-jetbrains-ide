package com.bridgecrew.ui

import com.bridgecrew.utils.*
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.bridgecrew.CheckovResult
import com.bridgecrew.ResourceToResultsMap
import com.bridgecrew.services.ResultsCacheService

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.ui.treeStructure.Tree

import javax.swing.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager

import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.ui.ScrollPaneFactory
import javax.swing.tree.DefaultMutableTreeNode


class CheckovToolWindowTree(val project: Project, descriptionPanel: CheckovToolWindowDescriptionPanel) : SimpleToolWindowPanel(true, true), Disposable {
    val testPanel = JPanel(BorderLayout())
    val descriptionPanel =  descriptionPanel

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
        val resultsMap = project.service<ResultsCacheService>().getAllResults()

        val rootNode = DefaultMutableTreeNode("")

        resultsMap.keys.forEach {
            val fileNode = createFileTree(resultsMap[it]!!)
            rootNode.add(fileNode)
        }

        val tree = Tree(rootNode).apply {
            this.isRootVisible = false
        }

        tree.selectionModel.addTreeSelectionListener {
            ApplicationManager.getApplication().invokeLater {
                val selectionPath = tree.selectionPath
                val node: DefaultMutableTreeNode = selectionPath!!.lastPathComponent as DefaultMutableTreeNode
                val checkoveTreeNode = node.userObject as CheckovTreeNode
                val checkoveResult = checkoveTreeNode.checkovResultObject
                if (selectionPath.pathCount == CHECKNAMEDEPTH) {
                    navigateAndSelectFailure(tree, checkoveResult)
                }
            }
        }

        testPanel.add(tree)
        return testPanel
    }

    private fun createFileTree(checkovResults: ResourceToResultsMap): DefaultMutableTreeNode  {
        val firstEntry = checkovResults.getValue(checkovResults.keys.first())
        val firstFile = firstEntry.first()
        val rootNode = DefaultMutableTreeNode(CheckovFileNameTreeNode(firstFile, project))

        checkovResults.forEach { result ->
            val resource = DefaultMutableTreeNode(CheckovResourceTreeNode(result.value.first()))
            result.value.forEach {
                val checkName = DefaultMutableTreeNode(CheckovCheckNameTreeNode(it))
                resource.add(checkName)
            }
            rootNode.add(resource)
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
            descriptionPanel.descriptionOfCheckovScan(checkovResultObject)
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
