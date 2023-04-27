package com.bridgecrew.services.checkovScanCommandsService

import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.settings.CheckovSettingsState
import com.bridgecrew.utils.*
import com.intellij.openapi.project.Project
import org.apache.commons.lang.StringUtils

abstract class CheckovScanCommandsService(val project: Project) {
    protected val settings = CheckovSettingsState().getInstance()
    var gitRepo = getRepoName()

    fun getExecCommandForSingleFile(filePath: String, outputFilePath: String): ArrayList<String> {
        val cmds = ArrayList<String>()
        cmds.addAll(getCheckovRunningCommandByServiceType())
        cmds.addAll(getCheckovCliArgsForExecCommand(outputFilePath))

        cmds.add("-f")
        cmds.add(getFilePath(filePath))
        return cmds
    }

    fun getExecCommandsForRepositoryByFramework(framework: String, outputFilePath: String): ArrayList<String> {

        val baseCmds = ArrayList<String>()
        baseCmds.addAll(getCheckovRunningCommandByServiceType())

        baseCmds.add("-d")
        baseCmds.add(getDirectory())

        baseCmds.addAll(getExcludePathCommand())

        val cmdByFramework = arrayListOf<String>()
        cmdByFramework.addAll(baseCmds)
        cmdByFramework.addAll(getCheckovCliArgsForExecCommand(outputFilePath))
        cmdByFramework.add("--framework")
        cmdByFramework.add(framework)

        return cmdByFramework
    }

    private fun getCheckovCliArgsForExecCommand(outputFilePath: String): ArrayList<String> {
        val apiToken = settings?.getApiKey()
        if (apiToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
            throw Exception("Wasn't able to get api token\n" +
                    "Please insert an Api Token to continue")
        }

        return arrayListOf("-s", "--bc-api-key", apiToken, "--repo-id", gitRepo, "--quiet", "-o", "cli", "-o", "json",
                "--output-file-path", "console,$outputFilePath")
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