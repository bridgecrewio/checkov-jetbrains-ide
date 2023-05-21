package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.LicenseDictionaryPanel

class LicenseExtraInfoPanel(result: LicenseCheckovResult) : CheckovExtraInfoPanel(result) {
    init {
        initLayout()
        add(LicenseDictionaryPanel(result))
        addCodeDiffPanel()
        setDimensions()
    }
}