package com.bridgecrew.ui

import javax.swing.*;
import com.intellij.json.psi.JsonObject
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.google.gson.Gson
import com.bridgecrew.CheckovResult
import java.io.File
import java.io.InputStream
import com.google.gson.reflect.TypeToken

class CheckovToolWindow : SimpleToolWindowPanel(false, true) {
    val gson: Gson = Gson()
    private val resultsList: ArrayList<CheckovResult> = arrayListOf<CheckovResult>()
    val results: List<String> = mutableListOf("""{
        "check_id": "CKV2_AWS_12",
        "bc_check_id": "BC_AWS_LOGGING_10",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentIssueDetails.java",
        "file_abs_path": "/Users/yorhov/development/test1.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        1,
        10
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""",
        """{
        "check_id": "CKV2_AWS_11",
        "bc_check_id": "BC_AWS_LOGGING_9",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentsTree.java",
        "file_abs_path": "/Users/yorhov/development/test.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        1,
        59
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""",
        """{
        "check_id": "CKV2_AWS_13",
        "bc_check_id": "BC_AWS_LOGGING_13",
        "check_name": "Ensure VPC flow logging is enabled in all VPCs",
        "file_path": "src/main/java/com/jfrog/ide/idea/ui/ComponentsTree.java",
        "file_abs_path": "/Users/yorhov/development/test.tf",
        "repo_file_path": "/test.tf",
        "file_line_range": [
        43,
        59
        ],
        "resource": "aws_vpc.eks_vpc",
        "evaluations": null,
        "check_class": "checkov.common.graph.checks_infra.base_check",
        "fixed_definition": null,

        "caller_file_path": null,
        "caller_file_line_range": null,
        "guideline": "https://docs.bridgecrew.io/docs/logging_9-enable-vpc-flow-logging"
    }""")


    fun getResultsList():  ArrayList<CheckovResult>{

        val fileString = readFileAsLinesUsingUseLines("/Users/yorhov/development/checkov-jetbrains-ide/src/main/kotlin/com/bridgecrew/a.json")
        val resultsList1 = object : TypeToken<List<CheckovResult>>() {}.type

        val listOfCheckovResults: ArrayList<CheckovResult> = gson.fromJson(fileString, resultsList1)

        return listOfCheckovResults
    }
    fun readFileAsLinesUsingUseLines(fileName: String): String{
        val inputStream: InputStream = File(fileName).inputStream()
        val inputString = inputStream.bufferedReader().use { it.readText() }
        return inputString
    }


}
