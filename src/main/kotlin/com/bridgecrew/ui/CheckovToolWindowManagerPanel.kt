package com.bridgecrew.ui

import com.bridgecrew.services.CheckovService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.CheckovSettingsState
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
        loadMainPanel(PANELTYPE.CHECKOVINSTALATION)

        project.messageBus.connect(this)
            .subscribe(CheckovScanListener.SCAN_TOPIC, object: CheckovScanListener {
                override fun scanningStarted() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVSTARTED)

                }

                override fun scanningFinished() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().displayResults()

                    }
                }

                override fun scanningFinished(fileName: String) {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVFINISHED, fileName)

                    }
                }

                override fun scanningError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVERROR)
                    }
                }

                override fun scanningParsingError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVPARSINGERROR)
                    }
                }
            })

        project.messageBus.connect(this)
            .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object: CheckovInstallerListener {
                override fun installerFinished() {
                    subscribeToListeners()
                }
            })

        project.messageBus.connect(this)
            .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object: CheckovSettingsListener {
                override fun settingsUpdated() {
                    loadMainPanel()
                }
            })
        }



    fun displayResults() {
        removeAll()
        val checkovTree = CheckovToolWindowTree(project, split, checkovDescription)
        val right = checkovDescription.emptyDescription()
        val left = checkovTree.createScroll()
        split.setFirstComponent(left)
        split.setSecondComponent(right)
        add(split)
        revalidate()
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTOCHOOSEPANEL, fileName: String = "") {
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
            PANELTYPE.CHECKOVFINISHED -> {
                add(checkovDescription.successfulScanDescription(fileName))
            }
            PANELTYPE.CHECKOVPARSINGERROR -> {
                add(checkovDescription.errorParsingScanDescription())
            }
            PANELTYPE.CHECKOVINSTALATION -> {
                add(checkovDescription.installationDescription())
            }
            PANELTYPE.AUTOCHOOSEPANEL ->{
                val setting = CheckovSettingsState().getInstance()
                when {
                    setting?.apiToken.isNullOrEmpty() -> add(checkovDescription.configurationDescription())
                    else -> add(checkovDescription.preScanDescription())
                }
            }
        }
        revalidate()
    }

    private fun subscribeToListeners() {
        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOVPRERSCAN)

        val extensionList = listOf("tf","yaml", "yaml", "json")

        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file);
                if (extensionList.contains(file.extension)) {
                    project.service<CheckovService>().scanFile(file.path, project);
                }

            }
        })
        project.messageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.size > 0 && extensionList.contains(events.get(0).file?.extension )) {
                    project.service<CheckovService>().scanFile(events.get(0).file!!.path, project);
                }
            }
        })
    }

    override fun dispose() = Unit

}
