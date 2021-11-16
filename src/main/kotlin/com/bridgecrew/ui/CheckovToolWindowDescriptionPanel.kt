package com.bridgecrew.ui

import com.bridgecrew.CheckovResult
import com.bridgecrew.utils.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.SimpleToolWindowPanel
import com.intellij.openapi.util.IconLoader
import com.intellij.ui.ScrollPaneFactory
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.uiDesigner.core.GridLayoutManager
import java.awt.*
import javax.swing.*

private val LOG = logger<CheckovToolWindowDescriptionPanel>()

class CheckovToolWindowDescriptionPanel(val project: Project) : SimpleToolWindowPanel(true, true) {
    var descriptionPanel: JPanel = JPanel()
    var fixButton: JButton = JButton()

    init {
        installationDescription()
    }

    /**
     * Create display of description before scanning.
     */

    fun emptyDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel(""), BorderLayout.CENTER)
        return descriptionPanel
    }

    fun installationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(1, 1, Insets(0, 0, 0, 0), -1, -1)
        val scanningPanel = JPanel()
        scanningPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_NORTHEAST))
        scanningPanel.add(JLabel("Checkov is being installed"),  createGridRowCol(1,0,GridConstraints.ANCHOR_CENTER))
        descriptionPanel.add(scanningPanel, createGridRowCol(0,0,GridConstraints.ANCHOR_CENTER))
        return descriptionPanel
    }

    fun preScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(1, 1, Insets(0, 0, 0, 0), -1, -1)
        val scanningPanel = JPanel()
        scanningPanel.layout = GridLayoutManager(3, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_CENTER))
        scanningPanel.add(JLabel("Checkov is ready to run."),  createGridRowCol(1,0,GridConstraints.ANCHOR_CENTER))
        scanningPanel.add(JLabel("Scanning would start automatically once an IaC file is opened or saved"), createGridRowCol(2,0,GridConstraints.ANCHOR_CENTER))
        descriptionPanel.add(scanningPanel, createGridRowCol(0,0,GridConstraints.ANCHOR_CENTER))
        return descriptionPanel
    }

    fun configurationDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(CheckovSettingsPanel(project),  BorderLayout.CENTER)
        return descriptionPanel
    }

    fun duringScanDescription(): JPanel {
        descriptionPanel = JPanel()
        val scanningPanel = JPanel()
        descriptionPanel.layout = GridLayoutManager(1, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.layout = GridLayoutManager(2, 1, Insets(0, 0, 0, 0), -1, -1)
        scanningPanel.add(JLabel(IconLoader.getIcon("/icons/checkov_m.svg")), createGridRowCol(0,0,GridConstraints.ANCHOR_CENTER))
        scanningPanel.add(JLabel("Scanning your file..."), createGridRowCol(1,0,GridConstraints.ANCHOR_CENTER))
        descriptionPanel.add(scanningPanel, createGridRowCol(0,0,GridConstraints.ANCHOR_CENTER))
        return descriptionPanel
    }

    fun errorScanDescription(): JPanel {
        descriptionPanel = JPanel()
        descriptionPanel.add(JLabel("Checkov has failed to run on the file"), BorderLayout.CENTER)
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
    fun descriptionOfCheckovScan(checkovResult: CheckovResult): JPanel {
        descriptionPanel = JPanel()

        descriptionPanel.layout = GridLayoutManager(1, 1)
        val descriptions = JPanel()
        descriptions.layout = GridLayoutManager(4, 1)
        val policyDetailsTitle = createTitle(POLICYDETAILS,Font.BOLD, 15)
        val policyDetailsData = JLabel(checkovResult.check_name + "(${checkovResult.check_id})")
        val guidelines = urlLink("View Guidelines", checkovResult.guideline)
        descriptions.add(policyDetailsTitle, createGridRowCol(0, 0, GridConstraints.ANCHOR_NORTHWEST));
        descriptions.add(policyDetailsData, createGridRowCol(1, 0, GridConstraints.ANCHOR_NORTHWEST));
        descriptions.add(guidelines, createGridRowCol(3, 0, GridConstraints.ANCHOR_NORTHWEST));


        fixButton = JButton("Fix")
        if (!checkovResult.fixed_definition.isNullOrEmpty()){
            fixButton.isEnabled = true
            fixButton.addActionListener {
                LOG.info("fix button was presssed")
                ApplicationManager.getApplication().invokeLater {
                        val (start, end) = getOffsetReplaceByLines(checkovResult.file_line_range, project)
                        updateFile(checkovResult.fixed_definition, project, start, end)

                }
            }
            descriptions.add(fixButton, createGridRowCol(2, 0, GridConstraints.ANCHOR_NORTHWEST));
        }
        descriptionPanel.add(descriptions, createGridRowCol(0,0, GridConstraints.ANCHOR_NORTHWEST))
        return descriptionPanel

    }


    fun createScroll(checkovResult: CheckovResult): JScrollPane {
        val descriptionPanelRes = descriptionOfCheckovScan(checkovResult)
        return ScrollPaneFactory.createScrollPane(
            descriptionPanelRes,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
        )
    }

}