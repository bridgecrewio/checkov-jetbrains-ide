package com.bridgecrew.ui
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.ui.ScrollPaneFactory
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.sun.jna.platform.unix.X11.Screen
import java.awt.*
import javax.swing.*


//class CheckovToolWindowDescriptionPanel(
//    val checkovResult: CheckovResult = "",
//    val psiFile: PsiFile = ""
//): JPanel() {
class CheckovToolWindowDescriptionPanel(): SimpleToolWindowPanel(true, true) {
    val descriptionPanel: JPanel
    val text1: JLabel
    val text2: JLabel
    val text3: JLabel
    val fixButton: JButton
    init {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(3, 2)
        fixButton = JButton("Fix")
        text1 = JLabel("Test1")
        text2 = JLabel("Test2")
        text3 = JLabel("Test3")

        descriptionPanel.add(text1, GridConstraints(
            0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
            null, 1, false
        ));
        descriptionPanel.add(text2, GridConstraints(
            1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
            null, 1, false
        ));
        descriptionPanel.add(text3,GridConstraints(
            2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
            null, 1, false
        ));
        descriptionPanel.add(fixButton, GridConstraints(
            0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null,
            null, 1, false
        ));

    }

    fun createScroll(): JScrollPane{
        return ScrollPaneFactory.createScrollPane(
            descriptionPanel,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }


}