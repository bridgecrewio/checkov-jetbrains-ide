package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.LicenseCheckovResult


class LicenseDictionaryPanel(result: LicenseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: MutableMap<String, Any?> = mutableMapOf(
            "License Type" to result.licenseType,
            "Approved SPDX" to result.approvedSPDX
    )
    init {
        addCustomPolicyGuidelinesIfNeeded(result)
        createDictionaryLayout()
    }
}