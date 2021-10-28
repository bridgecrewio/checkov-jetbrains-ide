package com.bridgecrew.services

import com.bridgecrew.ResourceToResultsMap
import com.intellij.openapi.components.Service

@Service
class ResultsCacheService {
    private val results: MutableMap<String, ResourceToResultsMap> = mutableMapOf()

    fun getAllResults(): MutableMap<String, ResourceToResultsMap> {
        return results;
    }

    fun setResult(key: String, value: ResourceToResultsMap) {
        results[key] = value
    }

    fun deleteAll() {
        results.keys.forEach{
            results.remove(it)
        }
    }
}