package com.bridgecrew
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

data class CheckovResult(
    val check_id: String,
    val bc_check_id: String,
    val check_name: String? = "",
    val file_path: String? = "",
    val file_line_range: ArrayList<Int>? = null,
    val resource: String? = "",
    val guideline: String? = "",
    val fixed_definition: String? = ""
    )



