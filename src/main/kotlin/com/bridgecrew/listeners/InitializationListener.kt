package com.bridgecrew.listeners

import com.intellij.util.messages.Topic

interface InitializationListener {
    companion object {
        val INITIALIZATION_TOPIC =
                Topic.create("Checkov initializator", InitializationListener::class.java)
    }
    fun initializationCompleted()
}