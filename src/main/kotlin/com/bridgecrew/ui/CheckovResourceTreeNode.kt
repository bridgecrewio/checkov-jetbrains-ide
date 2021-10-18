package com.bridgecrew.ui

import com.bridgecrew.CheckovResult

class CheckovResourceTreeNode(val checkovResult: CheckovResult): CheckovTreeNode(checkovResult) {
    override fun toString(): String {
        return checkovResult.resource;
    }
}