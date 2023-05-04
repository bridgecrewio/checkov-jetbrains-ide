package com.bridgecrew.ui

import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.listeners.CheckovScanListener
import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.services.CheckovResultsListUtils
import com.bridgecrew.services.ResultsCacheService
import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.ui.actions.CheckovScanAction
import com.bridgecrew.ui.errorBubble.CheckovGutterErrorIcon
import com.bridgecrew.ui.actions.SeverityFilterActions
import com.bridgecrew.ui.topPanel.CheckovTopPanel
import com.bridgecrew.ui.vulnerabilitiesTree.CheckovToolWindowTree
import com.bridgecrew.utils.FULL_SCAN_EXCLUDED_PATHS
import com.bridgecrew.utils.PANELTYPE
import com.bridgecrew.utils.getGitIgnoreValues
import com.bridgecrew.utils.toVirtualFilePath
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.markup.HighlighterLayer
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.ui.OnePixelSplitter
import java.awt.BorderLayout
import javax.swing.SwingUtilities

@Service
class CheckovToolWindowManagerPanel(val project: Project) : SimpleToolWindowPanel(true, true), Disposable {

    private val checkovDescription = CheckovToolWindowDescriptionPanel(project)
    private val mainPanelSplitter = OnePixelSplitter(PANEL_SPLITTER_KEY, 0.5f)
    private val LOG = logger<CheckovToolWindowManagerPanel>()
    private val key = Key<Boolean>("prismaCloudPlugin")

    /**
     * Create Splitter element which contains the tree element and description element
     * @return JBSplitter
     */
    init {
        loadMainPanel(PANELTYPE.CHECKOV_INITIALIZATION_PROGRESS)
    }

    companion object {
        const val PANEL_SPLITTER_KEY = "CHECKOV_PANEL_SPLITTER_KEY"
    }

    fun loadMainPanel(panelType: Int = PANELTYPE.AUTO_CHOOSE_PANEL) {
        removeAll()
        add(CheckovTopPanel(project), BorderLayout.NORTH)
        reloadTabCounts()
        when (panelType) {
            PANELTYPE.CHECKOV_LOAD_TABS_CONTENT -> {
                loadTabsContent(panelType)
            }

            PANELTYPE.CHECKOV_REPOSITORY_SCAN_STARTED -> {
                add(checkovDescription.duringScanDescription("Scanning your repository..."))
            }

            PANELTYPE.CHECKOV_INITIALIZATION_PROGRESS -> {
                add(checkovDescription.initializationDescription())
            }

            PANELTYPE.CHECKOV_FILE_SCAN_FINISHED -> {
                loadScanResultsPanel(panelType)
                project.service<AnalyticsService>().singleFileScanResultsWereFullyDisplayed()
            }

            PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED -> {
                loadFrameworkScanFinished(panelType)
            }

            PANELTYPE.CHECKOV_REPOSITORY_SCAN_FAILED -> {
                loadErrorsPanel()
            }

            PANELTYPE.AUTO_CHOOSE_PANEL -> {
                loadAutoChoosePanel()
                CheckovScanAction.resetActionDynamically(true)
            }
        }
        revalidate()
        if (panelType == PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED && project.service<FullScanStateService>().wereAllFrameworksFinished()) {
            project.service<AnalyticsService>().fullScanResultsWereFullyDisplayed()
        }
    }

    private fun loadScanResultsPanel(panelType: Int) {
        val checkovTree = CheckovToolWindowTree(project, mainPanelSplitter, checkovDescription)
        val filesTreePanel = checkovTree.createScroll()
        if (shouldDisplayNoErrorPanel(panelType)) {
            add(checkovDescription.noErrorsPanel())
        } else {
            val descriptionPanel = checkovDescription.emptyDescription()
            mainPanelSplitter.firstComponent = filesTreePanel
            mainPanelSplitter.secondComponent = descriptionPanel
            add(mainPanelSplitter)
            updateErrorsInFile()
        }
    }

    private fun shouldDisplayNoErrorPanel(panelType: Int): Boolean {
        return project.service<ResultsCacheService>().getAllCheckovResults().isEmpty() &&
                (panelType == PANELTYPE.CHECKOV_FILE_SCAN_FINISHED ||
                        (panelType == PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED && project.service<FullScanStateService>().wereAllFrameworksFinished()))
    }

