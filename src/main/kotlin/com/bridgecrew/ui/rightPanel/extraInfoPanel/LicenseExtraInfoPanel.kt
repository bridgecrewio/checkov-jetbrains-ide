package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.LicenseCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.LicenseDictionaryPanel

class LicenseExtraInfoPanel(result: LicenseCheckovResult): CheckovExtraInfoPanel() {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        add(LicenseDictionaryPanel(result))
        setDimensions()
    }
}