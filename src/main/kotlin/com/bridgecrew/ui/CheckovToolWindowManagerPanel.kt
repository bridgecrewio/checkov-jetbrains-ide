package com.bridgecrew.ui

import com.bridgecrew.services.CheckovScanService
import com.bridgecrew.listeners.CheckovInstallerListener
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.listeners.InitializationListener
import com.bridgecrew.services.checkovService.CheckovService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.utils.PANELTYPE
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.OnePixelSplitter
import java.awt.BorderLayout
import javax.swing.SwingUtilities

@Service
class CheckovToolWindowManagerPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private val checkovDescription = CheckovToolWindowDescriptionPanel(project)
    private val mainPanelSplitter = OnePixelSplitter(PANEL_SPLITTER_KEY, 0.5f)
    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    init {
        loadMainPanel(PANELTYPE.CHECKOV_INSTALATION_STARTED)
    }

    companion object {
        const val PANEL_SPLITTER_KEY = "CHECKOV_PANEL_SPLITTER_KEY"
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTO_CHOOSE_PANEL, fileName: String = "") {
        removeAll()
        add(CheckovActionToolbar(null), BorderLayout.NORTH)
        when (panelType) {
            PANELTYPE.CHECKOV_SCAN_ERROR -> {
                add(checkovDescription.errorScanDescription())
            }
            PANELTYPE.CHECKOV_SCAN_STARTED -> {
                add(checkovDescription.duringScanDescription())
            }
            PANELTYPE.CHECKOV_PRE_SCAN -> {
                add(checkovDescription.preScanDescription())
            }
            PANELTYPE.CHECKOV_SCAN_FINISHED_EMPTY -> {
                add(checkovDescription.successfulScanDescription(fileName))
            }
            PANELTYPE.CHECKOV_SCAN_PARSING_ERROR -> {
                add(checkovDescription.errorParsingScanDescription())
            }
            PANELTYPE.CHECKOV_INSTALATION_STARTED -> {
                add(checkovDescription.installationDescription())
            }
            PANELTYPE.CHECKOV_SCAN_FINISHED -> {
                removeAll()
                val checkovTree = CheckovToolWindowTree(project, mainPanelSplitter, checkovDescription)
                val descriptionPanel = checkovDescription.emptyDescription()
                val filesTreePanel = checkovTree.createScroll()
                add(CheckovActionToolbar(ScanResultMetadata(4, 35, 5222)), BorderLayout.NORTH)
                mainPanelSplitter.firstComponent = filesTreePanel
                mainPanelSplitter.secondComponent = descriptionPanel
                add(mainPanelSplitter)
            }
            PANELTYPE.AUTO_CHOOSE_PANEL ->{
                val setting = CheckovSettingsState().getInstance()
                when {
                    setting?.apiToken.isNullOrEmpty() -> add(checkovDescription.configurationDescription())
                    else -> add(checkovDescription.preScanDescription())
                }
            }
        }
        revalidate()
    }

    fun subscribeToProjectEventChange() {
        val extensionList = listOf("tf","yaml", "yml", "json")

        if (SwingUtilities.isEventDispatchThread()) {
            project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
        } else {
            ApplicationManager.getApplication().invokeLater {
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
            }
        }
        // subscribe to open file events
        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
            FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file);
//                if (extensionList.contains(file.extension)) {
                    project.service<CheckovScanService>().scanFile(file.path, project);
//                }
            }
        })

        // subscribe to update file events
        project.messageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.size>0 && events.get(0).file != null){
                    val relevantFile = extensionList.contains(events.get(0).file?.extension)
                    val relevantProject = ProjectRootManager.getInstance(project).fileIndex.isInContent(events.get(0).file!!)
                    if (events.size > 0 && relevantFile && relevantProject ){
                        project.service<CheckovScanService>().scanFile(events.get(0).file!!.path, project);
                    }
                }
            }
        })

    }
    fun subscribeToInternalEvents(project: Project){
        // Subscribe to Scanning Topic
        project.messageBus.connect(this)
            .subscribe(CheckovScanListener.SCAN_TOPIC, object: CheckovScanListener {
                override fun scanningStarted() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_SCAN_STARTED)
                }
                override fun scanningFinished() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_SCAN_FINISHED)
                    }
                }
                override fun scanningFinished(fileName: String) {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_SCAN_FINISHED_EMPTY, fileName)
                    }
                }
                override fun scanningError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_SCAN_ERROR)
                    }
                }
                override fun scanningParsingError() {
                    ApplicationManager.getApplication().invokeLater {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_SCAN_PARSING_ERROR)
                    }
                }
            })

        // Subscribe to Installer Topic
//        project.messageBus.connect(this)
//            .subscribe(CheckovInstallerListener.INSTALLER_TOPIC, object: CheckovInstallerListener {
//                override fun installerFinished(serviceClass: CheckovService) {
//                    project.service<CheckovScanService>().selectedCheckovScanner = serviceClass
////                    project.service<CheckovToolWindowManagerPanel>().subscribeToProjectEventChange()
//                    project.messageBus.syncPublisher(InitializationListener.INITIALIZATION_TOPIC).initializationCompleted()
//                }
//            })

        // Subscribe to Settings Topic
        project.messageBus.connect(this)
            .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object: CheckovSettingsListener {
                override fun settingsUpdated() {
                    project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
                }
            })
    }
    override fun dispose() = Unit
}
