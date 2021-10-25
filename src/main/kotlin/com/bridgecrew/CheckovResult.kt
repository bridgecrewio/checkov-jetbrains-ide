package com.bridgecrew
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

data class CheckovResult(
    val check_id: String,
    val bc_check_id: String = "",
    val check_name: String,
    val file_path: String,
    val file_line_range: ArrayList<Int>,
    val resource: String,
    val guideline: String = "\"No Guide\")",
    val fixed_definition: String = ""
    )

fun getFailedChecksFromResultString(raw: String): ArrayList<CheckovResult> {
    val gson = Gson()
    val json = JSONObject(raw)
    val results = json.getJSONObject("results")
    val failedChecks = results.getJSONArray("failed_checks")
    val resultsList = object : TypeToken<List<CheckovResult>>() {}.type
    return gson.fromJson(failedChecks.toString(), resultsList)
}
