package com.bridgecrew.utils

import com.bridgecrew.*
import com.bridgecrew.results.BaseCheckovResult
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger
import org.json.JSONArray
import org.json.JSONObject


data class CheckovResultExtractionData(
        val failedChecks: List<CheckovResult> = arrayListOf(),
        val parsingErrors: List<String> = arrayListOf(),
        val passedChecksSize: Int = 0
)

class CheckovUtils {
    companion object {
        private val LOG = logger<CheckovUtils>()
        fun isCustomPolicy(result: BaseCheckovResult): Boolean {
            return !result.id.startsWith("CKV")
        }

        fun extractFailedChecksAndParsingErrorsFromCheckovResult(rawResult: String, scanningSource: String): CheckovResultExtractionData {
            if (rawResult.isEmpty()) {
                return CheckovResultExtractionData(arrayListOf(), arrayListOf(), 0)
            }

            LOG.info("found checkov result for source $scanningSource")
            val checkovResult = rawResult.replace("\u001B[0m", "")

            return when (checkovResult[0]) {
                '{' -> {
                    return extractFailedChecksAndParsingErrorsFromObj(JSONObject(checkovResult))
                }

                '[' -> {
                    val resultsArray = JSONArray(checkovResult)

                    val failedChecks = arrayListOf<CheckovResult>()
                    val parsingErrors = arrayListOf<String>()
                    var passedChecks = 0

                    for (resultItem in resultsArray) {
                        val extractionResult = extractFailedChecksAndParsingErrorsFromObj(resultItem as JSONObject)
                        failedChecks.addAll(extractionResult.failedChecks)
                        parsingErrors.addAll(extractionResult.parsingErrors)
                        passedChecks += extractionResult.passedChecksSize
                    }
                    CheckovResultExtractionData(failedChecks, parsingErrors, passedChecks)
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
            val passedChecks: Int = summary.getInt("passed")

            return CheckovResultExtractionData(failedChecks, parsingErrors, passedChecks)
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

                    return listOf() // TODO - after getting the correct fields from checkov - gson.fromJson(results.getJSONArray("parsing_errors").toString(), parsingErrorsList)
                }
            } catch (e: Exception) {
                LOG.error("Error while extracting parsing errors", e)
            }

            return listOf()
        }
    }
}