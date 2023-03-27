package com.bridgecrew.ui.vulnerabilitiesTree

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.CheckovToolWindowDescriptionPanel
import com.bridgecrew.utils.navigateToFile
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBSplitter
import com.intellij.ui.ScrollPaneFactory
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
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
        val fileToResourceMap = project.service<ResultsCacheService>().getCheckovResultsFilteredBySeverityGroupedByPath(null)

        val rootNode = DefaultMutableTreeNode("")

        fileToResourceMap.forEach { (filePath, resources) ->
            createFolderTree(rootNode, resources, filePath)
        }

        val tree = Tree(rootNode).apply {
            this.isRootVisible = false
        }

        tree.cellRenderer = CheckovTreeRenderer()

        tree.selectionModel.addTreeSelectionListener {
            ApplicationManager.getApplication().invokeLater {
                val selectionPath = tree.selectionPath
                val node: DefaultMutableTreeNode = selectionPath!!.lastPathComponent as DefaultMutableTreeNode
                if (node.userObject is CheckovVulnerabilityTreeNode) {
                    val vulnerabilityTreeNode = node.userObject as CheckovVulnerabilityTreeNode
                    val checkovResult = vulnerabilityTreeNode.checkovResult
                    split.secondComponent = descriptionPanel.createScroll(checkovResult)
                    WriteCommandAction.runWriteCommandAction(project) {
                        navigateToFile(project, checkovResult.absoluteFilePath, checkovResult.fileLineRange[0])
                    }
                }
            }
        }

        resultsPanel.add(tree)
        return resultsPanel
    }

    private fun createFolderTree(currentTree: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>, fileName: String) {
        val fileWithErrorsNode = buildFilePath(currentTree, fileName)
        addErrorNodesToFileNode(fileWithErrorsNode, resultsPerFile)
    }

    private fun addErrorNodesToFileNode(fileWithErrorsNode: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>) {
        val resultsGroupedByResource = resultsPerFile.groupBy { it.resource }
        val parentIcon = (fileWithErrorsNode.userObject as CheckovFileTreeNode).getNodeIcon()

        resultsGroupedByResource.forEach { (resource, results) ->
            val resourceNode = DefaultMutableTreeNode(CheckovResourceTreeNode(resource, parentIcon))
            results.forEach { checkovResult ->
                val checkName = DefaultMutableTreeNode(CheckovVulnerabilityTreeNode(checkovResult))
                resourceNode.add(checkName)
            }
            fileWithErrorsNode.add(resourceNode)
        }
    }

    private fun buildFilePath(currentTree: DefaultMutableTreeNode, fileName: String): DefaultMutableTreeNode{
        val paths = fileName.split("/").toTypedArray().filter { it.isNotEmpty() }
        var currentNode = currentTree
        for(i in paths.indices){
            val newNode = if(i == paths.size - 1) CheckovFileTreeNode(paths[i]) else CheckovFolderTreeNode(paths[i])
            val existingChildNode = findExistingFilePathNodeInLevel(currentNode, newNode)
            currentNode = if(existingChildNode == null){
                // need to add child
                val pathPartNode = DefaultMutableTreeNode(newNode)
                currentNode.add(pathPartNode)
                pathPartNode
            } else {
                // node exists, traverse to it
                existingChildNode
            }
        }
        return currentNode
    }

    private fun findExistingFilePathNodeInLevel(currentNode: DefaultMutableTreeNode, userObject: CheckovTreeNode): DefaultMutableTreeNode? {
        for(child in currentNode.children()){
            val found = (child as DefaultMutableTreeNode).userObject.equals(userObject)
            if(found){
                return child
            }
        }
        return null
    }

    override fun dispose() = Unit
}
