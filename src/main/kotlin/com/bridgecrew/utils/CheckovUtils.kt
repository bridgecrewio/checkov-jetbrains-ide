package com.bridgecrew.utils

import com.bridgecrew.*
import com.bridgecrew.results.BaseCheckovResult
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger
import org.json.JSONArray
import org.json.JSONObject


data class CheckovResultExtractionData(
        val failedChecks: List<CheckovResult> = arrayListOf(),
        val parsingErrors: List<String> = arrayListOf()
)

class CheckovUtils {
    companion object {
        private val LOG = logger<CheckovUtils>()
        fun isCustomPolicy(result: BaseCheckovResult): Boolean {
            return !result.id.startsWith("CKV")
        }

        fun extractFailedChecksAndParsingErrorsFromCheckovResult(rawResult: String, scanningSource: String): CheckovResultExtractionData {
            if (rawResult.isEmpty()) {
                return CheckovResultExtractionData(arrayListOf(), arrayListOf())

            }
            var checkovResult = "checkovResult"
            val outputListOfLines = rawResult.split("\n").map { it.trim() }
            for (i in outputListOfLines.indices) {
                // filter lines that can appear in the Python version output, like '[GCC 10.2.1 20210110]'
                if (!outputListOfLines[i].startsWith('{') && !outputListOfLines[i].startsWith('[') ||
                        (outputListOfLines[i].startsWith("[") && outputListOfLines[i].endsWith("]"))) {
                    continue
                }
                checkovResult = outputListOfLines.subList(i, outputListOfLines.size - 1).joinToString("\n")
                break
            }

            LOG.info("found checkov result for source $scanningSource") // - $checkovResult")
            checkovResult = checkovResult.replace("\u001B[0m", "")

            return when (checkovResult[0]) {
                '{' -> {
                    return extractFailedChecksAndParsingErrorsFromObj(JSONObject(checkovResult))
                }

                '[' -> {
                    val resultsArray = JSONArray(checkovResult)

                    val failedChecks = arrayListOf<CheckovResult>()
                    val parsingErrors = arrayListOf<String>()

                    for (resultItem in resultsArray) {
                        val extractionResult = extractFailedChecksAndParsingErrorsFromObj(resultItem as JSONObject)
                        failedChecks.addAll(extractionResult.failedChecks)
                        parsingErrors.addAll(extractionResult.parsingErrors)
                    }
                    CheckovResultExtractionData(failedChecks, parsingErrors)
                }

                else -> throw Exception("couldn't parse checkov results output, reason: $rawResult")
            }
        }

        private fun extractFailedChecksAndParsingErrorsFromObj(outputObj: JSONObject): CheckovResultExtractionData {
            if (outputObj.has("failed") && outputObj.get("failed") == 0) {
                return CheckovResultExtractionData()
            }

            val summary = outputObj.getJSONObject("summary")
            val results = outputObj.getJSONObject("results")

            val failedChecks: List<CheckovResult> = extractFailedChecks(summary, results, outputObj)
            val parsingErrors: List<String> = extractParsingErrors(summary, results)

            return CheckovResultExtractionData(failedChecks, parsingErrors)

        }

        private fun extractFailedChecks(summary: JSONObject, results: JSONObject, outputObj: JSONObject): List<CheckovResult> {
            val failedSummary = summary.get("failed")

            if (failedSummary == 0) {
                return arrayListOf()
            }
            val failedChecks = results.getJSONArray("failed_checks")
            val resultsList = object : TypeToken<List<CheckovResult>>() {}.type
            val checkType = outputObj.getString("check_type")

            val checkovResults: ArrayList<CheckovResult> = gson.fromJson(failedChecks.toString(), resultsList)
            checkovResults.forEach { result -> result.check_type = checkType }

            return checkovResults
        }

        private fun extractParsingErrors(summary: JSONObject, results: JSONObject): List<String> {
            try {
                val parsingErrorSummary: Int = summary.getInt("parsing_errors")
                if (parsingErrorSummary > 0) {
                    val parsingErrorsList = object : TypeToken<List<String>>() {}.type

                    return gson.fromJson(results.getJSONArray("parsing_errors").toString(), parsingErrorsList)
                }
            } catch (e: Exception) {
                LOG.error("Error while extracting parsing errors", e)
            }

            return listOf()
        }
    }
}