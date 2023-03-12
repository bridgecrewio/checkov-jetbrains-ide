package com.bridgecrew

import com.google.gson.Gson

val gson = Gson()

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