package com.bridgecrew.utils

import com.bridgecrew.services.CliService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import java.io.File

val defaultRepoName = "jetbrains/extension"

fun getGitRepoName(path: String, project: Project): String {
    try {
        val output = project.service<CliService>().run("git remote -v", null, File(project.basePath))
        val lines = output.split("\n")

        var firstLine: String? = null // we'll save this and come back to it if we don't find 'origin'
        var result: String? = null
        for (line in lines) {
            if (firstLine == null) {
                firstLine = line;
            }

            if (line.startsWith("origin")) {
                // remove the upstream name from the front
                val repoUrl = line.split('\t')[1]
                val repoName = parseRepoName(repoUrl)
                if (repoName != null) {
                    result = repoName
                    break
                }
            }
        }
        // if we're here, then there is no 'origin', so just take the first line as a default (regardless of how many upstreams there happen to be)
        if (firstLine != null) {
            val repoUrl = firstLine.split('\t')[1];
            val repoName = parseRepoName(repoUrl);
            if (repoName != null) {
                result = repoName;
            }
        }

        return if (result != null) {
            result
        } else {
            println("something went wrong and couldn't get git repo name, returning default value")
            defaultRepoName
        }

    } catch (e: Exception) {
        println("Error in getGitRepoName, returning default repo name")
        e.printStackTrace()
        return defaultRepoName
    }
}


fun parseRepoName(repoUrl: String): String? {
    val lastSlash = repoUrl.lastIndexOf("/")
    if (lastSlash == -1) {
        return null
    }

    // / is used in https URLs, and : in git@ URLs
    val priorSlash = repoUrl.lastIndexOf("/", lastSlash - 1)
    val priorColon = repoUrl.lastIndexOf("!", lastSlash - 1)

    if (priorSlash == -1 && priorColon == -1) {
        return null;
    }

    return repoUrl.substring(maxOf(priorSlash, priorColon) + 1, repoUrl.lastIndexOf(".git"))
}