package com.bridgecrew.services.checkovService

import com.bridgecrew.listeners.CheckovSettingsListener
import com.bridgecrew.services.CheckovScanService
import com.bridgecrew.services.checkovService.CheckovCliFlagsConfig.Companion.frameworks
import com.bridgecrew.services.checkovService.CheckovCliFlagsConfig.Companion.excludedPaths
import com.bridgecrew.settings.CheckovSettingsState
//import com.bridgecrew.utils.defaultRepoName
import com.bridgecrew.utils.getRepoName
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.apache.commons.io.FilenameUtils
import java.nio.file.Paths

private val LOG = logger<CheckovScanService>()
abstract class CheckovService(val project: Project) {

    protected val settings = CheckovSettingsState().getInstance()
    var gitRepo = getRepoName() //TODO - get from GitUtils

    abstract fun getInstallCommand(): ArrayList<String>
//    abstract fun getExecCommand(filePath: String): ArrayList<String>
    abstract fun getVersion(project: Project): ArrayList<String>
    fun getExecCommandForRepository(): ArrayList<String> {
        return arrayListOf()
    }

    fun getExecCommandForSingleFile(filePath: String): ArrayList<String> {
        //docker run --rm --tty --env LOG_LEVEL=DEBUG --volume /Users/mshavit/source/platform:/platform bridgecrew/checkov -f platform/src/microStacks/alertsValidationStack/main.tf -s --bc-api-key '3789f913-f1bb-4da3-9990-70025039932d' -o json
        val cmds = ArrayList<String>()
        cmds.addAll(getCheckovRunningCommandByServiceType())
        cmds.addAll(getCheckovCliArgsForExecCommand())

        cmds.add("-f")
        cmds.add(FilenameUtils.separatorsToSystem(filePath))
        return cmds
    }

    fun getExecCommandsForRepositoryByFramework(): ArrayList<ArrayList<String>> {
        //docker run --rm --tty --env LOG_LEVEL=DEBUG --volume /Users/mshavit/source/platform:/platform bridgecrew/checkov -f platform/src/microStacks/alertsValidationStack/main.tf -s --bc-api-key '3789f913-f1bb-4da3-9990-70025039932d' -o json
        val directoryByFrameworkCommands = arrayListOf<ArrayList<String>>()

        val baseCmds = ArrayList<String>()
        baseCmds.addAll(getCheckovRunningCommandByServiceType())
        baseCmds.addAll(getCheckovCliArgsForExecCommand())

        baseCmds.add("-d")
        baseCmds.add(getDirectory())

        for (excludePath in excludedPaths) {
            baseCmds.add("--skip-path")
            baseCmds.add(excludePath)
        }

        for (framework in frameworks) {
            val cmdByFramework = arrayListOf<String>()
            cmdByFramework.addAll(baseCmds)
            cmdByFramework.add("--framework")
            cmdByFramework.add(framework)
            directoryByFrameworkCommands.add(cmdByFramework)
        }

        return directoryByFrameworkCommands
    }

    protected fun getCheckovCliArgsForExecCommand(): ArrayList<String> {
        val apiToken = settings?.apiToken
        if (apiToken.isNullOrEmpty()) {
            project.messageBus.syncPublisher(CheckovSettingsListener.SETTINGS_TOPIC).settingsUpdated()
            throw Exception("Wasn't able to get api token\n" +
                    "Please insert an Api Token to continue")
        }

//        val gitRepoName = defaultRepoName
//        var gitRepo = getRepoName()
        return arrayListOf("-s", "--bc-api-key", apiToken, "--repo-id", gitRepo, "-o", "json")
    }

    abstract fun getCheckovRunningCommandByServiceType(): ArrayList<String>
    abstract fun getDirectory(): String
}