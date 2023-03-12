package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.utils.FULL_SCAN_EXCLUDED_PATHS
import com.bridgecrew.utils.FULL_SCAN_FRAMEWORKS
import com.bridgecrew.utils.getGitIgnoreValues
import com.bridgecrew.utils.getRepoName
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils

abstract class CheckovScanCommandsService(val project: Project) {
    protected val settings = CheckovSettingsState().getInstance()
    var gitRepo = getRepoName()

    fun getExecCommandForSingleFile(filePath: String): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.addAll(getCheckovRunningCommandByServiceType())
        cmds.addAll(getCheckovCliArgsForExecCommand())

        cmds.add("-f")
        cmds.add(getFilePath(filePath))
        return cmds
    }

    fun getExecCommandsForRepositoryByFramework(): ArrayList<ArrayList<String>> {
        val directoryByFrameworkCommands = arrayListOf<ArrayList<String>>()

        val baseCmds = ArrayList<String>()
        baseCmds.addAll(getCheckovRunningCommandByServiceType())
        baseCmds.addAll(getCheckovCliArgsForExecCommand())

        baseCmds.add("-d")
        baseCmds.add(getDirectory())

        baseCmds.addAll(getExcludePathCommand())

        for (framework in FULL_SCAN_FRAMEWORKS) {
            val cmdByFramework = arrayListOf<String>()
            cmdByFramework.addAll(baseCmds)
            cmdByFramework.add("--framework")
            cmdByFramework.add(framework)
            directoryByFrameworkCommands.add(cmdByFramework)
        }

        return directoryByFrameworkCommands
    }

    private fun getCheckovCliArgsForExecCommand(): ArrayList<String> {
        val apiToken = settings?.apiToken
        if (apiToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
            throw Exception("Wasn't able to get api token\n" +
                    "Please insert an Api Token to continue")
        }

        return arrayListOf("-s", "--bc-api-key", apiToken, "--repo-id", gitRepo, "-o", "json") //, "--quiet" )
    }

    private fun getExcludePathCommand(): ArrayList<String> {
        val cmds = ArrayList<String>()

        val excludedPaths = (getGitIgnoreValues(project) + FULL_SCAN_EXCLUDED_PATHS).distinct()

        for (excludePath in excludedPaths) {
            cmds.add("--skip-path")
            cmds.add(getNormalizedExcludePath(excludePath))
        }

        return cmds
    }

    private fun getNormalizedExcludePath(excludePath: String): String {
        if (System.getProperty("os.name").lowercase().contains("win")) {
            return StringUtils.removeEnd(excludePath, "\\")
        }

        return StringUtils.removeEnd(excludePath, "/")
    }

    abstract fun getCheckovRunningCommandByServiceType(): ArrayList<String>
    abstract fun getDirectory(): String
    abstract fun getFilePath(originalFilePath: String): String
}