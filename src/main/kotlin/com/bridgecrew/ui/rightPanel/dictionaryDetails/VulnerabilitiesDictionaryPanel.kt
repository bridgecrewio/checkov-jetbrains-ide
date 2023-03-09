package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.VulnerabilityCheckovResult

class VulnerabilitiesDictionaryPanel(result: VulnerabilityCheckovResult): DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
        "Package Name" to result.resource,
        "CVE ID" to result.name,
        "Vulnerable Package Version" to result.packageVersion,
        "Fixed Version" to result.fixVersion,
        "CVSS" to result.cvss,
        "Published" to result.publishedDate,
        "Vector" to result.vector,
        "Risk Factors" to result.riskFactors
    )

    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}