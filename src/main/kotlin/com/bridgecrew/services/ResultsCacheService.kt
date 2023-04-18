package com.bridgecrew.services

import com.bridgecrew.CheckovResult
import com.bridgecrew.results.*
import com.bridgecrew.utils.CheckovUtils
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project

@Service
class ResultsCacheService(val project: Project) {
    var checkovResults: MutableList<BaseCheckovResult> = mutableListOf()

    private val checkovResultsComparator: Comparator<BaseCheckovResult> = CheckovResultsComparatorGenerator.generateCheckovResultComparator()
    private val baseDir: String = project.basePath!!
    private var selectedCategory: Category? = null
    private var severitiesToFilterBy: List<Severity>? = Severity.values().toMutableList()

    fun getAllCheckovResults(): List<BaseCheckovResult> {
        return this.checkovResults
    }

    fun updateCategory(category: Category?){
        this.selectedCategory = category
    }

    fun updateSelectedSeverities(severityList: List<Severity>) {
        this.severitiesToFilterBy = severityList
    }

    fun getCheckovResultsByPath(filePath: String): List<BaseCheckovResult> {
        return this.checkovResults.filter {baseCheckovResult ->
            baseCheckovResult.filePath == "/${filePath}"
        }
    }

    fun getCheckovResultsFilteredBySeverityGroupedByPath(): Map<String, List<BaseCheckovResult>> {
        val filteredResults = getFilteredResults(emptyList(), emptyList())
        checkovResults.sortWith(checkovResultsComparator)
        return filteredResults.groupBy { it.filePath }
    }

    fun getFilteredResults(categories: List<Category>?, severities: List<Severity>?): List<BaseCheckovResult> {
        var filteredResults = checkovResults
        filteredResults = if(!categories.isNullOrEmpty()) {
            filteredResults.filter { baseCheckovResult ->
                categories.contains(baseCheckovResult.category)
            }.toMutableList()
        } else (
            getResultsByCategory(filteredResults, selectedCategory)
        )
        filteredResults = if(!severities.isNullOrEmpty()) {
            filteredResults.filter { baseCheckovResult ->
                severities.contains(baseCheckovResult.severity)
            }.toMutableList()
        } else (
            getResultsBySeverities(filteredResults, severitiesToFilterBy)
        )

        return filteredResults
    }

    private fun getResultsByCategory(sourceList: List<BaseCheckovResult>?, category: Category?): MutableList<BaseCheckovResult> {
        val list = if(sourceList.isNullOrEmpty()) checkovResults else sourceList
        return if(category == null) list.toMutableList() else list.filter { baseCheckovResult ->
            category == baseCheckovResult.category
        }.toMutableList()
    }

    private fun getResultsBySeverities(sourceList: List<BaseCheckovResult>?, severities: List<Severity>?): MutableList<BaseCheckovResult> {
        val list = if(sourceList.isNullOrEmpty()) checkovResults else sourceList
        return if(severities == null) list.toMutableList() else list.filter { baseCheckovResult ->
            severities.contains(baseCheckovResult.severity)
        }.toMutableList()
    }

    fun addCheckovResult(checkovResult: BaseCheckovResult) {
        checkovResults.add(checkovResult)
    }

    fun addCheckovResults(newCheckovResults: List<CheckovResult>) {
        newCheckovResults.forEach { newCheckovResult ->
            run {
                checkovResults.removeIf { savedCheckovResult -> savedCheckovResult.absoluteFilePath == newCheckovResult.file_abs_path }

            }
        }

        setCheckovResultsFromResultsList(newCheckovResults)
    }

    fun deleteAllCheckovResults() {
        checkovResults.clear()
    }

