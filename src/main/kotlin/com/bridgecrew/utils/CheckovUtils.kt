package com.bridgecrew.utils

import com.bridgecrew.*
import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.CheckType
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger
import org.json.JSONArray
import org.json.JSONObject


data class CheckovResultExtractionData(
        val failedChecks: List<CheckovResult> = arrayListOf(),
        val parsingErrorsSize: Int = 0,
        val passedChecksSize: Int = 0
)

class CheckovUtils {
    companion object {
        private val LOG = logger<CheckovUtils>()
        fun isCustomPolicy(result: BaseCheckovResult): Boolean {
            return (result.category == Category.IAC || result.category == Category.SECRETS) && !result.id.startsWith("CKV")
        }
        fun extractResource(result: CheckovResult, category: Category, checkType: CheckType) : String {
            if (category == Category.VULNERABILITIES && checkType == CheckType.SCA_PACKAGE) {
                return extractVulnerabilityResource(result)
            }

            if (category == Category.VULNERABILITIES || category == Category.LICENSES) {
                return result.vulnerability_details?.package_name ?: throw Exception("null resource, category is ${category.name}, result is $result")
            }

            return result.resource
        }

        private fun extractVulnerabilityResource(result: CheckovResult): String {
            val vulnerabilityDetails: VulnerabilityDetails = result.vulnerability_details ?: return result.resource

            var vulnerabilityResource = generateVulnerabilityResourceByPackage(vulnerabilityDetails.root_package_name, vulnerabilityDetails.root_package_version)

            if (vulnerabilityResource != null)
                return vulnerabilityResource

            vulnerabilityResource = generateVulnerabilityResourceByPackage(vulnerabilityDetails.package_name, vulnerabilityDetails.package_version)

            if (vulnerabilityResource != null)
                return vulnerabilityResource

            return result.resource
        }

        private fun generateVulnerabilityResourceByPackage(vulnerablePackage: String?, vulnerablePackageVersion: String?): String? {
            var resource: String? = null
            if (vulnerablePackage != null)
                resource = vulnerablePackage
            if (vulnerablePackageVersion != null)
                resource += ":$vulnerablePackageVersion"

            return resource
        }

        fun extractFailedChecksAndParsingErrorsFromCheckovResult(rawResult: String, scanningSource: String): CheckovResultExtractionData {
            if (rawResult.isEmpty()) {
                return CheckovResultExtractionData(arrayListOf(), 0, 0)
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
                    var parsingErrors = 0
                    var passedChecks = 0

                    for (resultItem in resultsArray) {
                        val extractionResult = extractFailedChecksAndParsingErrorsFromObj(resultItem as JSONObject)
                        failedChecks.addAll(extractionResult.failedChecks)
                        parsingErrors += extractionResult.parsingErrorsSize
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
            val parsingErrors: Int = extractParsingErrors(summary)
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

        private fun extractParsingErrors(summary: JSONObject): Int {
            try {
                return summary.getInt("parsing_errors")
            } catch (e: Exception) {
                LOG.error("Error while extracting parsing errors", e)
            }

            return 0
        }
    }
}