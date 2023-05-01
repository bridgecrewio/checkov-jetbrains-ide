package com.bridgecrew.ui.vulnerabilitiesTree

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.CheckType
import com.bridgecrew.results.VulnerabilityCheckovResult
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
import java.awt.BorderLayout
import java.io.File
import java.nio.file.Paths
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.ScrollPaneConstants
import javax.swing.tree.DefaultMutableTreeNode


class CheckovToolWindowTree(val project: Project, val split: JBSplitter, private val descriptionPanel: CheckovToolWindowDescriptionPanel) : SimpleToolWindowPanel(true, true), Disposable {
    private val resultsPanel = JPanel(BorderLayout())
    var isTreeEmpty = true

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
//        var checkovResults: List<BaseCheckovResult> = project.service<ResultsCacheService>().checkovResults
        val checkovResults: MutableList<BaseCheckovResult> = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(project.service<ResultsCacheService>().checkovResults).toMutableList()


        checkovResults.forEach { result ->
            run {
                result.filePath = updateFilePath(result)
//                if (result.category == Category.VULNERABILITIES) {
//                    val vulnerabilityResult = result as VulnerabilityCheckovResult
//                    if (vulnerabilityResult.checkType == CheckType.SCA_IMAGE) {
//                        if (!vulnerabilityResult.filePath.endsWith(vulnerabilityResult.imageName!!)) {
//                            result.filePath = Paths.get(result.filePath, File.separator, vulnerabilityResult.imageName).toString()
//                        }
//                    }
//
//                    if (vulnerabilityResult.checkType == CheckType.SCA_PACKAGE) {
//                        if (!vulnerabilityResult.filePath.endsWith(vulnerabilityResult.rootPackageName!!)) {
//                            result.filePath = Paths.get(result.filePath, File.separator, vulnerabilityResult.rootPackageName).toString()
//                        }
//                    }
//                }
            }
        }

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

        resultsPanel.add(tree)
        isTreeEmpty = tree.isEmpty
        return resultsPanel
    }
    private fun updateFilePath(result: BaseCheckovResult): String {
        if (result.category != Category.VULNERABILITIES) {
            return result.filePath
        }

        val vulnerabilityResult = result as VulnerabilityCheckovResult
        if (vulnerabilityResult.checkType == CheckType.SCA_IMAGE && !vulnerabilityResult.filePath.endsWith(vulnerabilityResult.imageName!!)) {

            return Paths.get(result.filePath, File.separator, vulnerabilityResult.imageName).toString()
//            if (!vulnerabilityResult.filePath.endsWith(vulnerabilityResult.imageName!!)) {
//                result.filePath = Paths.get(result.filePath, File.separator, vulnerabilityResult.imageName).toString()
//            }
        }

        if (vulnerabilityResult.checkType == CheckType.SCA_PACKAGE && !vulnerabilityResult.filePath.endsWith(vulnerabilityResult.rootPackageName!!)) {
            return Paths.get(result.filePath, File.separator, vulnerabilityResult.rootPackageName).toString()
//            if (!vulnerabilityResult.filePath.endsWith(vulnerabilityResult.rootPackageName!!)) {
//                result.filePath = Paths.get(result.filePath, File.separator, vulnerabilityResult.rootPackageName).toString()
//            }
        }

        return result.filePath
    }

    private fun createFolderTree(currentTree: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>, fileName: String) {
        val images: List<String?> = resultsPerFile.filter { it.category == Category.VULNERABILITIES && it.checkType == CheckType.SCA_IMAGE}.distinctBy { result -> (result as VulnerabilityCheckovResult).imageName }.map { (it as VulnerabilityCheckovResult).imageName }.filter { !it.isNullOrBlank() }
        val rootPackages: List<String?> = resultsPerFile.filter { it.category == Category.VULNERABILITIES && it.checkType == CheckType.SCA_PACKAGE}.map { (it as VulnerabilityCheckovResult).imageName }.distinct().filter { !it.isNullOrBlank() }
        val fileWithErrorsNode = buildFilePath(currentTree, fileName, images, rootPackages)
        addErrorNodesToFileNode(fileWithErrorsNode, resultsPerFile)
    }

    private fun addErrorNodesToFileNode(fileWithErrorsNode: DefaultMutableTreeNode, resultsPerFile: List<BaseCheckovResult>) {
        val resultsGroupedByResource: Map<String, List<BaseCheckovResult>> = resultsPerFile.groupBy { it.resource }
        val parentIcon = (fileWithErrorsNode.userObject as CheckovFileTreeNode).getNodeIcon()
        val secretsNodes = mutableListOf<DefaultMutableTreeNode>()

//        val vulnerabilitiesImage: List<BaseCheckovResult> = resultsPerFile.filter { result -> result.category == Category.VULNERABILITIES && result.checkType == CheckType.SCA_IMAGE }
//        val vulnerabilitiesPackage: List<BaseCheckovResult> = resultsPerFile.filter { result -> result.category == Category.VULNERABILITIES && result.checkType == CheckType.SCA_PACKAGE }

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

            if(resourceNode.childCount > 0)
                fileWithErrorsNode.add(resourceNode)
        }

        secretsNodes.forEach { node -> fileWithErrorsNode.add(node) }
    }

    private fun buildFilePath(currentTree: DefaultMutableTreeNode, fileName: String, images: List<String?>, rootPackages: List<String?>): DefaultMutableTreeNode {
        val filePathImage = images.find { image -> !image.isNullOrBlank() && fileName.endsWith(image) }
        val filePathRootPackage = rootPackages.find { rootPackage -> !rootPackage.isNullOrBlank() && fileName.endsWith(rootPackage) }

        var sanitizedFilePath = fileName

        if (!filePathImage.isNullOrBlank()) {
            sanitizedFilePath = fileName.replace(filePathImage, "")
        }

        if (!filePathRootPackage.isNullOrBlank()) {
            sanitizedFilePath = fileName.replace(filePathRootPackage, "")
        }


        val paths = sanitizedFilePath.split("/").toTypedArray().filter { it.isNotEmpty() }
        var currentNode = currentTree
        for(i in paths.indices){
            val newNode = if(i == paths.size - 1) CheckovFileTreeNode(paths[i]) else CheckovFolderTreeNode(paths[i])
            val existingChildNode = findExistingFilePathNodeInLevel(currentNode, newNode)
            currentNode = if(existingChildNode == null) {
                // need to add child
                val pathPartNode = DefaultMutableTreeNode(newNode)
                currentNode.add(pathPartNode)
                pathPartNode
            } else {
                // node exists, traverse to it
                existingChildNode
            }
        }

        if (!filePathImage.isNullOrBlank()) {
            val pathPartNode = DefaultMutableTreeNode(CheckovFileTreeNode(filePathImage))
            currentNode.add(pathPartNode)
            currentNode = pathPartNode
        }

        if (!filePathRootPackage.isNullOrBlank()) {
            val pathPartNode = DefaultMutableTreeNode(CheckovFileTreeNode(filePathRootPackage))
            currentNode.add(pathPartNode)
            currentNode = pathPartNode
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
