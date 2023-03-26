package com.bridgecrew.ui.buttons

import java.awt.Cursor
import javax.swing.JButton

open class CheckovLinkButton(private var text: String): JButton() {

    init {
        setButtonTitle("<html><u>$text</u></html>")
        this.isBorderPainted = false
        this.isOpaque = false
        this.isContentAreaFilled = false
        setEnabledLook()
    }

    private fun setButtonTitle(title: String) {
        setText(title)
    }

    protected fun setDisabledLook() {
        this.cursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)
    }

    protected fun setEnabledLook() {
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }
}