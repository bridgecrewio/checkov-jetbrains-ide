//package com.bridgecrew.services.checkovService
//
//import com.intellij.ide.plugins.PluginManagerCore
//import com.intellij.openapi.diagnostic.logger
//import com.intellij.openapi.extensions.PluginId
//import com.intellij.openapi.project.Project
//import org.apache.commons.io.FilenameUtils
//import java.nio.file.Paths
//
//private val LOG = logger<DockerCheckovService>()
//
//const val DOCKER_MOUNT_DIR = "/checkovScan"
//
//class DockerCheckovService(project: Project) : CheckovService(project) {
//
//    val image = "bridgecrew/checkov"
//    val volumeDirectory = "checkovScan"
//    override fun getInstallCommand(): ArrayList<String> {
//        val cmds = arrayListOf("docker", "pull", "bridgecrew/checkov")
//        return cmds
//    }
//
////    override fun getExecCommand(filePath: String, apiToken: String, gitRepoName: String, prismaUrl: String?): ArrayList<String> {
////        val pluginVersion =
////                PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"
////
////        val fileName = Paths.get(filePath).fileName.toString()
////        val image = "bridgecrew/checkov"
////        val fileDir = "$filePath:/checkovScan/$fileName"
////        val dockerCommand = arrayListOf("docker", "run", "--rm", "--tty", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
////        if (!prismaUrl.isNullOrEmpty()) {
////            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
////        }
////        dockerCommand.addAll(arrayListOf("--volume", fileDir, image))
////        val checkovCommand = arrayListOf("-d", "/checkovScan", "-s", "--bc-api-key", apiToken, "--repo-id", gitRepoName, "-o", "json")
////        val cmds = ArrayList<String>()
////        cmds.addAll(dockerCommand)
////        cmds.addAll(checkovCommand)
////        return cmds
////    }
//
//    override fun getVersion(project: Project): ArrayList<String> {
//        val cmds = arrayListOf("docker", "run", "--rm", "--tty", "bridgecrew/checkov", "-v")
//        return cmds
//    }
//
//    //docker run --rm --tty --env LOG_LEVEL=DEBUG --volume /Users/mshavit/source/platform:/platform bridgecrew/checkov -f platform/src/microStacks/alertsValidationStack/main.tf -s --bc-api-key '3789f913-f1bb-4da3-9990-70025039932d' -o json
//
//    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
//        val pluginVersion =
//        PluginManagerCore.getPlugin(PluginId.getId("com.github.bridgecrewio.checkov"))?.version ?: "UNKNOWN"
//
//        val volumeDir = "${FilenameUtils.separatorsToUnix(project.basePath!!)}:/${volumeDirectory}"
//        val dockerCommand = arrayListOf("docker", "run", "--rm", "--tty", "--env", "BC_SOURCE=jetbrains", "--env", "BC_SOURCE_VERSION=$pluginVersion", "--env", "LOG_LEVEL=DEBUG")
//        val prismaUrl = settings?.prismaURL
//        if (!prismaUrl.isNullOrEmpty()) {
//            dockerCommand.addAll(arrayListOf("--env", "PRISMA_API_URL=${prismaUrl}"))
//        }
//        dockerCommand.addAll(arrayListOf("--volume", volumeDir, image))
//        return dockerCommand
//
//    }
//
//    override fun getDirectory(): String {
//        return volumeDirectory
//    }
//
//}