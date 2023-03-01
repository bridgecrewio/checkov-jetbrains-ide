package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.LicenseCheckovResult


class LicenseDictionaryPanel(result: LicenseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: Map<String, Any?> = mapOf(
            "License Type" to result.licenseType,
            "Approved SPDX" to result.approvedSPDX
    )
    init {
        createDictionaryLayout()
    }
}