package com.bridgecrew.ui.vulnerabilitiesTree

import com.bridgecrew.settings.CheckovGlobalState
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.CheckovResultsListUtils
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
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.event.TreeExpansionEvent
import javax.swing.event.TreeExpansionListener
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.TreeNode
import javax.swing.tree.TreePath


class CheckovToolWindowTree(
    val project: Project,
    val split: JBSplitter,
    private val descriptionPanel: CheckovToolWindowDescriptionPanel
) : SimpleToolWindowPanel(true, true), Disposable {
    private val resultsPanel = JPanel(BorderLayout())
    var isTreeEmpty = true

    init {
        resultsPanel.background = UIUtil.getEditorPaneBackground() ?: resultsPanel.background
    }

    /**
     * Create scrollers panel around a Tree element
     * @return JScrollPane of the Tree element
     */
    fun createScroll(): JScrollPane {
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
    fun createTree(): JPanel {
        var checkovResults: List<BaseCheckovResult> = project.service<ResultsCacheService>().checkovResults
        checkovResults = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(checkovResults).toMutableList()
        CheckovResultsListUtils.sortResults(checkovResults)

        val fileToResourceMap: Map<String, List<BaseCheckovResult>> = checkovResults.groupBy { it.filePath }

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

        expandsFromPathState(tree, CheckovGlobalState!!.expandedDescendants)

        tree.addTreeExpansionListener(object : TreeExpansionListener {
            override fun treeExpanded(event: TreeExpansionEvent?) {
                CheckovGlobalState!!.expandedDescendants = getExpandedDescendants((event?.source as Tree))
            }

            override fun treeCollapsed(event: TreeExpansionEvent?) {
                CheckovGlobalState!!.expandedDescendants = getExpandedDescendants((event?.source as Tree))
            }
        })

        resultsPanel.add(tree)
        isTreeEmpty = tree.isEmpty
        return resultsPanel
    }

    private fun expandsFromPathState(tree: Tree, expandsTreePaths: List<TreePath>) {
        expandsTreePaths.forEach {
//            TODO: instead of searching the tree for each, expand while scanning
                path ->
            tree.expandPath(
                findTreePathByUserObject(
                    tree.model.root as DefaultMutableTreeNode,
                    (path.lastPathComponent as DefaultMutableTreeNode).userObject
                )
            )
        }
    }

    private fun findTreePathByUserObject(root: DefaultMutableTreeNode, userObject: Any): TreePath? {
        val stack = Stack<Pair<DefaultMutableTreeNode, TreePath>>()
        stack.push(Pair(root, TreePath(root)))

        while (stack.isNotEmpty()) {
            val (node, path) = stack.pop()
            if (node.userObject == userObject) {
                return path
            }
            for (i in 0 until node.childCount) {
                val childNode = node.getChildAt(i) as DefaultMutableTreeNode
                val childPath = path.pathByAddingChild(childNode)
                stack.push(Pair(childNode, childPath))
            }
        }

        return null
    }

    private fun getExpandedDescendants(tree: Tree): List<TreePath> {
        val rootPath = TreePath(tree.model.root)
        return tree.getExpandedDescendants(rootPath).toList()
    }

    private fun createFolderTree(
        currentTree: DefaultMutableTreeNode,
        resultsPerFile: List<BaseCheckovResult>,
        fileName: String
    ) {
        val fileWithErrorsNode = buildFilePath(currentTree, fileName)
        addErrorNodesToFileNode(fileWithErrorsNode, resultsPerFile)
    }

    private fun addErrorNodesToFileNode(
        fileWithErrorsNode: DefaultMutableTreeNode,
        resultsPerFile: List<BaseCheckovResult>
    ) {
        val resultsGroupedByResource: Map<String, List<BaseCheckovResult>> = resultsPerFile.groupBy { it.resource }
        val parentIcon = (fileWithErrorsNode.userObject as CheckovFileTreeNode).getNodeIcon()
        val secretsNodes = mutableListOf<DefaultMutableTreeNode>()

        resultsGroupedByResource.forEach { (resource, results) ->
            val resourceNode = DefaultMutableTreeNode(CheckovResourceTreeNode(resource, parentIcon))
            results.forEach { checkovResult ->
                val checkName = DefaultMutableTreeNode(CheckovVulnerabilityTreeNode(checkovResult))
                if (checkovResult.category == Category.SECRETS) {
                    secretsNodes.add(checkName)
                } else {
                    resourceNode.add(checkName)
                }
            }

            if (resourceNode.childCount > 0)
                fileWithErrorsNode.add(resourceNode)
        }

        secretsNodes.forEach { node -> fileWithErrorsNode.add(node) }
    }

    private fun buildFilePath(currentTree: DefaultMutableTreeNode, fileName: String): DefaultMutableTreeNode {
        val paths = fileName.split("/").toTypedArray().filter { it.isNotEmpty() }
        var currentNode = currentTree
        for (i in paths.indices) {
            val newNode = if (i == paths.size - 1) CheckovFileTreeNode(paths[i]) else CheckovFolderTreeNode(paths[i])
            val existingChildNode = findExistingFilePathNodeInLevel(currentNode, newNode)
            currentNode = if (existingChildNode == null) {
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

    private fun findExistingFilePathNodeInLevel(
        currentNode: DefaultMutableTreeNode,
        userObject: CheckovTreeNode
    ): DefaultMutableTreeNode? {
        for (child in currentNode.children()) {
            val found = (child as DefaultMutableTreeNode).userObject.equals(userObject)
            if (found) {
                return child
            }
        }
        return null
    }

    override fun dispose() = Unit
}
