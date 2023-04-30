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

        fun generateResourceComparator(): Comparator<BaseCheckovResult> {
            return Comparator { result1, result2 ->

                if (result1.category != Category.SECRETS && result2.category != Category.SECRETS) {
                    return@Comparator compareResultsByName(CheckovResultProperty.RESOURCE, result1, result2)
                }

                return@Comparator result1.category.compareTo(result2.category)
            }
        }

        fun generateNameComparator(property: CheckovResultProperty): Comparator<BaseCheckovResult> {
            return Comparator { result1, result2 ->
                return@Comparator compareResultsByName(property, result1, result2)
            }
        }

        fun compareResultsByName(property: CheckovResultProperty, result1: BaseCheckovResult, result2: BaseCheckovResult): Int {

            val name1 = extractNameByProperty(property, result1)
            val name2 = extractNameByProperty(property, result2)
            try {
                if (name1.equals(name2, true))
                    return 0

                val regex = Regex("""\d+|\D+""")

                val tokens1: List<String> = regex.findAll(name1).map { it.groupValues.first() }.toList()
                val tokens2: List<String> = regex.findAll(name2).map { it.groupValues.first() }.toList()

                if (tokens1.isEmpty() && tokens2.isEmpty())
                    return name1.compareTo(name2, true)

                if (tokens1.size == 1 || tokens2.size == 1)
                    return name1.compareTo(name2, true)

                var i = 0
                while (i < tokens1.size && i < tokens2.size && tokens1[i].equals(tokens2[i], true)) {
                    i++
                }

                if (i >= tokens1.size)
                    return 1
                else if (i >= tokens2.size)
                    return -1


                if (tokens1[i].isEmpty() || tokens2[i].isEmpty())
                    LOG.info("empty...")
                val numberStr1 = tokens1[i].replace("\\D".toRegex(), "")
                val numberStr2 = tokens2[i].replace("\\D".toRegex(), "")

                if (numberStr1.isEmpty() || numberStr2.isEmpty())
                    return name1.compareTo(name2, true)

                return (numberStr1.toLong() - numberStr2.toLong()).toInt()
            } catch (e: Exception) {
                LOG.warn("Error while comparing ${property.name.lowercase()}", e)
                return name1.compareTo(name2, true)
            }

        }

        fun extractNameByProperty(property: CheckovResultProperty, checkovResult: BaseCheckovResult): String {
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