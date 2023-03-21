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
import com.intellij.openapi.components.service

@Service
class CheckovErrorHandlerService(val project: Project) {
    private val LOG = logger<CheckovErrorHandlerService>()

    private fun saveErrorResultToFile(result: String, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        val errorMessage = if (error.message != null) {
            "Error while scanning $dataSourceValue, original error message - ${error.message}"
        } else "Error while scanning $dataSourceValue"

        var errorFilePath = ""
        try {
            val json = JSONObject()

            json.put(dataSourceKey, dataSourceValue)
            json.put("error", error)
            json.put("error_message", error.message)
            json.put("checkov_result", result)

            val errorFileDirectoryPath = "${project.basePath!!}/${ERROR_LOG_DIR_PATH}/${dataSourceDirName}"
            Files.createDirectories(Path(errorFileDirectoryPath))

            errorFilePath = "$errorFileDirectoryPath/${System.currentTimeMillis()}.json"

            PrintWriter(FileWriter(errorFilePath, Charset.defaultCharset()))
                    .use { it.write(json.toString()) }
            LOG.error("${errorMessage}\n, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
        } catch (e: Exception) {
            logCheckovResult(result, "${errorMessage}\n, error reason - ${e.message}.")
            e.printStackTrace()
        }

        CheckovNotificationBalloon.showNotification(project, "Error while running Checkov scan on ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues", NotificationType.ERROR)

    }

    private fun saveParsingErrorResultToFile(result: String, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {
        val json = JSONObject()
        var errorFilePath = ""
        try {
            json.put(dataSourceKey, dataSourceValue)
            json.put("failed_files", failedFiles)
            json.put("checkov_result", result)


            val errorFileDirectoryPath = "${project.basePath!!}/${ERROR_LOG_DIR_PATH}/${dataSourceDirName}/parsingErrors"
            Files.createDirectories(Path(errorFileDirectoryPath))

            errorFilePath = "${errorFileDirectoryPath}/${System.currentTimeMillis()}.json"

            PrintWriter(FileWriter(errorFilePath, Charset.defaultCharset()))
                    .use { it.write(json.toString()) }
            LOG.error("Error while parsing result while scanning ${dataSourceValue.replace(project.basePath!!, "")} for files ${failedFiles}, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")

        } catch (e: Exception) {
            logCheckovResult(result, "Error while parsing result for ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")} - error files ${failedFiles}. Failed files - ${failedFiles}.")
            e.printStackTrace()
        }

        CheckovNotificationBalloon.showNotification(project, "Parsing error while scanning ${scanSourceType.toString().lowercase()} ${dataSourceValue.replace(project.basePath!!, "")}, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues", NotificationType.ERROR)
    }

    fun scanningParsingError(result: String, source: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveParsingErrorResultToFile(result, "file_path", source, extractFileNameFromPath(source), failedFiles, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveParsingErrorResultToFile(result, "framework", source, source, failedFiles, scanSourceType)
                project.service<AnalyticsService>().fullScanFrameworkError(source)
            }
        }

    }

    fun scanningError(result: String, source: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                saveErrorResultToFile(result, "file_path", source, extractFileNameFromPath(source), error, scanSourceType)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                saveErrorResultToFile(result, "framework", source, source, error, scanSourceType)
                project.service<AnalyticsService>().fullScanFrameworkError(source)

            }
        }

    }

    private fun logCheckovResult(result: String, errorMessage: String) {
        LOG.error("$errorMessage\n. Checkov result: \n" +
                "------------------------------------------\n" +
                result.substring((result.length - 1000).coerceAtLeast(0)) +
                "\n------------------------------------------\n\n")
    }

}