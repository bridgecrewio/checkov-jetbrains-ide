package com.bridgecrew
import com.bridgecrew.services.CheckovResultException
import com.bridgecrew.services.CheckovResultParsingException
import com.bridgecrew.utils.normalizeFilePathToAbsolute
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

val gson = Gson()

data class CheckovResult(
    val check_id: String,
    val bc_check_id: String = "",
    val check_name: String,
    val file_path: String,
    val repo_file_path: String,
    var file_abs_path: String,
    val file_line_range: ArrayList<Int>,
    val resource: String,
    val guideline: String = "\"No Guide\")",
    val fixed_definition: String = ""
    )

typealias ResourceToCheckovResultsList = MutableMap<String, ArrayList<CheckovResult>>

fun getFailedChecksFromResultString(raw: String): ArrayList<CheckovResult> {
    if (raw.isEmpty()){
        throw CheckovResultException("Checkov result returned empty")
    }
    return when (raw[0]) {
        '{' -> getFailedChecksFromObj(JSONObject(raw))
        '[' -> {
            val results = JSONArray(raw)
            var res: ArrayList<CheckovResult> = arrayListOf()
            for (obj in results) {
                res.addAll(getFailedChecksFromObj(obj as JSONObject))
            }
            res
        }
        else -> throw Exception("couldn't parse checkov results output, reason: $raw")
    }
}

fun groupResultsByResource(results: ArrayList<CheckovResult>, project: Project, relativeFilePath: String): ResourceToCheckovResultsList {
    val resourceToResultsMap = mutableMapOf<String, ArrayList<CheckovResult>>()

    results.forEach { result ->
        // setting path to absolute for docker mounted paths
        result.file_abs_path = normalizeFilePathToAbsolute(result.file_abs_path, project.basePath!!, relativeFilePath)

        val resourceResults = resourceToResultsMap.getOrDefault(result.resource, arrayListOf())
        resourceResults.add(result)
        resourceToResultsMap[result.resource] = resourceResults
    }

    return resourceToResultsMap
}

fun getFailedChecksFromObj(resultsObj: JSONObject): ArrayList<CheckovResult> {
    try {
        val failedRaw = resultsObj.get("failed")
        if (failedRaw == 0){
            throw CheckovResultException("Results empty")
        }
        val summary = resultsObj.getJSONObject("summary")
        val failedSummary = summary.get("failed")
        val parsingErrorSummary: Int =summary.getInt("parsing_errors")
        if (parsingErrorSummary > 0 ) {
            throw CheckovResultParsingException("Checkov parsing error")
        }
        if (failedSummary == 0){
            throw CheckovResultException("Results empty")
        }
    } catch (e : JSONException) { }
    val results = resultsObj.getJSONObject("results")
    val failedChecks = results.getJSONArray("failed_checks")
    val resultsList = object : TypeToken<List<CheckovResult>>() {}.type
    return gson.fromJson(failedChecks.toString(), resultsList)
}

