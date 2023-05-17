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
    val project: Project, val split: JBSplitter, private val descriptionPanel: CheckovToolWindowDescriptionPanel, private val selectedPath: String
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
            tree, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
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
                CheckovGlobalState.expandedDescendants = getExpandedDescendants((event?.source as Tree))
            }

            override fun treeCollapsed(event: TreeExpansionEvent?) {
                CheckovGlobalState.expandedDescendants = getExpandedDescendants((event?.source as Tree))
            }
        })

        selectNodeBySelectedPath(tree)

        resultsPanel.add(tree)
        isTreeEmpty = tree.isEmpty
        return resultsPanel
    }

    //use this function to dynamically select a specific node in the tree by using `selectedPath`
    private fun selectNodeBySelectedPath(tree: Tree) {
        if(selectedPath == "") return
        val root = tree.model.root as DefaultMutableTreeNode
        val userObjectNode = findNode(root) as DefaultMutableTreeNode

        val selectedPath = findTreePathByUserObject(root, userObjectNode.userObject)
        if(selectedPath != null) {
            tree.selectionPath = selectedPath
        }
    }

    // find the node in the existing tree by a given similar node (same path+resource+vulnerability).
    private fun findNode(root: TreeNode): TreeNode {
        val stack = Stack<DefaultMutableTreeNode>()
        var foundNode = root as DefaultMutableTreeNode
        stack.push(foundNode)

        while (stack.isNotEmpty()) {
            val node = stack.pop()
            if(node.userObject is CheckovTreeNode && (node.userObject as CheckovTreeNode).relativePathNode == selectedPath ) {
                foundNode = node
                break
            }
            for (i in 0 until node.childCount) {
                val childNode = node.getChildAt(i) as DefaultMutableTreeNode
                stack.push(childNode)
            }
        }

        return foundNode
    }

    private fun expandsFromPathState(tree: Tree, expandsTreePaths: List<TreePath>) {
        expandsTreePaths.forEach {
//            TODO: instead of searching the tree for each, expand while scanning
                path ->
            val currentTreePath = findTreePathByUserObject(
                tree.model.root as DefaultMutableTreeNode, (path.lastPathComponent as DefaultMutableTreeNode).userObject
            )
            if(currentTreePath != null){
                tree.expandPath(currentTreePath)
            }
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
        currentTree: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>, fileName: String
    ) {
        val fileWithErrorsNode = buildFilePath(currentTree, fileName)
        addErrorNodesToFileNode(fileWithErrorsNode, resultsPerFile)
    }

    private fun addErrorNodesToFileNode(
        fileWithErrorsNode: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>
    ) {
        val resultsGroupedByResource: Map<String, List<BaseCheckovResult>> = resultsPerFile.groupBy { it.resource }
        val parentIcon = (fileWithErrorsNode.userObject as CheckovFileTreeNode).getNodeIcon()
        val secretsNodes = mutableListOf<DefaultMutableTreeNode>()
        var relativeFilePath = (fileWithErrorsNode.userObject as CheckovTreeNode).relativePathNode

        resultsGroupedByResource.forEach { (resource, results) ->
            val resourceNode = DefaultMutableTreeNode(CheckovResourceTreeNode(resource, parentIcon, "${relativeFilePath}/${resource}"))
            results.forEach { checkovResult ->
                val checkName = DefaultMutableTreeNode(CheckovVulnerabilityTreeNode(checkovResult, "${relativeFilePath}/${resource}/${checkovResult.name}"))
                if (checkovResult.category == Category.SECRETS) {
                    secretsNodes.add(checkName)
                } else {
                    resourceNode.add(checkName)
                }
            }

            if (resourceNode.childCount > 0) fileWithErrorsNode.add(resourceNode)
        }

        secretsNodes.forEach { node -> fileWithErrorsNode.add(node) }
    }

    private fun buildFilePath(currentTree: DefaultMutableTreeNode, fileName: String): DefaultMutableTreeNode {
        val paths = fileName.split("/").toTypedArray().filter { it.isNotEmpty() }
        var currentNode = currentTree
        var relativePath = ""
        for (i in paths.indices) {
            relativePath += "/${paths[i]}"
            val newNode = if (i == paths.size - 1) CheckovFileTreeNode(paths[i], relativePath) else CheckovFolderTreeNode(paths[i], relativePath)
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
        currentNode: DefaultMutableTreeNode, userObject: CheckovTreeNode
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
