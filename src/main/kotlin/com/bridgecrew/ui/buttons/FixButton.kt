package com.bridgecrew.ui.buttons

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.actions.FixAction
import javax.swing.JButton


class FixButton(val result: BaseCheckovResult) : JButton() {

    init {
        text = "Fix"
        addActionListener(FixAction(this, result))
    }
}