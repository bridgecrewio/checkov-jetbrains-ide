package com.bridgecrew.ui.buttons

import java.awt.Cursor
import javax.swing.JButton

open class CheckovLinkButton(private var text: String): JButton() {

    init {
        setButtonTitle("<html><u>$text</u></html>")
        this.isBorderPainted = false
        this.isOpaque = false
        this.isContentAreaFilled = false
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
    }

    private fun setButtonTitle(title: String) {
        setText(title)
    }
}