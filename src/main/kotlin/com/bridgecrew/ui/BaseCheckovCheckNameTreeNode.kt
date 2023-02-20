package com.bridgecrew.ui

// TODO - remove

import com.bridgecrew.CheckovResult
import com.bridgecrew.results.BaseCheckovResult

class BaseCheckovCheckNameTreeNode(val checkovResult: BaseCheckovResult) {
    override fun toString(): String {
        return checkovResult.name
    }
}