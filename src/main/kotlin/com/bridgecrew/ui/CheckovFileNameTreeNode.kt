package com.bridgecrew.ui

import com.bridgecrew.CheckovResult

class CheckovFileNameTreeNode(val checkovResult: CheckovResult): CheckovTreeNode(checkovResult) {
    override fun toString(): String {
        return checkovResult.file_path;
    }
}