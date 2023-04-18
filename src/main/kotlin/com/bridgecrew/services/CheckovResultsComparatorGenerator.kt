package com.bridgecrew.services

import com.bridgecrew.results.BaseCheckovResult
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
                    .thenComparing(generateNameComparator(CheckovResultProperty.RESOURCE))
                    .thenBy { it.severity }
                    .thenComparing(generateNameComparator(CheckovResultProperty.NAME))
        }

        fun generateNameComparator(property: CheckovResultProperty): Comparator<BaseCheckovResult> {
            return Comparator { result1, result2 ->
                val name1 = extractNameByProperty(property, result1)
                val name2 = extractNameByProperty(property, result2)
                try {
                    if (name1.equals(name2, true))
                        return@Comparator 0

                    val regex = Regex("""\d+|\D+""")

                    val tokens1: List<String> = regex.findAll(name1).map { it.groupValues.first() }.toList()
                    val tokens2: List<String> = regex.findAll(name2).map { it.groupValues.first() }.toList()

                    if (tokens1.isEmpty() && tokens2.isEmpty())
                        return@Comparator name1.compareTo(name2, true)

                    if (tokens1.size == 1 || tokens2.size == 1)
                        return@Comparator name1.compareTo(name2, true)

                    var i = 0
                    while (i < tokens1.size && i < tokens2.size && tokens1[i].equals(tokens2[i], true)) {
                        i++
                    }

                    if (i >= tokens1.size)
                        return@Comparator 1
                    else if (i >= tokens2.size)
                        return@Comparator -1


                    if (tokens1[i].isEmpty() || tokens2[i].isEmpty())
                        LOG.info("empty...")
                    val numberStr1 = tokens1[i].replace("\\D".toRegex(), "")
                    val numberStr2 = tokens2[i].replace("\\D".toRegex(), "")

                    if (numberStr1.isEmpty() || numberStr2.isEmpty())
                        return@Comparator name1.compareTo(name2, true)

                    return@Comparator (numberStr1.toLong() - numberStr2.toLong()).toInt()
                } catch (e: Exception) {
                    LOG.warn("Error while comparing ${property.name.lowercase()}", e)
                    return@Comparator name1.compareTo(name2, true)
                }
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