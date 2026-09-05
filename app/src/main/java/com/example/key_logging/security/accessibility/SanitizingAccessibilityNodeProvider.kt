package com.example.key_logging.security.accessibility

import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityNodeProvider
import androidx.annotation.RequiresApi

/**
 * A wrapper for AccessibilityNodeProvider that sanitizes returned AccessibilityNodeInfo objects.
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class SanitizingAccessibilityNodeProvider(
    private val original: AccessibilityNodeProvider
) : AccessibilityNodeProvider() {

    override fun createAccessibilityNodeInfo(virtualViewId: Int): AccessibilityNodeInfo? {
        val node = original.createAccessibilityNodeInfo(virtualViewId)
        if (node != null && SensitiveNodeDetector.isSensitive(node)) {
            AccessibilityNodeSanitizer.sanitize(node)
        }
        return node
    }

    override fun findAccessibilityNodeInfosByText(
        text: String?,
        virtualViewId: Int
    ): List<AccessibilityNodeInfo>? {
        val nodes = original.findAccessibilityNodeInfosByText(text, virtualViewId)
        nodes?.forEach { node ->
            if (SensitiveNodeDetector.isSensitive(node)) {
                AccessibilityNodeSanitizer.sanitize(node)
            }
        }
        return nodes
    }

    override fun performAction(virtualViewId: Int, action: Int, arguments: Bundle?): Boolean {
        return original.performAction(virtualViewId, action, arguments)
    }

    override fun findFocus(focus: Int): AccessibilityNodeInfo? {
        val node = original.findFocus(focus)
        if (node != null && SensitiveNodeDetector.isSensitive(node)) {
            AccessibilityNodeSanitizer.sanitize(node)
        }
        return node
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun addExtraDataToAccessibilityNodeInfo(
        virtualViewId: Int,
        info: AccessibilityNodeInfo?,
        extraDataKey: String?,
        arguments: Bundle?
    ) {
        original.addExtraDataToAccessibilityNodeInfo(virtualViewId, info, extraDataKey, arguments)
        if (info != null && SensitiveNodeDetector.isSensitive(info)) {
            AccessibilityNodeSanitizer.sanitize(info)
        }
    }
}
