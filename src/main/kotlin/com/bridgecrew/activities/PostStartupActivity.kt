package com.bridgecrew.activities
import com.intellij.openapi.startup.StartupActivity
import com.intellij.openapi.project.Project
import com.bridgecrew.CheckovRunnerTesting

import com.bridgecrew.services.CheckovServiceInstance
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
class PostStartupActivity : StartupActivity {

    override fun runActivity(project: Project) {
        this.installCheckovOnStartup()
        subscribeToListeners(project)
        println("startup finished")
    }

    private fun installCheckovOnStartup() {
        val checkov = CheckovServiceInstance
        checkov.installCheckov()
        println("[installCheckovOnStartup] Using checkov version ${checkov.getVersion()}")
    }

    private fun subscribeToListeners(project: Project) {
        val extensionList = listOf("tf","yaml", "yaml", "json")

        project.messageBus.connect(project).subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, object : FileEditorManagerListener {
            override fun fileOpened(source: FileEditorManager, file: VirtualFile) {
                super.fileOpened(source, file);
                if (extensionList.contains(file.extension)) {
                    // TODO: "getResultList will be changed with checkov run function.
                    CheckovRunnerTesting().getResultsList(0, project)
                }

            }
        })
        project.messageBus.connect(project).subscribe(VirtualFileManager.VFS_CHANGES, object : BulkFileListener {
            override fun after(events: MutableList<out VFileEvent>) {
                if (events.size > 0 && extensionList.contains(events.get(0).file?.extension )) {
                    // TODO: "getResultList will be changed with checkov run function.
                    CheckovRunnerTesting().getResultsList(2, project)
                }
            }
        })
    }
}