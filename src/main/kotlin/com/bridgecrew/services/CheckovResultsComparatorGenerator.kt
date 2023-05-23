package com.bridgecrew.services

import com.bridgecrew.results.BaseCheckovResult
import com.bridgecrew.results.Category
import com.intellij.openapi.diagnostic.logger

class CheckovResultsComparatorGenerator {

    enum class CheckovResultProperty {
        FILE_PATH,
        RESOURCE,
        NAME,
    }

    companion object {
        private val LOG = logger<CheckovResultProperty>()
        fun generateCheckovResultComparator(): Comparator<BaseCheckovResult> {
            return generateNameComparator(CheckovResultProperty.FILE_PATH)
                    .thenComparing(generateResourceComparator())
                    .thenBy { it.severity }
                    .thenComparing(generateNameComparator(CheckovResultProperty.NAME))
        }

        private fun generateResourceComparator(): Comparator<BaseCheckovResult> {
            return Comparator { result1, result2 ->

                if (result1.category != Category.SECRETS && result2.category != Category.SECRETS) {
                    return@Comparator generateNameComparator(CheckovResultProperty.RESOURCE).compare(result1, result2)
                }

                return@Comparator result1.category.compareTo(result2.category)
            }
        }

        private fun generateNameComparator(property: CheckovResultProperty): Comparator<BaseCheckovResult> {
            return Comparator { result1, result2 ->
                val name1 = extractNameByProperty(property, result1)
                val name2 = extractNameByProperty(property, result2)
                return@Comparator try {
                    getAlphanumericComparator().compare(name1, name2)
                } catch (e: Exception) {
                    LOG.warn("Error while comparing ${property.name.lowercase()}", e)
                    name1.compareTo(name2, true)
                }
            }
        }

        // compare file names alphanumerically and also account for special characters like " ' / : . - _
        private fun getAlphanumericComparator(): Comparator<String> {
            return Comparator { a, b ->
                val regex = Regex("(\\d+)|(\\D+)")
                val partsA = regex.findAll(a).map { it.value }
                val partsB = regex.findAll(b).map { it.value }

                val minSize = minOf(partsA.count(), partsB.count())

                for (i in 0 until minSize) {
                    val partA = partsA.elementAt(i)
                    val partB = partsB.elementAt(i)

                    val result = when {
                        partA.matches("\\d+".toRegex()) && partB.matches("\\d+".toRegex()) -> {
                            partA.toBigInteger().compareTo(partB.toBigInteger())
                        }

                        partA == partB -> 0
                        else -> partA.compareTo(partB, ignoreCase = true)
                    }

                    if (result != 0) {
                        return@Comparator result
                    }
                }

                return@Comparator partsA.count().compareTo(partsB.count())
            }
        }

        private fun extractNameByProperty(property: CheckovResultProperty, checkovResult: BaseCheckovResult): String {
            return when (property) {
                CheckovResultProperty.FILE_PATH -> {
                    checkovResult.filePath
                }

                CheckovResultProperty.RESOURCE -> {
                    checkovResult.resource
                }

                else -> {
                    checkovResult.name
                }
            }
        }
    }
}