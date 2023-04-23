package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.LicenseCheckovResult


class LicenseDictionaryPanel(result: LicenseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "License Type" to result.licenseType,
            "Approved SPDX" to result.approvedSPDX,
            "Description" to result.description,
            "Category" to result.category, // TODO - remove before release
            "Check Type" to result.checkType.name.lowercase(), // TODO - remove before release
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}