package com.bridgecrew.ui

import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.Disposable
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.bridgecrew.CheckovResult

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.ui.treeStructure.Tree

import javax.swing.*
import com.intellij.ui.components.labels.LinkLabel
import com.intellij.openapi.application.ApplicationManager

import javax.swing.JFrame
import javax.swing.JTree
import javax.swing.JPanel
import java.awt.BorderLayout
import com.intellij.ui.ScrollPaneFactory
import javax.swing.tree.DefaultMutableTreeNode

class CheckovToolWindowTreeNode(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {
    val fileList = listOf("src/main/java/com/jfrog/ide/idea/ui/ComponentDetails.java","src/main/java/com/jfrog/ide/idea/ui/ComponentIssueDetails.java","src/main/java/com/jfrog/ide/idea/ui/ComponentsTree.java")
    val checkovTestAnswer = CheckovToolWindow().getResultsList()
    val testPanel = JPanel(BorderLayout())

    fun createTree() : JPanel {

        val rootNode = DefaultMutableTreeNode(fileList[0])
        checkovTestAnswer.sortBy { it.file_path }
        checkovTestAnswer.forEach {
            val child1 = DefaultMutableTreeNode(it.file_path)
            val child1child1 =  DefaultMutableTreeNode(it.resource)
            val child1child1child1 =  DefaultMutableTreeNode(it.check_id)
            child1child1.add(child1child1child1)
            child1.add(child1child1)
            rootNode.add(child1)
        }

        val tree = Tree(rootNode).apply {
            this.isRootVisible = false
        }
//        val treePanel = JPanel()
//        treePanel.add(tree)
//        val scrollPane = ScrollPaneFactory.createScrollPane(
//            treePanel,
//            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
//            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
//        )
        tree.selectionModel.addTreeSelectionListener {
            ApplicationManager.getApplication().invokeLater {
                val selectionPath = tree.selectionPath
                val fileToNavigate: PsiFile? = getPsFileByPath(selectionPath.lastPathComponent.toString(), project=project)
                if (fileToNavigate != null) {
                    navigateToFile(fileToNavigate)
                }
            }
        }

        testPanel.add(tree)
        return testPanel
    }

    private fun navigateToFile(fileToNavigate: PsiFile) {
            PsiNavigationSupport.getInstance().createNavigatable(
                fileToNavigate.project,
                fileToNavigate.virtualFile,
                0
            ).navigate(false)
        }

    private fun getPsFileByPath(pathTest: String, project: Project): PsiFile? {
        val absolutePath = project.getBasePath() + if (pathTest.startsWith("/")) pathTest else "/$pathTest"
        val virtualFile = LocalFileSystem.getInstance().findFileByPath(absolutePath)
        if (virtualFile != null) {
            return PsiManager.getInstance(project).findFile(virtualFile)
        }
        return null
    }
    override fun dispose() = Unit

}
