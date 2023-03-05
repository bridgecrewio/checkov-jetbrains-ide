package com.bridgecrew.ui.rightPanel.extraInfoPanel

import com.bridgecrew.results.BaseCheckovResult

class SecretsExtraInfoPanel(result: BaseCheckovResult): CheckovExtraInfoPanel() {

    init {
        initLayout()
        createRightPanelDescriptionLine(result.name)
        setDimensions()
    }
}