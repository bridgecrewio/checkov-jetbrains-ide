package com.bridgecrew.services

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.bridgecrew.results.Severity
import com.bridgecrew.ui.CheckovToolWindowFactory
import com.bridgecrew.ui.actions.SeverityFilterActions

class CheckovResultsListUtils {
    companion object {
        private val checkovResultsComparator: Comparator<BaseCheckovResult> = CheckovResultsComparatorGenerator.generateCheckovResultComparator()

        fun filterResultsByCategories(sourceList: List<BaseCheckovResult>, categories: List<Category>?): List<BaseCheckovResult> {
            if (!categories.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    categories.contains(baseCheckovResult.category)
                }.toMutableList()
            }

            val selectedCategory = CheckovToolWindowFactory.lastSelectedCategory

            if (selectedCategory == null) {
                return sourceList.toMutableList()
            }

            return sourceList.filter { baseCheckovResult -> selectedCategory == baseCheckovResult.category }
        }

        fun filterResultBySeverities(sourceList: List<BaseCheckovResult>, severities: List<Severity>?): List<BaseCheckovResult> {
            if (!severities.isNullOrEmpty()) {
                return sourceList.filter { baseCheckovResult ->
                    severities.contains(baseCheckovResult.severity)
                }
            }

            return sourceList.filter { baseCheckovResult ->
                SeverityFilterActions.currentSelectedSeverities.contains(baseCheckovResult.severity)
            }
        }

        fun sortResults(sourceList: MutableList<BaseCheckovResult>) {
            sourceList.sortWith(checkovResultsComparator)
        }

        fun filterResultsByCategoriesAndSeverities(sourceList: List<BaseCheckovResult>, categories: List<Category>? = null, severities: List<Severity>? = null): List<BaseCheckovResult> {
            val checkovResults = filterResultsByCategories(sourceList, categories)
            return filterResultBySeverities(checkovResults, severities)
        }

        fun getCheckovResultsByPath(sourceList: List<BaseCheckovResult>, filePath: String): List<BaseCheckovResult> {
            return sourceList.filter {baseCheckovResult ->
                baseCheckovResult.filePath == "/${filePath}"
            }
        }

        fun getCurrentResultsSeverities(sourceList: List<BaseCheckovResult>): List<Severity> {
            return sourceList.map { checkovResult -> checkovResult.severity }.distinct()
        }

        fun getCurrentResultsCategories(sourceList: List<BaseCheckovResult>): List<Category> {
            return sourceList.map { checkovResult -> checkovResult.category }.distinct()
        }

    }
}