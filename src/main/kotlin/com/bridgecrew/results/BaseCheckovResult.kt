package com.bridgecrew.results


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
        val category: Category,
        val checkType: CheckType,
        val filePath: String,
        val resource: String,
        val name: String,
        val id: String,
        val severity: Severity,
        val description: String?,
        val guideline: String?,
        val absoluteFilePath: String,
        val fileLineRange: List<Int>,
        val fixDefinition: String?,
        val codeBlock: List<List<Object>>
)