package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult

class IacDictionaryPanel(result: BaseCheckovResult) : DictionaryExtraInfoPanel() {

    override var fieldsMap: Map<String, Any?> = mapOf(
            "Resource" to result.resource,
            "Policy Name" to result.name
    )
    init {
        createDictionaryLayout()
    }
}