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


@Service
class CheckovErrorHandlerService(val project: Project) {
    private val LOG = logger<CheckovErrorHandlerService>()
    private fun saveErrorResultToFile(result: String, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, error: Exception) {

        val errorMessage = if (error.message != null) {
            "Error while scanning $dataSourceValue, original error message - ${error.message}"
        } else "Error while scanning $dataSourceValue"

        try {
            val json = JSONObject()

            json.put(dataSourceKey, dataSourceValue)
            json.put("error", error)
            json.put("error_message", error.message)
            json.put("checkov_result", result)

            val errorFileDirectoryPath = "${project.basePath!!}/${ERROR_LOG_DIR_PATH}/${dataSourceDirName}"
            Files.createDirectories(Path(errorFileDirectoryPath))

            val errorFilePath = "$errorFileDirectoryPath/${System.currentTimeMillis()}.json"

            PrintWriter(FileWriter(errorFilePath, Charset.defaultCharset()))
                    .use { it.write(json.toString()) }
            LOG.error("${errorMessage}\n, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")
        } catch (e: Exception) {
            logCheckovResult(result, "${errorMessage}\n, error reason - ${e.message}.")
            e.printStackTrace()
        }
    }

    private fun saveParsingErrorResultToFile(result: String, dataSourceKey: String, dataSourceValue: String, dataSourceDirName: String, failedFiles: List<String>) {
        val json = JSONObject()

        try {
            json.put(dataSourceKey, dataSourceValue)
            json.put("failed_files", failedFiles)
            json.put("checkov_result", result)


            val errorFileDirectoryPath = "${project.basePath!!}/${ERROR_LOG_DIR_PATH}/${dataSourceDirName}/parsingErrors"
            Files.createDirectories(Path(errorFileDirectoryPath))

            val errorFilePath = "${errorFileDirectoryPath}/${System.currentTimeMillis()}.json"

            PrintWriter(FileWriter(errorFilePath, Charset.defaultCharset()))
                    .use { it.write(json.toString()) }
            LOG.error("Error while parsing result while scanning ${dataSourceValue.replace(project.basePath!!, "")} for files ${failedFiles}, please check the log file in $errorFilePath. To report: open a issue at https://github.com/bridgecrewio/checkov-jetbrains-ide/issues\n\n")

        } catch (e: Exception) {
            logCheckovResult(result, "Error while parsing result for file ${dataSourceValue.replace(project.basePath!!, "")} for files ${failedFiles}. Failed files - ${failedFiles}.")
            e.printStackTrace()
        }
    }

    fun scanningParsingError(result: String, source: String, failedFiles: List<String>, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                scanningFileParsingError(result, source, failedFiles)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                scanningFrameworkParsingError(result, source, failedFiles)
            }
        }

    }

    private fun logCheckovResult(result: String, errorMessage: String) {
        LOG.error("$errorMessage\n. Checkov result: \n" +
                "------------------------------------------\n" +
                result.substring((result.length - 1000).coerceAtLeast(0)) +
                "\n------------------------------------------\n\n")
    }

    fun scanningError(result: String, source: String, error: Exception, scanSourceType: CheckovScanService.ScanSourceType) {

        when (scanSourceType) {
            CheckovScanService.ScanSourceType.FILE -> {
                scanningFileError(result, source, error)
            }

            CheckovScanService.ScanSourceType.FRAMEWORK -> {
                scanningFrameworkError(result, source, error)
            }
        }

    }

    private fun scanningFileParsingError(result: String, filePath: String, failedFiles: List<String>) {
        saveParsingErrorResultToFile(result, "file_path", filePath, extractFileNameFromPath(filePath), failedFiles)
        CheckovNotificationBalloon.showNotification(project, "Parsing error while scanning file ${filePath.replace(project.basePath!!, "")} due to parsing errors, please check the logs for further action", NotificationType.ERROR)
    }

    private fun scanningFrameworkParsingError(result: String, framework: String, failedFiles: List<String>) {
        saveParsingErrorResultToFile(result, "framework", framework, framework, failedFiles)
        CheckovNotificationBalloon.showNotification(project, "Parsing error while scanning framework $framework due to parsing errors, please check the logs for further action", NotificationType.ERROR)
    }

    private fun scanningFileError(result: String, filePath: String, error: Exception) {
        saveErrorResultToFile(result, "file_path", filePath, extractFileNameFromPath(filePath), error)
        CheckovNotificationBalloon.showNotification(project, "Error while running Checkov scan on file ${filePath.replace(project.basePath!!, "")}, please check the logs for further action", NotificationType.ERROR)
    }

    private fun scanningFrameworkError(result: String, framework: String, error: Exception) {
        saveErrorResultToFile(result, "framework", framework, framework, error)
        CheckovNotificationBalloon.showNotification(project, "Error while running Checkov full repository scan on framework $framework, please check the logs for further action", NotificationType.ERROR)
    }

}