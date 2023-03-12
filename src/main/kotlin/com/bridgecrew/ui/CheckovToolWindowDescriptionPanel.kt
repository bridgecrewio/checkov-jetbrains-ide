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
import java.awt.BorderLayout
import java.awt.Insets
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
        descriptionPanel.add(JLabel("Select a file from the errors tree to show more details about it here"), BorderLayout.CENTER)
        return descriptionPanel
    }

    fun initializationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        val scanningPanel = JPanel()
        scanningPanel.add(JLabel("Checkov is being initialized"),  createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun preScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        val scanningPanel = JPanel()
        scanningPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel("Checkov is ready to run."),  createGridRowCol(0,0,GridConstraints.ANCHOR_NORTH))
        scanningPanel.add(JLabel("Scanning would start automatically once an IaC file is opened or saved"), createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun configurationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")))
        val configPanel = JPanel()
        configPanel.add(CheckovSettingsPanel(project), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(configPanel,  createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun duringScanDescription(description: String): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        val imagePanel = JPanel()
        imagePanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        val scanningPanel = JPanel()
        scanningPanel.add(JLabel(description), GridConstraints.ANCHOR_CENTER)
        descriptionPanel.add(imagePanel, createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        descriptionPanel.add(scanningPanel, createGridRowCol(1,0,GridConstraints.ANCHOR_NORTH))
        return descriptionPanel
    }

    fun errorScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel("Checkov has failed to run on the file"), BorderLayout.CENTER) //
        return descriptionPanel
    }

    fun errorParsingScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel("Checkov has failed to run on the file due to parsing errors, Please make sure that you file is valid."), BorderLayout.CENTER)
        return descriptionPanel
    }

    fun successfulScanDescription(fileName: String): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel("Checkov scanning finished, No errors have been detected in this file: $fileName"), BorderLayout.CENTER)
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
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }

}