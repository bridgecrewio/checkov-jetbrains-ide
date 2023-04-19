package com.bridgecrew.services

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity

class ResultsFilter {
    companion object {
        private var severitiesToFilterBy: List<Severity>? = Severity.values().toMutableList()

        fun filterResultsByCategories(sourceList: List<BaseCheckovResult>, categories: List<Category>?): MutableList<BaseCheckovResult> {
            if(!categories.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    categories.contains(baseCheckovResult.category)
                }.toMutableList()

                return if(category == null) list.toMutableList() else list.filter { baseCheckovResult ->
                    category == baseCheckovResult.category
                }.toMutableList()

                    getResultsByCategory(filteredResults, selectedCategory)
        }
        fun filterResultBySeverities(filteredResults: MutableList<BaseCheckovResult>, severities: List<Severity>?): MutableList<BaseCheckovResult> {
            if(!severities.isNullOrEmpty()) {
                return filteredResults.filter { baseCheckovResult ->
                    severities.contains(baseCheckovResult.severity)
                }.toMutableList()
            }

            val list = if(sourceList.isNullOrEmpty()) checkovResults else sourceList
            return if(severities == null) list.toMutableList() else list.filter { baseCheckovResult ->
                severities.contains(baseCheckovResult.severity)
            }.toMutableList()
        }
    }
}