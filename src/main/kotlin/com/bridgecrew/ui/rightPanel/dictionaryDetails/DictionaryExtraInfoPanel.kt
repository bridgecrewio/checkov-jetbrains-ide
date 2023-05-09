package com.bridgecrew.ui.rightPanel.dictionaryDetails

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.utils.CheckovUtils
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import javax.swing.*

abstract class DictionaryExtraInfoPanel : JPanel() {

    // Field description (key) to field name from #BaseCheckovResult (value)
    abstract var fieldsMap: MutableMap<String, Any?>

    init {
        layout = GridBagLayout()
        background = UIUtil.getEditorPaneBackground()
        border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                val parentWidth = (e!!.component as Container).width
                preferredSize = Dimension(parentWidth / 2, preferredSize.height)
                revalidate()
            }
        })
    }

    fun addCustomPolicyGuidelinesIfNeeded(result: BaseCheckovResult){
        if(CheckovUtils.isCustomPolicy(result) && result.guideline != null){
            fieldsMap["Guidelines"] = "<html>${result.guideline}<html>"
        }
    }

    fun createDictionaryLayout(){
        val maxKeyWidth = fieldsMap.keys.maxByOrNull { it.length }?.let {
            getFontMetrics(font).stringWidth(it)
        } ?: 0

        val keyConstraints = GridBagConstraints().apply {
            weightx = 0.0
            anchor = GridBagConstraints.LINE_START
            insets = JBUI.insets(0, 0, 10, 15)
        }

        val valueConstraints = GridBagConstraints().apply {
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            gridwidth = GridBagConstraints.REMAINDER
            insets = JBUI.insetsBottom(10)
        }

        val boldFont = Font(font.name, Font.BOLD, font.size)

        for ((key, value) in fieldsMap) {
            val isEmptyVal = (value == null || value.toString().isEmpty())
            if ((key.lowercase() == "description" || key.lowercase() == "custom policy") && isEmptyVal) // TODO - remove Custom Policy part before release
                continue
            val valueAsString = if (isEmptyVal) "---" else value.toString().trim()
            val keyLabel = JLabel(key)
            keyLabel.font = boldFont
            keyLabel.preferredSize = Dimension(maxKeyWidth + 50, keyLabel.preferredSize.height)
            add(keyLabel, keyConstraints)
            val valueLabel = JLabel(valueAsString)
            valueLabel.toolTipText = valueAsString
            add(valueLabel, valueConstraints)
        }
    }
}