package com.bridgecrew.ui

import com.bridgecrew.results.Category
import com.intellij.ui.content.impl.ContentImpl
import javax.swing.JComponent

class CheckovTabContent(component: JComponent, name: String, val id: String, val category: Category?) : ContentImpl(component, name, false) {
}