package com.bridgecrew.services

import com.bridgecrew.ResourceToCheckovResultsList
import com.intellij.openapi.components.Service

@Service
class ResultsCacheService {
    private val results: MutableMap<String, ResourceToCheckovResultsList> = mutableMapOf()

    fun getAllResults(): MutableMap<String, ResourceToCheckovResultsList> {
        return results;
    }

    fun setResult(key: String, value: ResourceToCheckovResultsList) {
        results[key] = value
    }

    fun deleteAll() {
        results.keys.forEach{
            results.remove(it)
        }
    }
}