    private fun loadAutoChoosePanel() {
        val setting = CheckovSettingsState().getInstance()
        when {
            setting?.accessKey.isNullOrEmpty() -> add(checkovDescription.configurationDescription())
            setting?.secretKey.isNullOrEmpty() -> add(checkovDescription.configurationDescription())
            else -> add(checkovDescription.preScanDescription())
        }
    }

    private fun loadErrorsPanel() {
        add(checkovDescription.failedScanDescription())
    }

    private fun loadPreviousStatePanel(panelType: Int) {
        when (project.service<FullScanStateService>().previousState) {
            FullScanStateService.State.FIRST_TIME_SCAN -> {
                loadAutoChoosePanel()
            }

            FullScanStateService.State.SUCCESSFUL_SCAN -> {
                loadScanResultsPanel(panelType)
            }

            FullScanStateService.State.FAILED_SCAN -> {
                loadErrorsPanel()
            }
        }
    }

    private fun loadFrameworkScanFinished(panelType: Int) {
        if (project.service<FullScanStateService>().wereAllFrameworksFinished()) {
            CheckovScanAction.resetActionDynamically(true)
            if (project.service<FullScanStateService>().onCancel) {
                loadPreviousStatePanel(panelType)
                return
            }
        }

        if (!project.service<FullScanStateService>().onCancel) {
            project.service<FullScanStateService>().isFrameworkResultsWereDisplayed = true
            loadScanResultsPanel(panelType)
        }
    }

    private fun loadTabsContent(panelType: Int) {
        if (project.service<FullScanStateService>().isFullScanRunning) {

            if (project.service<FullScanStateService>().onCancel) {
                loadPreviousStatePanel(panelType)
                return
            }

            if(!project.service<FullScanStateService>().isFrameworkResultsWereDisplayed) {
                add(checkovDescription.duringScanDescription("Scanning your repository..."))
                return
            }
        }

        if (project.service<FullScanStateService>().isFrameworkResultsWereDisplayed || project.service<AnalyticsService>().wereResultsDisplayed) {
            loadScanResultsPanel(panelType)
            return
        }

        loadAutoChoosePanel()
    }

    private fun reloadTabCounts() {
        val toolWindowManager = ToolWindowManager.getInstance(project)
        CheckovToolWindowFactory.internalExecution = true
        project.messageBus.syncPublisher(ToolWindowManagerListener.TOPIC).stateChanged(toolWindowManager)
    }

