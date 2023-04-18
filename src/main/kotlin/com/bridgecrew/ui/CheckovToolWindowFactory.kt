package com.bridgecrew.ui

import com.bridgecrew.results.Category
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.topPanel.CheckovActionToolbar
import com.bridgecrew.ui.CheckovTabContent
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener

const val PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID = "Prisma Code Security"
const val OVERVIEW_TAB_NAME = "Overview"
const val IAC_TAB_NAME = "IaC"
const val VULNERABILITIES_TAB_NAME = "Vulnerabilities"
const val LICENSES_TAB_NAME = "Licenses"
const val SECRETS_TAB_NAME = "Secrets"

class CheckovToolWindowFactory : ToolWindowFactory {

    private val LOG = logger<CheckovToolWindowFactory>()

    private val tabNameToCategory: Map<String, Category?> = mapOf(
            OVERVIEW_TAB_NAME to null,
            IAC_TAB_NAME to Category.IAC,
            VULNERABILITIES_TAB_NAME to Category.VULNERABILITIES,
            LICENSES_TAB_NAME to Category.LICENSES,
            SECRETS_TAB_NAME to Category.SECRETS
    )

    companion object {
        var internalExecution = false
        var currentlyRunning = false
        private var lastSelectedTab = ""
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val checkovToolWindowPanel = CheckovToolWindowPanel(project)
        CheckovActionToolbar.setComponent(checkovToolWindowPanel)
        buildTabs(project, toolWindow, checkovToolWindowPanel)

        Disposer.register(project, checkovToolWindowPanel)

        val connection = project.messageBus.connect()
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                try {
                    if (!currentlyRunning && (internalExecution || toolWindowManager.activeToolWindowId == PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)) {
                        internalExecution = false
                        currentlyRunning = true
                        val selectedContent = toolWindowManager.getToolWindow(PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)?.contentManager?.selectedContent
                        if (selectedContent != null) {
                            val checkovTabContent = selectedContent as CheckovTabContent
                            refreshCounts(toolWindowManager, project)
                            reloadContents(project, checkovTabContent.id)
                        }
                    }
                } catch (e: Exception) {
                    LOG.error("Error while creating tool window: $e.message")
                } finally {
                    currentlyRunning = false
                }
            }
        })
    }

    private fun reloadContents(project: Project, tabId: String) {
        if (tabNameToCategory.keys.contains(tabId)) {
            if (lastSelectedTab != tabId) {
                val category = tabNameToCategory[tabId]
                project.service<ResultsCacheService>().updateCategory(category)
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED, null)
            }
            lastSelectedTab = tabId
        }
    }

    private fun buildTabs(project: Project, toolWindow: ToolWindow, checkovToolWindowPanel: CheckovToolWindowPanel) {
        val contentManager = toolWindow.contentManager
        tabNameToCategory.forEach { (name, category) ->
            val tabName = getTabName(project, name, category)
            val tabContent = CheckovTabContent(checkovToolWindowPanel, tabName, name, category)
            contentManager.addContent(tabContent)
        }
    }

    private fun getTabName(project: Project, name: String, category: Category?): String {
        val categories = if(category != null) listOf(category) else Category.values().toList()
        val resultsCount = project.service<ResultsCacheService>().getFilteredResults(categories, emptyList()).size
        return "$name ($resultsCount)"
    }

    private fun refreshCounts(toolWindowManager: ToolWindowManager, project: Project) {
        toolWindowManager.getToolWindow(PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)?.contentManager?.contents?.forEach { content ->
            val checkovTabContent = content as CheckovTabContent
            checkovTabContent.displayName = getTabName(project, checkovTabContent.id, checkovTabContent.category)
        }
    }
}