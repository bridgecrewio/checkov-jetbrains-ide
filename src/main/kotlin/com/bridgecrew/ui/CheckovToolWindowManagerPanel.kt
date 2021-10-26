package com.bridgecrew.ui

import com.bridgecrew.CheckovResult
import com.bridgecrew.services.CheckovService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
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
        loadMainPanel(PANELTYPE.CHECKOVPRERSCAN)

        project.messageBus.connect(this)
            .subscribe(CheckovScanListener.SCAN_TOPIC, object: CheckovScanListener {
                override fun scanningStarted() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVSTARTED)

                }

                override fun scanningFinished(scanResults: ArrayList<CheckovResult>) {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().displayResults(scanResults)

                    }
                }

                override fun scanningError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVERROR)
                    }
                }
            })

        project.messageBus.connect(this)
            .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object: CheckovInstallerListener {
                override fun installerFinished() {
                    subscribeToListeners()
                }
            })
        }

    fun displayResults(checkovResults: ArrayList<CheckovResult>) {
        removeAll()
        val checkovTree = CheckovToolWindowTree(project, checkovDescription)
        val right = checkovDescription.createScroll()
        val left = checkovTree.createScroll(checkovResults)
        split.setFirstComponent(left)
        split.setSecondComponent(right)
        add(split)
        revalidate()
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTOCHOOSEPANEL) {
        removeAll()
        when (panelType) {
            PANELTYPE.CHECKOVERROR -> {
                add(checkovDescription.errorScanDescription())
            }
            PANELTYPE.CHECKOVSTARTED -> {
                add(checkovDescription.duringScanDescription())
            }
            PANELTYPE.CHECKOVPRERSCAN -> {
                add(checkovDescription.preScanDescription())
            }
        }
        revalidate()
    }

    private fun subscribeToListeners() {
        val extensionList = listOf("tf","yaml", "yaml", "json")

        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file);
                if (extensionList.contains(file.extension)) {
                    project.service<CheckovService>().scanFile(file.path, "unknown", "apitoken", project);
                }

            }
        })
        project.messageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.size > 0 && extensionList.contains(events.get(0).file?.extension )) {
                    project.service<CheckovService>().scanFile(events.get(0).file!!.path, "unknown", "apitoken", project);
                }
            }
        })
    }

    override fun dispose() = Unit

}
