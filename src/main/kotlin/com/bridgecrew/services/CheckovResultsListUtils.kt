package com.bridgecrew.services

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.ui.CheckovToolWindowFactory
import com.bridgecrew.ui.actions.SeverityFilterActions

class CheckovResultsListUtils {
    companion object {
        private val checkovResultsComparator: Comparator<BaseCheckovResult> = CheckovResultsComparatorGenerator.generateCheckovResultComparator()

//        private var severitiesToFilterBy: List<Severity> = Severity.values().toMutableList()

        fun filterResultsByCategories(sourceList: List<BaseCheckovResult>, categories: List<Category>?): MutableList<BaseCheckovResult> {
            if (!categories.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    categories.contains(baseCheckovResult.category)
                }.toMutableList()
            }

            val selectedCategory = CheckovToolWindowFactory.lastSelectedCategory

            if (selectedCategory == null) {
                return sourceList.toMutableList()
            }

            return sourceList.filter { baseCheckovResult -> selectedCategory == baseCheckovResult.category }.toMutableList()
        }

        fun filterResultBySeverities(sourceList: List<BaseCheckovResult>, severities: List<Severity>?): MutableList<BaseCheckovResult> {
            if (!severities.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    severities.contains(baseCheckovResult.severity)
                }.toMutableList()
            }

            return sourceList.filter { baseCheckovResult ->
                SeverityFilterActions.currentSelectedSeverities.contains(baseCheckovResult.severity)
            }.toMutableList()
        }

        fun sortResults(sourceList: MutableList<BaseCheckovResult>) {
            sourceList.sortWith(checkovResultsComparator)
        }

        fun filterResultsByCategoriesAndSeverities(sourceList: List<BaseCheckovResult>, categories: List<Category>? = null, severities: List<Severity>? = null): MutableList<BaseCheckovResult> {
            val checkovResults = filterResultsByCategories(sourceList, categories)
            return filterResultBySeverities(checkovResults, severities)
        }

    }
}