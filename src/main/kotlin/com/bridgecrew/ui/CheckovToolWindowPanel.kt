package com.bridgecrew.ui

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel

class CheckovToolWindowPanel(val project: Project) : SimpleToolWindowPanel(false, true), Disposable {

    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    init {
        val main = project.service<CheckovToolWindowManagerPanel>()
        setContent(main)
    }

    override fun dispose() = Unit

}
