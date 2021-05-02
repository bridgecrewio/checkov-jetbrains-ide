package com.github.niradler.checkovjetbrainsidea.services.checkov

interface CheckovRunner {
    fun installOrUpdate() : Boolean
    fun run()
}