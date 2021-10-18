package com.bridgecrew.ui

import com.bridgecrew.CheckovResult

class CheckovCheckNameTreeNode(val checkovResult: CheckovResult): CheckovTreeNode(checkovResult) {
    override fun toString(): String {
        return checkovResult.check_name
    }
}