package com.bridgecrew.ui

import com.bridgecrew.CheckovResult
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.utils.CHECKOVERROR
import com.bridgecrew.utils.CHECKOVPRERSCAN
import com.bridgecrew.utils.CHECKOVSTARTED
import com.bridgecrew.utils.AUTOCHOOSEPANEL
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.JBSplitter

@Service
class CheckovToolWindowManagerPanel(val project: Project) : SimpleToolWindowPanel(false, true), Disposable {

    val checkovDescription = CheckovToolWindowDescriptionPanel(project)
    val split = JBSplitter()
    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    init {
        loadMainPanel(CHECKOVPRERSCAN)

        project.messageBus.connect(this)
            .subscribe(CheckovScanListener.SCAN_TOPIC, object: CheckovScanListener {
                override fun scanningStarted() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel(CHECKOVSTARTED)

                }

                override fun scanningFinished(scanResults: ArrayList<CheckovResult>) {
                    ApplicationManager.getApplication().invokeLater {
                        println(scanResults)
                        project.service<CheckovToolWindowManagerPanel>().displayResults(scanResults)

                    }
                }

                override fun scanningError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(CHECKOVERROR)
                    }
                }
            })

        }

    fun displayResults(checkovResults: ArrayList<CheckovResult>) {
        val setting = CheckovSettingsState().getInstance()
        println(setting?.apiToken)
        removeAll()
        val checkovTree = CheckovToolWindowTree(project, checkovDescription)
        val right = checkovDescription.createScroll()
        val left = checkovTree.createScroll(checkovResults)
        split.setFirstComponent(left)
        split.setSecondComponent(right)
        add(split)
        revalidate()
    }

    fun loadMainPanel(panelType: Int = AUTOCHOOSEPANEL) {
        removeAll()
        when (panelType) {
            CHECKOVERROR -> {
                add(checkovDescription.errorScanDescription())
            }
            CHECKOVSTARTED -> {
                add(checkovDescription.duringScanDescription())
            }
            CHECKOVPRERSCAN -> {
                add(checkovDescription.preScanDescription())
            }
        }
        revalidate()
    }

    override fun dispose() = Unit

}
