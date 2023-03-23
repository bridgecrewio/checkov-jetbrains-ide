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
import com.bridgecrew.services.scan.FullScanStateService
import com.bridgecrew.services.scan.ScanTaskResult
import com.intellij.openapi.components.service

@Service
class CheckovErrorHandlerService(val project: Project) {
    private val LOG = logger<CheckovErrorHandlerService>()

    private fun saveErrorResultToFile(scanTaskResult: ScanTaskResult, dataSourceValue: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        var errorMessagePrefix = if (error.message != null) {
            "Error while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, original error message - ${error.message}"
        } else "Error while scanning $dataSourceValue"

        val errorMessage = "${errorMessagePrefix}.\n " +
                "Please check the log file in ${scanTaskResult.debugOutput.path}.\n" +
                "Checkov result can be found in ${scanTaskResult.checkovResult.path}.\n" +
                "To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n"

        LOG.warn(errorMessage)

        CheckovNotificationBalloon.showNotification(project,
                errorMessage,
                NotificationType.ERROR)
    }

    private fun saveParsingErrorResultToFile(scanTaskResult: ScanTaskResult, dataSourceValue: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {
        val errorMessage = "Error while parsing result while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")} for files ${failedFiles.joinToString { "," }}\n" +
                "Please check the log file in ${scanTaskResult.debugOutput.path}.\n Checkov result can be found in ${scanTaskResult.checkovResult.path}.\n"

        LOG.warn(errorMessage)

        CheckovNotificationBalloon.showNotification(project,
                errorMessage,
                NotificationType.ERROR)
    }

    fun scanningParsingError(scanTaskResult: ScanTaskResult, source: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveParsingErrorResultToFile(scanTaskResult, source, failedFiles, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveParsingErrorResultToFile(scanTaskResult, source, failedFiles, scanSourceType)
                project.service<FullScanStateService>().parsingErrorsFoundInFiles(source, failedFiles)
            }
        }

    }

    fun scanningError(scanTaskResult: ScanTaskResult, source: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveErrorResultToFile(scanTaskResult, source, error, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveErrorResultToFile(scanTaskResult, source, error, scanSourceType)
                project.service<FullScanStateService>().frameworkFinishedWithErrors(source, scanTaskResult)
            }
        }

    }

}