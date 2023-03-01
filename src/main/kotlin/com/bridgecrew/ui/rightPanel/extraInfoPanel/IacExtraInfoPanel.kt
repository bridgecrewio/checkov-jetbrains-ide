package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.rightPanel.dictionaryDetails.IacDictionaryPanel

class IacExtraInfoPanel(result: BaseCheckovResult): CheckovExtraInfoPanel() {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        add(IacDictionaryPanel(result))
        setDimensions()
    }
}