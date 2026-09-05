package com.example.key_logging.security.accessibility

import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo

/**
 * Responsible for sanitizing AccessibilityNodeInfo objects to prevent sensitive data leakage.
 */
object AccessibilityNodeSanitizer {

    fun sanitize(node: AccessibilityNodeInfo?) {
        if (node == null) return

        // Log to verify it's working
        // Log.d("AccessibilityShield", "Sanitizing node: ${node.className}")

        // 1. Mandatory sanitization for sensitive nodes
        node.text = null
        node.isPassword = true
        node.isEditable = false

        // 2. Clear other textual channels
        node.contentDescription = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            node.hintText = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            node.error = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            node.tooltipText = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            node.stateDescription = null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            node.setTextSelection(0, 0)
        }
        
        // 3. Clear extras to prevent metadata leaks
        try {
            node.extras.clear()
        } catch (e: Exception) {}
    }
}
