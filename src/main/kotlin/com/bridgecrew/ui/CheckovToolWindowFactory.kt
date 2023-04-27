package com.bridgecrew.ui

import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.results.Category
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.ui.topPanel.CheckovActionToolbar
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.util.messages.MessageBusConnection

const val PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID = "Prisma Code Security"
const val OVERVIEW_TAB_NAME = "Overview"
const val IAC_TAB_NAME = "IaC"
const val VULNERABILITIES_TAB_NAME = "Vulnerabilities"
const val LICENSES_TAB_NAME = "Licenses"
const val SECRETS_TAB_NAME = "Secrets"

private val tabNameToCategory: Map<String, Category?> = mapOf(
        OVERVIEW_TAB_NAME to null,
        IAC_TAB_NAME to Category.IAC,
        VULNERABILITIES_TAB_NAME to Category.VULNERABILITIES,
        LICENSES_TAB_NAME to Category.LICENSES,
        SECRETS_TAB_NAME to Category.SECRETS
)

class CheckovToolWindowFactory : ToolWindowFactory {

    private val LOG = logger<CheckovToolWindowFactory>()

    companion object {
        var internalExecution = false
        var currentlyRunning = false
        private var lastSelectedTab = ""
        var lastSelectedCategory = if (tabNameToCategory.contains(lastSelectedTab)) tabNameToCategory[lastSelectedTab] else null
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val checkovToolWindowPanel = CheckovToolWindowPanel(project)
        CheckovActionToolbar.setComponent(checkovToolWindowPanel)
        buildTabs(project, toolWindow, checkovToolWindowPanel)

        Disposer.register(project, checkovToolWindowPanel)

        val connection: MessageBusConnection = project.messageBus.connect()
        connection.subscribe(InitializationListener.INITIALIZATION_TOPIC, object : InitializationListener {
            override fun initializationCompleted() {
                subscribeToTollWindowManagerEvents(connection, project)
            }

        })
    }

    private fun subscribeToTollWindowManagerEvents(connection: MessageBusConnection, project: Project) {
        connection.subscribe(ToolWindowManagerListener.TOPIC, object : ToolWindowManagerListener {
            override fun stateChanged(toolWindowManager: ToolWindowManager) {
                try {
                    if (!currentlyRunning && (internalExecution || toolWindowManager.activeToolWindowId == PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)) {
                        internalExecution = false
                        currentlyRunning = true
                        val selectedContent = toolWindowManager.getToolWindow(PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)?.contentManager?.selectedContent

                        if (selectedContent == null) {
                            return
                        }

                        refreshCounts(toolWindowManager, project)

                        val checkovTabContent = selectedContent as CheckovTabContent
                        reloadContents(project, checkovTabContent.id)
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
                lastSelectedCategory = category
                SeverityFilterActions.onChangeCategory(category, project)
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_LOAD_TABS_CONTENT)
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
        val checkovResults = project.service<ResultsCacheService>().checkovResults
        val resultsCount = CheckovResultsListUtils.filterResultsByCategoriesAndSeverities(checkovResults, categories).size
        return "$name ($resultsCount)"
    }

    private fun refreshCounts(toolWindowManager: ToolWindowManager, project: Project) {
        toolWindowManager.getToolWindow(PRISMA_CODE_SECUTIRY_TOOL_WINDOW_ID)?.contentManager?.contents?.forEach { content ->
            val checkovTabContent = content as CheckovTabContent
            checkovTabContent.displayName = getTabName(project, checkovTabContent.id, checkovTabContent.category)
        }
    }
}