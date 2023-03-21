package com.bridgecrew.errors

import com.bridgecrew.services.scan.CheckovScanService
import com.bridgecrew.ui.CheckovNotificationBalloon
import com.bridgecrew.utils.ERROR_LOG_DIR_PATH
import com.bridgecrew.utils.extractFileNameFromPath
import com.intellij.notification.NotificationType
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.json.JSONObject
import java.io.FileWriter
import java.io.PrintWriter
import java.nio.charset.Charset
import java.nio.file.Files
import kotlin.io.path.Path
import com.bridgecrew.analytics.AnalyticsService
import com.bridgecrew.services.scan.ScanTaskResult
import com.intellij.openapi.components.service

@Service
class CheckovErrorHandlerService(val project: Project) {
    private val LOG = logger<CheckovErrorHandlerService>()

    private fun saveErrorResultToFile(scanTaskResult: ScanTaskResult, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        val errorMessage = if (error.message != null) {
            "Error while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, original error message - ${error.message}"
        } else "Error while scanning $dataSourceValue"

        LOG.error("${errorMessage}. Please check the log file in ${scanTaskResult.debugOutput.path}. Checkov result can be found in ${scanTaskResult.checkovResult.path}. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")

        CheckovNotificationBalloon.showNotification(project, "Error while running Checkov scan on ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, please check the debug log file in ${scanTaskResult.debugOutput.path}, Checkov result can be found in ${scanTaskResult.checkovResult.path}. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues", NotificationType.ERROR)
    }

    private fun saveParsingErrorResultToFile(scanTaskResult: ScanTaskResult, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {
        var errorFilePath = ""

        LOG.error("Error while parsing result while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")} for files $failedFiles" +
                "Please check the log file in ${scanTaskResult.debugOutput.path}. Checkov result can be found in ${scanTaskResult.checkovResult.path}. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")

        CheckovNotificationBalloon.showNotification(project, "Parsing error while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues", NotificationType.ERROR)
    }

    fun scanningParsingError(scanTaskResult: ScanTaskResult, source: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveParsingErrorResultToFile(scanTaskResult, "file_path", source, extractFileNameFromPath(source), failedFiles, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveParsingErrorResultToFile(scanTaskResult, "framework", source, source, failedFiles, scanSourceType)
                project.service<AnalyticsService>().fullScanFrameworkError(source)
            }
        }

    }

    fun scanningError(scanTaskResult: ScanTaskResult, source: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveErrorResultToFile(scanTaskResult, "file_path", source, extractFileNameFromPath(source), error, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveErrorResultToFile(scanTaskResult, "framework", source, source, error, scanSourceType)
                project.service<AnalyticsService>().fullScanFrameworkError(source)

            }
        }

    }

}