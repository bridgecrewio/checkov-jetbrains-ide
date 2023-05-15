package com.bridgecrew.listeners

import com.intellij.util.messages.Topic

interface ErrorBubbleFixListener {

    companion object {
        val ERROR_BUBBLE_FIX_TOPIC = Topic.create("Error Bubble Fix", ErrorBubbleFixListener::class.java)
    }

    fun fixClicked()
}