    fun setMockCheckovResultsFromExampleFile() {
        val inputString: String = javaClass.classLoader.getResource("examples/example-output.json").readText()
        val checkovResults: List<CheckovResult> = CheckovUtils.extractFailedChecksAndParsingErrorsFromCheckovResult(inputString, "mock - examples/example-output.json").failedChecks
        setCheckovResultsFromResultsList(checkovResults)
    }
    fun setCheckovResultsFromResultsList(results: List<CheckovResult>) {
        for (result in results) {
            val category: Category = mapCheckovCheckTypeToScanType(result.check_type, result.check_id)
            val resource: String = getResource(result, category)
            val name: String = getResourceName(result, category)
                    ?: throw Exception("null name, category is ${category.name}, result is $result")
            val checkType = CheckType.valueOf(result.check_type.uppercase())
            val severity = if (result.severity != null) Severity.valueOf(result.severity.uppercase()) else Severity.UNKNOWN
            val description = if(!result.description.isNullOrEmpty()) result.description else result.short_description

            when (category) {
                Category.VULNERABILITIES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is vulnerability but no vulnerability_details")
                    }
                    val vulnerabilityCheckovResult = VulnerabilityCheckovResult(
                            checkType, result.file_abs_path.replace(baseDir, ""),
                            resource, name, result.check_id, severity, description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.cvss,
                            result.vulnerability_details.package_version,
                            result.vulnerability_details.lowest_fixed_version,
                            result.vulnerability_details.link,
                            result.vulnerability_details.published_date,
                            result.vulnerability_details.vector,
                            result.vulnerability_details.id,
                            result.file_path,
                            null, // TODO - fix after Saar's team fixes their side
                            result.vulnerability_details.root_package_name,
                            result.vulnerability_details.root_package_version,
                            result.vulnerability_details.root_package_fix_version,

                            )
                    checkovResults.add(vulnerabilityCheckovResult)

                    continue
                }
                Category.SECRETS -> {
                    val secretCheckovResult = SecretsCheckovResult(checkType, result.file_abs_path.replace(baseDir, ""),
                            resource, name, result.check_id, severity, description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    checkovResults.add(secretCheckovResult)
                    continue
                }
                Category.IAC -> {
                    val iacCheckovResult = IacCheckovResult(checkType, result.file_abs_path.replace(baseDir, ""),
                            resource, name, result.check_id, severity, description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block)
                    checkovResults.add(iacCheckovResult)
                    continue
                }
                Category.LICENSES -> {
                    if (result.vulnerability_details == null) {
                        throw Exception("type is license but no vulnerability_details")
                    }

                    val licenseCheckovResult = LicenseCheckovResult(checkType, result.file_abs_path.replace(baseDir, ""),
                            resource, name, result.check_id, severity, description,
                            result.guideline, result.file_abs_path, result.file_line_range, result.fixed_definition,
                            result.code_block,
                            result.vulnerability_details.package_name,
                            result.vulnerability_details.license,
                            result.check_id.uppercase() == "BC_LIC_1"
                    )
                    checkovResults.add(licenseCheckovResult)
                    continue
                }
            }
        }
    }

    private fun addToSorted(checkovResult: BaseCheckovResult) {
        val index = checkovResults.binarySearch(checkovResult, checkovResultsComparator)
        val insertionPoint =
                if (index < 0) {
                    -(index + 1)
                } else {
                    index
                }
        checkovResults.add(insertionPoint, checkovResult)
    }
    private fun mapCheckovCheckTypeToScanType(checkType: String, checkId: String): Category {
        when (checkType) {
            "ansible", "arm", "bicep", "cloudformation", "dockerfile", "helm", "json",
            "yaml", "kubernetes", "kustomize", "openapi", "serverless", "terraform", "terraform_plan" -> {
                return Category.IAC
            }

            "secrets" -> {
                return Category.SECRETS
            }

            "sca_package", "sca_image" -> {
                if (checkId.uppercase().startsWith("BC_LIC")) {
                    return Category.LICENSES
                } else if (checkId.uppercase().startsWith("BC_VUL")) {
                    return Category.VULNERABILITIES
                }
            }
        }

        throw Exception("Scan type is not found in the result!")
    }

    private fun getResourceName(result: CheckovResult, category: Category): String? {
        return when (category) {
            Category.IAC, Category.SECRETS -> {
                "${result.check_name} (${result.file_line_range[0]} - ${result.file_line_range[1]})"
            }

            Category.LICENSES -> {
                result.vulnerability_details?.license ?: "NOT FOUND"
            }

            Category.VULNERABILITIES -> {
                result.vulnerability_details?.id
            }
        }
    }
    private fun getResource(result: CheckovResult, category: Category) : String {
        if (category == Category.VULNERABILITIES || category == Category.LICENSES) {
            return result.vulnerability_details?.package_name ?: throw Exception("null resource, category is ${category.name}, result is $result")
        }

        return result.resource
    }
}
