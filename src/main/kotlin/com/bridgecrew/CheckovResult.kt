package com.bridgecrew
//import com.bridgecrew.services.scan.CheckovResultException
//import com.bridgecrew.services.scan.CheckovResultParsingException
import com.bridgecrew.utils.normalizeFilePathToAbsolute
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONObject

val gson = Gson()

private val LOG = logger<VulnerabilityDetails>()
data class VulnerabilityDetails(
        val id: String?,
        val package_name: String?,
        val package_version: String?,
        val link: String?,
        val description: String?,
        val licenses: String?,
        val cvss: Double?,
        val lowest_fixed_version: String?,
        val published_date: String?,
        val vector: String?,
        val risk_factors: List<String>?
)
data class CheckovResult(
        val check_id: String,
        val bc_check_id: String = "",
        val check_name: String,
        val file_path: String,
        val repo_file_path: String,
        var file_abs_path: String,
        val file_line_range: ArrayList<Int>,
        val resource: String,
        val severity: String,
        val description: String,
        val short_description: String,
        val vulnerability_details: VulnerabilityDetails?,
        val guideline: String = "\"No Guide\")",
        val code_block: List<List<Object>>,
        var check_type: String,
        val fixed_definition: String = ""
    )

//typealias ResourceToCheckovResultsList = MutableMap<String, ArrayList<CheckovResult>>
//
//fun extractFailesCheckAndParsingErrorsFromCheckovResult(raw: String, framework: String = ""): ArrayList<CheckovResult> {
//    if (raw.isEmpty()){
//        return arrayListOf()
//
////        throw CheckovResultException("Checkov result returned empty")
//    }
//    var checkovResult = "checkovResult"
//    val outputListOfLines = raw.split("\n").map { it.trim() }
//    for (i in outputListOfLines.indices) {
//        // filter lines that can appear in the Python version output, like '[GCC 10.2.1 20210110]'
//        if (!outputListOfLines[i].startsWith('{') && !outputListOfLines[i].startsWith('[') ||
//                (outputListOfLines[i].startsWith("[") && outputListOfLines[i].endsWith("]"))){
//            continue
//        }
//        checkovResult = outputListOfLines.subList(i,outputListOfLines.size-1).joinToString("\n")
//        break
//    }
//
//    LOG.info("found checkov result for framework $framework") // - $checkovResult")
//    checkovResult = checkovResult.replace("\u001B[0m","")
//
//    return when (checkovResult[0]) {
//        '{' -> getFailedChecksFromObj(JSONObject(checkovResult), framework)
//        '[' -> {
//            val results = JSONArray(checkovResult)
//            val res: ArrayList<CheckovResult> = arrayListOf()
//            for (obj in results) {
//                res.addAll(getFailedChecksFromObj(obj as JSONObject, framework))
//            }
//            res
//        }
//        else -> throw Exception("couldn't parse checkov results output, reason: $raw")
//    }
//}
//
//fun groupResultsByResource(results: ArrayList<CheckovResult>, project: Project, relativeFilePath: String): ResourceToCheckovResultsList {
//    val resourceToResultsMap = mutableMapOf<String, ArrayList<CheckovResult>>()
//
//    results.forEach { result ->
//        // setting path to absolute for docker mounted paths
//        result.file_abs_path = normalizeFilePathToAbsolute(result.file_abs_path, project.basePath!!, relativeFilePath)
//
//        val resourceResults = resourceToResultsMap.getOrDefault(result.resource, arrayListOf())
//        resourceResults.add(result)
//        resourceToResultsMap[result.resource] = resourceResults
//    }
//
//    return resourceToResultsMap
//}
//
//fun getFailedChecksFromObj(resultsObj: JSONObject, framework: String): ArrayList<CheckovResult> {
////    try {
////        try {
////            val failedRaw = resultsObj.get("failed")
//            if (resultsObj.has("failed") && resultsObj.get("failed") == 0) {
////                throw CheckovResultException("Results empty")
//                return arrayListOf()
//            }
//            // if failed does not appear in the raw response
////        } catch (e: JSONException) {
////        }
//
//        val summary = resultsObj.getJSONObject("summary")
//        val failedSummary = summary.get("failed")
//    val results = resultsObj.getJSONObject("results")
//
//        val parsingErrorSummary: Int = summary.getInt("parsing_errors")
//        if (parsingErrorSummary > 0 ) {
//            val parsingErrors = results.getJSONArray("parsing_errors")
//            throw CheckovResultParsingException("Checkov parsing error") //, parsingErrors)
//        }
//        if (failedSummary == 0) {
//            return arrayListOf()
//
////            throw CheckovResultException("Results empty")
//        }
//        val failedChecks = results.getJSONArray("failed_checks")
//        val resultsList = object : TypeToken<List<CheckovResult>>() {}.type
//        val checkType = resultsObj.getString("check_type")
//
//        val checkovResults: ArrayList<CheckovResult> = gson.fromJson(failedChecks.toString(), resultsList)
//        checkovResults.forEach { result -> result.check_type = checkType }
//
//        return checkovResults
////    } catch (e: Exception) {
////        LOG.error("error while parsing result for framework $framework", e)
////        throw e
////    }
//}