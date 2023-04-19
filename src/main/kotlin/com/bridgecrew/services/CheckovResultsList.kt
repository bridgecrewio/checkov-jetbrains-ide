//package com.bridgecrew.services
//
//import com.bridgecrew.results.BaseCheckovResult
//import com.bridgecrew.results.Category
//import com.bridgecrew.results.Severity
//import com.bridgecrew.ui.CheckovToolWindowFactory
//import com.bridgecrew.ui.actions.SeverityFilterActions
//
//class CheckovResultsList : MutableList<BaseCheckovResult> by mutableListOf() {
//
//    private val checkovResultsComparator: Comparator<BaseCheckovResult> = CheckovResultsComparatorGenerator.generateCheckovResultComparator()
//
//    fun filterResultsByCategories(categories: List<Category>? = null): CheckovResultsList {
//        if(!categories.isNullOrEmpty()) {
//            return CheckovResultsList(this.filter { baseCheckovResult ->
//                categories.contains(baseCheckovResult.category)
//            }.toMutableList())
//        }
//
//        val selectedCategory = CheckovToolWindowFactory.lastSelectedCategory
//
//        if (selectedCategory == null) {
//            return this
//        }
//
//        return this.filter { baseCheckovResult -> selectedCategory == baseCheckovResult.category } as CheckovResultsList
//    }
//
//    fun filterResultBySeverities(severities: List<Severity>? = null): CheckovResultsList {
//        if(!severities.isNullOrEmpty()) {
//            return this.filter { baseCheckovResult ->
//                severities.contains(baseCheckovResult.severity)
//            } as CheckovResultsList
//        }
//
//        return this.filter { baseCheckovResult ->
//            SeverityFilterActions.currentSelectedSeverities.contains(baseCheckovResult.severity)
//        } as CheckovResultsList
//    }
//
//    fun sortCheckovResults(): CheckovResultsList {
//        this.sortWith(checkovResultsComparator)
//        return this
//    }
//}