    fun subscribeToProjectEventChange() {
        if (SwingUtilities.isEventDispatchThread()) {
            project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
        } else {
            ApplicationManager.getApplication().invokeLater {
                project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
            }
        }

        // subscribe to open file events
        project.messageBus.connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object :
                FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file)
                if (shouldScanFile(file)) {
                    val checkovResults = project.service<ResultsCacheService>().checkovResults
                    val hasResources = CheckovResultsListUtils.getCheckovResultsByPath(checkovResults, toVirtualFilePath(project, file)).isNotEmpty()
                    if(!hasResources){
                        project.service<CheckovScanService>().scanFile(file.path, project)
                    }
                }
            }
            override fun selectionChanged(event: FileEditorManagerEvent) {
                super.selectionChanged(event)
                val file = event.newFile
                if(file != null) {
                    updateErrorsInline(file)
                }
            }
        })

        // subscribe to update file events
        project.messageBus.connect().subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.isEmpty()) {
                    return
                }

                LOG.debug("file event for file: ${events[0].file!!.path}. isValid: ${events[0].isValid}, isFromRefresh: ${events[0].isFromRefresh}, isFromSave: ${events[0].isFromSave}, requestor: ${events[0].requestor}")

                if (events.isEmpty() || !events[0].isFromSave || events[0].file == null) {
                    return
                }

                if (shouldScanFile(events[0].file!!)) {
                    project.service<CheckovScanService>().scanFile(events[0].file!!.path, project);
                }
            }
        })
    }

    fun shouldScanFile(virtualFile: VirtualFile): Boolean {
        if (!virtualFile.isValid) {
            return false
        }

        val virtualFilePath: String = toVirtualFilePath(project, virtualFile)

        val excludedPaths = (getGitIgnoreValues(project) + FULL_SCAN_EXCLUDED_PATHS).distinct()

        return ProjectRootManager.getInstance(project).fileIndex.isInContent(virtualFile) &&
                excludedPaths.find { excludedPath -> virtualFilePath.startsWith(excludedPath) }.isNullOrEmpty()
    }

    fun subscribeToInternalEvents(project: Project) {
        // Subscribe to Scanning Topic
        project.messageBus.connect(this)
                .subscribe(CheckovScanListener.SCAN_TOPIC, object : CheckovScanListener {

                    override fun projectScanningStarted() {
                        SeverityFilterActions.restartState()
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_REPOSITORY_SCAN_STARTED)
                    }

                    override fun scanningFinished(scanSourceType: CheckovScanService.ScanSourceType) {
                        ApplicationManager.getApplication().invokeLater {
                            SeverityFilterActions.onScanFinishedForDisplayingResults(project)
                            if (scanSourceType == CheckovScanService.ScanSourceType.FILE) {
                                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FILE_SCAN_FINISHED)
                            } else {
                                project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_FRAMEWORK_SCAN_FINISHED)
                            }
                        }
                    }

                    override fun fullScanFailed() {
                        ApplicationManager.getApplication().invokeLater {
                            project.service<CheckovToolWindowManagerPanel>().loadMainPanel(PANELTYPE.CHECKOV_REPOSITORY_SCAN_FAILED)
                        }
                    }
                })

        // Subscribe to Settings Topic
        project.messageBus.connect(this)
                .subscribe(CheckovSettingsListener.SETTINGS_TOPIC, object: CheckovSettingsListener {
                    override fun settingsUpdated() {
                        project.service<CheckovToolWindowManagerPanel>().loadMainPanel()
                    }
                })
    }

    private fun updateErrorsInFile(){
        val manager = FileEditorManager.getInstance(project)
        val openedFiles = manager.selectedFiles

        openedFiles.forEach { file->
            updateErrorsInline(file)
        }
    }

    private fun updateErrorsInline(file: VirtualFile) {
        val editor = FileEditorManager.getInstance(project).getSelectedEditor(file)
        val markup = (editor as TextEditor).editor.markupModel
        removeOldHighlighters(markup)
        val document = editor.editor.document

        val checkovResults: MutableList<BaseCheckovResult> = project.service<ResultsCacheService>().checkovResults
        val fileToResourceMap = CheckovResultsListUtils.sortAndGroupResultsByPath(checkovResults)
        val relativePath = file.path.replace(project.basePath.toString(), "")
        val fileInResults = fileToResourceMap.filter { it.key == relativePath }[relativePath]
        if (!fileInResults.isNullOrEmpty()) {
            val errorsPerLine = fileInResults.groupBy { it.fileLineRange[0] }
            errorsPerLine.forEach { (row, errorsPerLine) ->
                createIconForLineErrors(row, errorsPerLine, markup, document)
            }
        }
    }

    private fun createIconForLineErrors(firstRow: Int, results: List<BaseCheckovResult>, markup: MarkupModel, document: Document) {
        val rangeHighlighter: RangeHighlighter = markup.addLineHighlighter(firstRow, HighlighterLayer.ERROR, null)
        val iconLocation = if(firstRow >= document.lineCount) document.getLineStartOffset(firstRow) else document.getLineStartOffset(firstRow + 1)
        val gutterIconRenderer = CheckovGutterErrorIcon(results, iconLocation, markup, firstRow)
        rangeHighlighter.gutterIconRenderer = gutterIconRenderer
        rangeHighlighter.putUserData(key, true)
    }

    private fun removeOldHighlighters(markup: MarkupModel) {
        markup.allHighlighters.forEach { rangeHighlighter ->
            if(rangeHighlighter.isValid && rangeHighlighter.getUserData(key) == true) {
                markup.removeHighlighter(rangeHighlighter)
            }
        }
    }

    override fun dispose() {
        SeverityFilterActions.restartState()
    }
}
