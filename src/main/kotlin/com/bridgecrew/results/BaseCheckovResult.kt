package com.bridgecrew.results

import com.google.gson.annotations.SerializedName
import java.nio.file.Path


enum class Category(category: String) {
    IAC("IAC"),
    SECRETS("Secrets"),
    VULNERABILITIES("Vulnerabilities"),
    LICENSES("Licenses")
}

enum class CheckType(checkType: String) {
    ANSIBLE("ansible"),
    ARM("arm"),
    BICEP("bicep"),
    CLOUDFORMATION("cloudformation"),
    DOCKERFILE("dockerfile"),
    HELM("helm"),
    JSON("json"),
    YAML("yaml"),
    KUSTOMIZE("kustomize"),
    OPENAPI("openapi"),
    SCA_PACKAGE("sca_package"),
    SCA_IMAGE("sca_image"),
    SECRETS("secrets"),
    SERVERLESS("serverless"),
    TERRAFORM("terraform"),
    TERRAFORM_PLAN("terraform_plan")
}

enum class Severity {
    CRITICAL,
    HIGH,
    MEDIUM,
    LOW,
    UNKNOWN
}

open class BaseCheckovResult(
        @SerializedName("Category")
        val category: Category,
        @SerializedName("check_type")
        val checkType: CheckType,
        @SerializedName("file_path")
        val filePath: Path,
        @SerializedName("resource")
        val resource: String,
        @SerializedName("name")
        val name: String,
        @SerializedName("id")
        val id: String,
        @SerializedName("severity")
        val severity: Severity,
        @SerializedName("description")
        val description: String?,
        @SerializedName("guideline")
        val guideline: String?,
        @SerializedName("file_abs_path")
        val absoluteFilePath: String,
        @SerializedName("file_line_range")
        val fileLineRange: List<Int>,
        @SerializedName("fixed_definition")
        val fixDefinition: String?,
        @SerializedName("code_block")
        val codeBlock: List<List<Object>>
)