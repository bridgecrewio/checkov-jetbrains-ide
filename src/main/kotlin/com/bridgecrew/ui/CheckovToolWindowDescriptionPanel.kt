package com.bridgecrew.ui

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.ui.rightPanel.CheckovErrorRightPanel
import com.bridgecrew.utils.createGridRowCol
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ScrollPaneFactory
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import com.intellij.util.ui.UIUtil
import com.intellij.util.ui.JBUI
import icons.CheckovIcons
import java.awt.*
import javax.swing.*

private val LOG = logger<CheckovToolWindowDescriptionPanel>()

class CheckovToolWindowDescriptionPanel(val project: Project) : SimpleToolWindowPanel(true, true) {
    var descriptionPanel: JPanel = JPanel()

    init {
        initializationDescription()
    }

    /**
     * Create display of description before scanning.
     */

    fun emptyDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        descriptionPanel.add(JLabel("Select a file from the errors tree to show more details about it here"), BorderLayout.CENTER)
        return descriptionPanel
    }

    fun noErrorsPanel(): JPanel {
        val mainPanel = JPanel()
        mainPanel.background = UIUtil.getEditorPaneBackground()
        val imagePanel = JPanel()
        imagePanel.layout = BoxLayout(imagePanel, BoxLayout.Y_AXIS)
        imagePanel.background = UIUtil.getEditorPaneBackground()
        val status = JLabel("Great Job - Your Code Is Valid!")
        status.alignmentX = CENTER_ALIGNMENT
        status.font = Font(status.font.name, Font.BOLD, 14)
        val iconLabel = JLabel(CheckovIcons.prismaIcon)
        iconLabel.alignmentX = CENTER_ALIGNMENT
        val prismaText = JLabel("Prisma Cloud")
        prismaText.alignmentX = CENTER_ALIGNMENT
        imagePanel.add(Box.createRigidArea(Dimension(0, 50)))
        imagePanel.add(Box.createVerticalGlue())
        imagePanel.add(status)
        imagePanel.add(Box.createRigidArea(Dimension(0, 15)))
        imagePanel.add(iconLabel)
        imagePanel.add(Box.createRigidArea(Dimension(0, 10)))
        imagePanel.add(prismaText)
        imagePanel.add(Box.createVerticalGlue())
        mainPanel.add(imagePanel, BorderLayout.CENTER)
        return mainPanel
    }

    fun initializationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        scanningPanel.add(JLabel("Prisma Cloud is being initialized"),  createGridRowCol(1,0, GridConstraints.ANCHOR_NORTH))
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun preScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel("Prisma Cloud is ready to run."),  createGridRowCol(0,0,GridConstraints.ANCHOR_NORTH))
        scanningPanel.background = UIUtil.getEditorPaneBackground() ?: scanningPanel.background
        scanningPanel.add(JLabel("Scanning would start automatically once a file is opened or saved"), createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun configurationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = createImagePanel()
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val configPanel = JPanel()
        configPanel.background = UIUtil.getEditorPaneBackground()
        configPanel.add(CheckovSettingsPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(configPanel,  createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun duringScanDescription(description: String): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.add(JLabel(description), GridConstraints.ANCHOR_CENTER)
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun failedScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        descriptionPanel.background = UIUtil.getEditorPaneBackground() ?: descriptionPanel.background
        val imagePanel = createImagePanel()
        val scanningPanel = JPanel()
        scanningPanel.background = UIUtil.getEditorPaneBackground()
        scanningPanel.add(JLabel("Scan failed to run, please check the logs for further action"), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    /**
     * Create description for specific checkov result.
     */
    private fun descriptionOfCheckovScan(checkovResult: BaseCheckovResult): JPanel {
        return CheckovErrorRightPanel(checkovResult)
    }

    fun createScroll(checkovResult: BaseCheckovResult): JScrollPane {
        val descriptionPanelRes = descriptionOfCheckovScan(checkovResult)
        return ScrollPaneFactory.createScrollPane(
            descriptionPanelRes,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
        )
    }

    private fun createImagePanel(): JPanel {
        val imagePanel = JPanel()
        imagePanel.layout = GridLayoutManager(2, 2, JBUI.emptyInsets(), -1, -1)
        imagePanel.background = UIUtil.getEditorPaneBackground()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/plugin_large_icon.svg")), createGridRowCol(0,0, GridConstraints.ANCHOR_CENTER))
        imagePanel.add(JLabel("Prisma Cloud"), createGridRowCol(1,0, GridConstraints.ANCHOR_NORTHEAST))
        imagePanel.add(JLabel("  "), createGridRowCol(0,1, GridConstraints.ANCHOR_NORTHEAST))
        return imagePanel
    }

}