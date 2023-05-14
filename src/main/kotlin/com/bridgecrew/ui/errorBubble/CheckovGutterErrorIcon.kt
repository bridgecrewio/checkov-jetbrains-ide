package com.bridgecrew.ui.errorBubble

import com.bridgecrew.listeners.ErrorBubbleFixListener
import com.bridgecrew.results.BaseCheckovResult
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.markup.*
import com.intellij.openapi.project.ProjectManager
import com.intellij.util.ui.EmptyIcon
import icons.CheckovIcons
import java.awt.Color
import java.awt.Point
import javax.swing.Icon

class CheckovGutterErrorIcon(val results: List<BaseCheckovResult>, val offset: Int, val markup: MarkupModel, val firstRow: Int) : GutterIconRenderer() {

    var isFixInProgress = false

    init {
        val project = ProjectManager.getInstance().defaultProject
        val connection = project.messageBus.connect()
        connection.subscribe(ErrorBubbleFixListener.ERROR_BUBBLE_FIX_TOPIC, object : ErrorBubbleFixListener {
            override fun fixClicked() {
                isFixInProgress = true
            }
        })
    }

    override fun getIcon(): Icon {
        return if(isFixInProgress) EmptyIcon.create(0) else CheckovIcons.ErrorIcon
    }

    override fun isNavigateAction(): Boolean {
        return true
    }

    override fun getTooltipText() = "Errors found for this code block, click for actions"

    override fun hashCode() = icon.hashCode() + tooltipText.hashCode()

    override fun equals(other: Any?) = other is CheckovGutterErrorIcon && other.icon == icon && other.tooltipText == tooltipText && results == other.results

    override fun getClickAction(): AnAction {
        return object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                if(! isFixInProgress) {
                    val editor = e.getData(CommonDataKeys.EDITOR)
                    val visual = editor?.offsetToVisualPosition(offset)
                    val start = visual?.let { editor.visualPositionToXY(it) }
                    if (start != null) {

                        val textAttributes = TextAttributes()
                        textAttributes.backgroundColor = Color.decode("#F5E6E7")
                        val rangeHighlighter: RangeHighlighter = markup.addLineHighlighter(firstRow, HighlighterLayer.ERROR, textAttributes)

                        val screen = editor.contentComponent.locationOnScreen.let { Point(it.x + start.x, it.y + start.y) }
                        ApplicationManager.getApplication().invokeLater(Runnable {
                            CheckovErrorBubble(results, screen, markup, rangeHighlighter)
                        })
                    }
                }
            }
        }
    }
}