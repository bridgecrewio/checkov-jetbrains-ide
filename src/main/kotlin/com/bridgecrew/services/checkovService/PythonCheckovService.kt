//package com.bridgecrew.services.checkovService
//
//import CliService
//import com.intellij.openapi.components.service
//import com.intellij.openapi.project.Project
//import org.apache.commons.io.FilenameUtils
//
//abstract class PythonCheckovService(project: Project): CheckovService(project) {
//    abstract override fun getInstallCommand(): ArrayList<String>
//
//    override fun getVersion(project: Project): ArrayList<String> {
//        return arrayListOf(project.service<CliService>().checkovPath, "-v")
//    }
//
//    override fun getCheckovRunningCommandByServiceType(): ArrayList<String> {
//        return arrayListOf(project.service<CliService>().checkovPath)
//    }
//
//    override fun getDirectory(): String {
//        return FilenameUtils.separatorsToSystem(project.basePath!!)
//    }
//}