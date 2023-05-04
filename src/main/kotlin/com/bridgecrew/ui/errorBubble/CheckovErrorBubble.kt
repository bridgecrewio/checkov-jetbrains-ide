package com.bridgecrew.ui.errorBubble

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.intellij.openapi.editor.markup.MarkupModel
import com.intellij.openapi.editor.markup.RangeHighlighter
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Point
import javax.swing.Action
import javax.swing.JComponent


typealias navigationCallback = (Int, String) -> Unit

class CheckovErrorBubble(val results: List<BaseCheckovResult>, private val modalLocation: Point, val markup: MarkupModel, val rangeHighlighter: RangeHighlighter) : DialogWrapper(true) {

    private var panelList: ArrayList<ErrorBubbleInnerPanel> = arrayListOf()
    private var currentPanel: ErrorBubbleInnerPanel? = null


    private val callBack: navigationCallback = { index, action ->

        currentPanel?.let {

            val newIdx = if (action == "right") index + 1 else index - 1
            val newCurr = panelList[newIdx]

            contentPanel.removeAll()
            contentPanel.add(newCurr)
            currentPanel = newCurr
            init()

            contentPanel.revalidate()
        }
    }

    init {
        val (vulnerabilityResults, otherResults) = results.partition { it.category == Category.VULNERABILITIES }

        val vulnerabilityCount = if (vulnerabilityResults.isNotEmpty()) 1 else 0
        val totalPanels = otherResults.size + vulnerabilityCount
        var runningIndex = 0
        otherResults.forEachIndexed { index, baseCheckovResult ->
            panelList.add(ErrorBubbleInnerPanel(baseCheckovResult, 0, index, totalPanels, callBack))
            runningIndex = index + 1
        }

        if (vulnerabilityResults.isNotEmpty()) {
            val vulnerability = vulnerabilityResults.sortedBy { it.severity }[0]
            panelList.add(ErrorBubbleInnerPanel(vulnerability, vulnerabilityResults.size, runningIndex, totalPanels, callBack))
        }

        currentPanel = panelList[0]

        init()
        setLocation(modalLocation.x, modalLocation.y)
        show()
    }

    //remove default actions
    override fun createActions(): Array<Action> {
        return emptyArray()
    }

    override fun createCenterPanel(): JComponent? {
        return currentPanel
    }

    override fun doCancelAction() {
        super.doCancelAction()
        markup.removeHighlighter(rangeHighlighter)
    }
}