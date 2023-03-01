package com.bridgecrew.utils

import com.bridgecrew.results.BaseCheckovResult


fun isCustomPolicy(result: BaseCheckovResult): Boolean{
    return !result.id.startsWith("CKV")
}