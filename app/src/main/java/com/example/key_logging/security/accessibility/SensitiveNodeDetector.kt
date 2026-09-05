package com.example.key_logging.security.accessibility

import android.view.accessibility.AccessibilityNodeInfo

/**
 * Detects whether an AccessibilityNodeInfo represents a sensitive node.
 */
object SensitiveNodeDetector {

    enum class ProtectionMode {
        EDITABLE_ONLY,
        EDITABLE_AND_PASSWORD,
        STRICT
    }

    // Default to EDITABLE_AND_PASSWORD for production to maintain balance 
    // between security and TalkBack usability.
    var currentMode: ProtectionMode = ProtectionMode.EDITABLE_AND_PASSWORD

    fun isSensitive(node: AccessibilityNodeInfo?): Boolean {
        if (node == null) return false

        return when (currentMode) {
            ProtectionMode.EDITABLE_ONLY -> node.isEditable
            ProtectionMode.EDITABLE_AND_PASSWORD -> node.isEditable || node.isPassword
            ProtectionMode.STRICT -> true // Protect everything for verification
        }
    }
}
