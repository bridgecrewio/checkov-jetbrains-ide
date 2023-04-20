package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.VulnerabilityCheckovResult

class VulnerabilitiesDictionaryPanel(result: VulnerabilityCheckovResult): DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "Vulnerable Package Version" to result.packageName,
            "Vulnerable Package Name" to result.packageVersion,
            "Fixed Version" to result.fixVersion,
            "Root Package" to result.rootPackageName,
            "Root Package Version" to result.rootPackageVersion,
            "Compliant Version (Fix all CVEs)" to result.rootPackageFixVersion,
            "CVE ID" to result.name,
            "CVSS" to result.cvss,
            "Published" to result.publishedDate,
            "Vector" to result.vector,
            "Risk Factors" to result.riskFactors,
            "Description" to result.description,
            "Category" to result.category,
            "Check Type" to result.checkType.name.lowercase(),
    )

    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}